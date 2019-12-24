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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.management.MBeanException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.ApplicationInitializer;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.SessionInfo;
import org.openbravo.jmx.MBeanRegistry;
import org.openbravo.model.ad.system.Cache;

@ApplicationScoped
public class CacheInvalidationBackgroundManager
    implements CacheInvalidationBackgroundManagerMBean, ApplicationInitializer {

  private static final String CACHE_INVALIDATION_BACKGROUND_MANAGER = "Cache Invalidation Background Manager";
  private static final String CACHE_INVALIDATION_CHECK_PERIOD_PROPERTY = "cache.invalidation.check.period";
  private static final long CACHE_INVALIDATION_DEFAULT_PERIOD = 10000; // 10 seconds

  private static final Logger logger = LogManager.getLogger();
  public static final String CONTEXT_SYSTEM_USER = "0";

  private boolean shutdownRequested = false;
  private ExecutorService executorService;

  @Inject
  @Any
  private Instance<CacheManager<?, ?>> cacheManagers;

  @Override
  public void initialize() {
    MBeanRegistry.registerMBean("CacheInvalidationBackgroundManager", this);
    try {
      this.start();
    } catch (Exception e) {
      logger.error("Failed to start the CacheInvalidationBackgroundManager", e);
    }
  }

  private static long getCacheInvalidationCheckPeriod() {
    final String propertyValue = OBPropertiesProvider.getInstance()
        .getOpenbravoProperties()
        .getProperty(CACHE_INVALIDATION_CHECK_PERIOD_PROPERTY);
    if (propertyValue == null) {
      return CACHE_INVALIDATION_DEFAULT_PERIOD;
    }
    try {
      return Long.parseLong(propertyValue);
    } catch (Exception e) {
      logger
          .error("An invalid value was specified in the " + CACHE_INVALIDATION_CHECK_PERIOD_PROPERTY
              + " property. The default value will be used.");
      return CACHE_INVALIDATION_DEFAULT_PERIOD;
    }
  }

  /**
   * This method starts the cache invalidation background thread
   * 
   */
  @Override
  public void start() throws MBeanException {
    long period;
    if (executorService != null) {
      throw new MBeanException(
          new IllegalStateException("The CacheInvalidationBackgroundManager is already started"));
    }
    period = getCacheInvalidationCheckPeriod();
    shutdownRequested = false;
    executorService = createExecutorService();
    CacheInvalidationThread thread = new CacheInvalidationThread(this, period);
    executorService.execute(thread);
  }

  @Override
  public boolean isStarted() {
    return executorService != null;
  }

  @Override
  public void invalidateCache(String searchKey) throws MBeanException {
    if (searchKey == null) {
      return;
    }
    Instance<CacheManager<?, ?>> qualifiedCacheManagers = cacheManagers
        .select(new CacheManager.Selector(searchKey));
    for (CacheManager<?, ?> cacheManager : qualifiedCacheManagers) {
      cacheManager.invalidate();
    }
    throw new MBeanException(new NoSuchElementException("The CacheManager for " + searchKey
        + " was not injected into the CacheInvalidationBackgroundManager"));
  }

  @Override
  public void stop() throws MBeanException {
    if (executorService == null) {
      throw new MBeanException(
          new IllegalStateException("The CacheInvalidationBackgroundManager is not started"));
    }
    shutdownRequested = true;
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

  private static class CacheInvalidationThread implements Runnable {

    private final CacheInvalidationBackgroundManager manager;
    private long timeBetweenThreadExecutions;

    public CacheInvalidationThread(CacheInvalidationBackgroundManager manager, long period) {
      this.manager = manager;
      this.timeBetweenThreadExecutions = period;
    }

    @Override
    public void run() {
      List<String> missingCacheManagers = new ArrayList<String>();
      boolean found;
      OBContext.setOBContext(CONTEXT_SYSTEM_USER);
      logger.info(String.format(
          "The CacheInvalidationBackgroundManager has started. Check period is: %d ms",
          timeBetweenThreadExecutions));
      try {
        OBContext.setAdminMode(false);
        while (true) {
          try {
            if (manager.shutdownRequested) {
              return;
            }
            OBCriteria<Cache> cacheCriteria = OBDal.getInstance().createCriteria(Cache.class);
            cacheCriteria.add(Restrictions.isNotNull(Cache.PROPERTY_LASTINVALIDATION));
            if (!missingCacheManagers.isEmpty()) {
              cacheCriteria.add(Restrictions
                  .not(Restrictions.in(Cache.PROPERTY_SEARCHKEY, missingCacheManagers)));
            }
            List<Cache> cacheList = cacheCriteria.list();
            for (Cache cache : cacheList) {
              found = false;
              Instance<CacheManager<?, ?>> qualifiedCacheManagers = manager.cacheManagers
                  .select(new CacheManager.Selector(cache.getSearchKey()));
              for (CacheManager<?, ?> cacheManager : qualifiedCacheManagers) {
                cacheManager.invalidateIfExpired(cache.getLastInvalidation());
                found = true;
              }
              if (!found) {
                missingCacheManagers.add(cache.getSearchKey());
                logger.error("The CacheManager for " + cache.getSearchKey()
                    + " was not injected into the CacheInvalidationBackgroundManager");
              }
            }
            OBDal.getInstance().getSession().clear();
            OBDal.getInstance().commitAndClose();
            SessionInfo.init();
            try {
              Thread.sleep(timeBetweenThreadExecutions);
            } catch (Exception ignored) {
              break;
            }
          } catch (Throwable e) {
            logger.error(e.getMessage(), e);
          }
        }
      } finally {
        OBContext.restorePreviousMode();
      }
    }

  }
}
