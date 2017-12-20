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
 * All portions are Copyright (C) 2017 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.cluster;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;

import org.apache.axis.utils.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.ad.system.ADClusterService;
import org.openbravo.model.ad.system.ADClusterServiceSettings;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.importprocess.ImportEntryManager.DaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class in charge of registering the node that should handle a particular service when working in a
 * clustered environment.
 */
@ApplicationScoped
public class ClusterServiceManager {
  private static final Logger log = LoggerFactory.getLogger(ClusterServiceManager.class);

  private Boolean isCluster;
  private boolean isShutDown;
  private String nodeName;
  private ExecutorService executorService;

  public void start() {
    if (!isCluster()) {
      return;
    }
    isShutDown = false;
    log.info("Starting Cluster Service Manager");
    executorService = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
    ClusterServiceThread thread = new ClusterServiceThread(this);
    executorService.execute(thread);
  }

  public void shutdown() {
    if (!isCluster() || executorService == null) {
      return;
    }
    isShutDown = true;
    log.info("Shutting down Cluster Service Manager");
    executorService.shutdownNow();
    executorService = null;
  }

  public boolean isCluster() {
    if (isCluster == null) {
      isCluster = OBPropertiesProvider.getInstance().getBooleanProperty("cluster");
    }
    return isCluster;
  }

  public boolean nodeHandlesService(String serviceType) {
    if (!isCluster()) {
      return false;
    }
    ADClusterService service = getService(serviceType);
    return getNodeName().equals(service.getNode());
  }

  private String getNodeName() {
    if (nodeName != null) {
      return nodeName;
    }
    nodeName = System.getProperty("machine.name");
    if (StringUtils.isEmpty(nodeName)) {
      try {
        nodeName = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {
        nodeName = SequenceIdData.getUUID();
      }
    }
    return nodeName;
  }

  private ADClusterService getService(String service) {
    OBCriteria<ADClusterService> criteria = OBDal.getInstance().createCriteria(
        ADClusterService.class);
    criteria.add(Restrictions.eq(ADClusterService.PROPERTY_SERVICE, service));
    return (ADClusterService) criteria.uniqueResult();
  }

  private static class ClusterServiceThread implements Runnable {
    private static final Long DEFAULT_TIMEOUT = 10000L;
    private static final Long DEFAULT_THRESHOLD = 1000L;

    private final ClusterServiceManager manager;
    private Map<String, Long> serviceNextPings;
    private Map<String, Long> serviceTimeouts;
    private Map<String, Long> serviceThresholds;

    public ClusterServiceThread(ClusterServiceManager manager) {
      this.manager = manager;
      this.serviceNextPings = new HashMap<>();
      this.serviceTimeouts = new HashMap<>();
      this.serviceThresholds = new HashMap<>();
    }

    @Override
    public void run() {
      Thread.currentThread().setName("Cluster Service Leader Registrator");

      if (!manager.isCluster()) {
        // don't even start, we are not in cluster
        return;
      }

      // make ourselves an admin
      OBContext.setOBContext("0", "0", "0", "0");

      registerAvailableClusterServices();

      if (serviceNextPings.isEmpty()) {
        log.warn("Could not find any available cluster service");
        return;
      }

      while (true) {
        try {
          if (manager.isShutDown) {
            return;
          }
          Long nextSleep = doPingRound();
          // wait for the next ping round
          try {
            log.debug("Going to sleep {} milliseconds until the next ping", nextSleep);
            Thread.sleep(nextSleep);
          } catch (Exception ignored) {
          }
        } catch (Throwable t) {
          log.error(t.getMessage(), t);
        }
      }
    }

    private void registerAvailableClusterServices() {
      OBCriteria<ADClusterServiceSettings> criteria = OBDal.getInstance().createCriteria(
          ADClusterServiceSettings.class);
      List<ADClusterServiceSettings> serviceSettings = criteria.list();
      long current = System.currentTimeMillis();
      for (ADClusterServiceSettings settings : serviceSettings) {
        String service = settings.getService();
        Long timeout = getTimeout(settings);
        Long threshold = getThreshold(settings);
        register(service, timeout, threshold);
        serviceNextPings.put(settings.getService(), current + timeout);
        serviceTimeouts.put(service, timeout);
        serviceThresholds.put(service, threshold);
      }
    }

    private Long getTimeout(ADClusterServiceSettings settings) {
      if (settings.getTimeout() == null) {
        return DEFAULT_TIMEOUT;
      }
      return settings.getTimeout();
    }

    private Long getThreshold(ADClusterServiceSettings settings) {
      if (settings.getThreshold() == null) {
        return DEFAULT_THRESHOLD;
      }
      return settings.getThreshold();
    }

    private Long doPingRound() {
      long nextSleep = 0L;
      long current = System.currentTimeMillis();
      for (Map.Entry<String, Long> entry : serviceNextPings.entrySet()) {
        long sleep;
        String service = entry.getKey();
        Long serviceNextPing = entry.getValue();
        if (serviceNextPing <= current) {
          register(service, serviceTimeouts.get(service), serviceThresholds.get(service));
          entry.setValue(serviceNextPing + serviceTimeouts.get(service));
          sleep = serviceTimeouts.get(service);
        } else {
          sleep = serviceNextPing - current;
        }
        if (sleep < nextSleep || nextSleep == 0) {
          nextSleep = sleep;
        }
      }
      log.debug("Ping round completed in {} milliseconds", (System.currentTimeMillis() - current));
      return nextSleep;
    }

    private void register(String serviceType, Long interval, Long threshold) {
      try {
        ADClusterService service = manager.getService(serviceType);
        if (service == null) {
          // register the service for the first time
          service = createService(serviceType);
        } else if (manager.getNodeName().equals(service.getNode())) {
          // update the last ping
          service.setUpdated(new Date());
        } else if (service.getUpdated().before(
            substractMilliseconds(new Date(), interval + threshold))) {
          // register the current node as the one in charge of handling the service
          // the last ping (updated) will be updated automatically by the OBInterceptor
          service.setNode(manager.getNodeName());
        }
        if (service.isNewOBObject()) {
          OBDal.getInstance().save(service);
        }
        OBDal.getInstance().commitAndClose();
      } catch (HibernateException e) {
        log.warn("Could not register service for node {}. It may be already registered.",
            manager.getNodeName());
      }
    }

    private Date substractMilliseconds(Date date, Long interval) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.add(Calendar.MILLISECOND, interval.intValue() * -1);
      return calendar.getTime();
    }

    private ADClusterService createService(String service) {
      ADClusterService clusterService = OBProvider.getInstance().get(ADClusterService.class);
      clusterService.setOrganization(OBDal.getInstance().getProxy(Organization.class, "0"));
      clusterService.setClient(OBDal.getInstance().getProxy(Client.class, "0"));
      clusterService.setService(service);
      clusterService.setNode(manager.getNodeName());
      return clusterService;
    }
  }
}