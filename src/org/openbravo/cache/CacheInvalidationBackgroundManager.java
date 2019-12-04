/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2008-2019 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  Mauricio Peccorini.
 ************************************************************************
 */

package org.openbravo.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.jmx.MBeanRegistry;

@WebListener
@ApplicationScoped
public class CacheInvalidationBackgroundManager implements CacheInvalidationBackgroundManagerMBean {

  private static final String CACHE_INVALIDATION_BACKGROUND_MANAGER = "Caché Invalidation Background Manager";
  private static final String CACHE_INVALIDATION_CHECK_PERIOD_PREFERENCE = "CacheInvalidationControlPeriod";
  private static final long DEFAULT_PERIOD = 10000; // 10 seconds

  private static final Logger logger = Logger.getLogger(CacheInvalidationBackgroundManager.class);
  public static final String CONTEXT_SYSTEM_USER = "0";

  private boolean isShutDown;
  private ExecutorService executorService;

  @Inject
  @Any
  private Instance<CacheInvalidationHelper> invalidationHelpers;

  private Map<String, CacheInvalidationRecord> cacheControl = new HashMap<String, CacheInvalidationRecord>();

  public void listen(@Observes StartEvent event) {
    MBeanRegistry.registerMBean("CacheInvalidationBackgroundManager", this);
    this.start();
  }

  /**
   * This method starts the cache invalidation background thread
   * 
   */
  @Override
  public void start() {
    long period;
    try {
      period = Long
          .parseLong(Preferences.getPreferenceValue(CACHE_INVALIDATION_CHECK_PERIOD_PREFERENCE,
              false, (String) null, null, null, null, null));
    } catch (Exception e) {
      period = DEFAULT_PERIOD;
    }

    Iterator<CacheInvalidationHelper> helpers = invalidationHelpers.iterator();
    CacheInvalidationRecord record;
    while (helpers.hasNext()) {
      CacheInvalidationHelper helper = helpers.next();
      record = new CacheInvalidationRecord(helper.getCacheRecordSearchKey(), helper);
      cacheControl.put(record.getName(), record);
    }

    executorService = createExecutorService();
    CacheInvalidationThread thread = new CacheInvalidationThread(this, period);
    executorService.execute(thread);
    isShutDown = false;
  }

  @Override
  public boolean isStarted() {
    return !isShutDown;
  }

  @Override
  public void stop() {
    if (executorService == null) {
      return;
    }
    isShutDown = true;
    executorService.shutdownNow();
    executorService = null;
  }

  private ExecutorService createExecutorService() {
    return Executors.newSingleThreadExecutor(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable runnable) {
        SecurityManager s = System.getSecurityManager();
        ThreadGroup group = (s != null) ? s.getThreadGroup()
            : Thread.currentThread().getThreadGroup();
        final Thread thread = new Thread(group, runnable, CACHE_INVALIDATION_BACKGROUND_MANAGER);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
          thread.setPriority(Thread.NORM_PRIORITY);
        }
        thread.setDaemon(true);
        return thread;
      }
    });
  }

  private class CacheInvalidationRecord {
    private Date lastInvalidation;
    private CacheInvalidationHelper invalidationHelper;
    private String name;

    CacheInvalidationRecord(String name, CacheInvalidationHelper helper) {
      this.name = name;
      this.invalidationHelper = helper;
    }

    public Date getLastInvalidation() {
      return lastInvalidation;
    }

    public void setLastInvalidation(Date lastInvalidation) {
      this.lastInvalidation = lastInvalidation;
    }

    public CacheInvalidationHelper getInvalidationHelper() {
      return invalidationHelper;
    }

    public String getName() {
      return name;
    }
  }

  private static class CacheInvalidationThread implements Runnable {

    private final CacheInvalidationBackgroundManager manager;
    private long timeBetweenThreadExecutions;

    public CacheInvalidationThread(CacheInvalidationBackgroundManager manager, long period) {
      this.manager = manager;
      this.timeBetweenThreadExecutions = period;
    }

    @Override
    public void run() {
      OBCriteria<Cache> cacheCriteria;
      List<Cache> cacheList;
      CacheInvalidationRecord controlRecord;
      try {
        OBContext.setOBContext(CONTEXT_SYSTEM_USER);
        OBContext.setAdminMode(false);
        cacheCriteria = OBDal.getInstance().createCriteria(Cache.class);
        while (true) {
          if (manager.isShutDown) {
            return;
          }
          cacheList = cacheCriteria.list();
          for (Cache cache : cacheList) {
            controlRecord = manager.cacheControl.get(cache.getValue());
            if (controlRecord != null) {
              if (controlRecord.getLastInvalidation() == null || controlRecord.getLastInvalidation()
                  .compareTo(cache.getLastInvalidation()) < 0) {
                if (!controlRecord.getInvalidationHelper().invalidateCache()) {
                  logger.error(
                      "Caché invalidation for caché " + controlRecord.getName() + " has failed.");
                } else {
                  controlRecord.setLastInvalidation(cache.getLastInvalidation());
                  logger.debug("Caché " + controlRecord.getName() + " has been invalidated");
                }
              }
            }
          }
          OBDal.getInstance().getSession().clear();

          try {
            Thread.sleep(timeBetweenThreadExecutions);
          } catch (Exception ignored) {
            break;
          }

        }

      } catch (Throwable e) {
        logger.error(e.getMessage(), e);
      } finally {
        OBContext.restorePreviousMode();
      }
    }
  }

  static class StartEvent {
  }
}
