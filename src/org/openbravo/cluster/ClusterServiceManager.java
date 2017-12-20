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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;

import org.apache.axis.utils.StringUtils;
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
    nodeName = getNodeName();
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
    if (service == null) {
      return false;
    }
    return nodeName.equals(service.getNode());
  }

  private String getNodeName() {
    String name = System.getProperty("machine.name");
    if (StringUtils.isEmpty(name)) {
      try {
        name = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {
        name = SequenceIdData.getUUID();
      }
    }
    return name;
  }

  private ADClusterService getService(String service) {
    OBCriteria<ADClusterService> criteria = OBDal.getInstance().createCriteria(
        ADClusterService.class);
    criteria.add(Restrictions.eq(ADClusterService.PROPERTY_SERVICE, service));
    return (ADClusterService) criteria.uniqueResult();
  }

  private static class ClusterServiceThread implements Runnable {
    private static final Long DEFAULT_TIMEOUT = 10_000L;
    // The threshold is an extra amount of time added to the timeout that helps to avoid constantly
    // switching the node that should handle a service on every ping round.
    private static final Long THRESHOLD = 1000L;

    private final ClusterServiceManager manager;
    private Map<String, Long> serviceNextPings;
    private Map<String, Long> serviceTimeouts;

    public ClusterServiceThread(ClusterServiceManager manager) {
      this.manager = manager;
      this.serviceNextPings = new HashMap<>();
      this.serviceTimeouts = new HashMap<>();
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
        registerOrUpdateService(service, timeout);
        serviceNextPings.put(settings.getService(), current + timeout);
        serviceTimeouts.put(service, timeout);
      }
    }

    private Long getTimeout(ADClusterServiceSettings settings) {
      if (settings.getTimeout() == null) {
        return DEFAULT_TIMEOUT;
      }
      // the timeout is defined in the AD in seconds, convert to milliseconds
      return settings.getTimeout() * 1000;
    }

    private Long doPingRound() {
      long nextSleep = 0L;
      long current = System.currentTimeMillis();
      for (Map.Entry<String, Long> entry : serviceNextPings.entrySet()) {
        long sleep;
        String service = entry.getKey();
        Long serviceNextPing = entry.getValue();
        if (serviceNextPing <= current) {
          registerOrUpdateService(service, serviceTimeouts.get(service));
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

    private void registerOrUpdateService(String serviceName, Long interval) {
      try {
        ADClusterService service = manager.getService(serviceName);
        if (service == null) {
          // register the service for the first time
          registerService(serviceName);
        } else if (manager.nodeName.equals(service.getNode())) {
          // current node is charge of handling the service, just update the last ping
          updateLastPing(service);
        } else if (shouldReplaceNodeOfService(service, interval + THRESHOLD)) {
          // try to register the current node as the one in charge of handling the service
          replaceNodeOfService(service);
        } else {
          // do nothing, other node is already handling the service
          log.debug("Node {} still in charge of service {}", service.getNode(),
              service.getService());
        }
      } catch (Exception ex) {
        log.warn("Node {} could not complete register/update task for service {}", serviceName,
            manager.nodeName);
      }
    }

    private boolean shouldReplaceNodeOfService(ADClusterService service, Long intervalAmount) {
      long leaderLostTime = service.getUpdated().getTime() + intervalAmount;
      long now = new Date().getTime();
      return leaderLostTime < now;
    }

    private void registerService(String serviceName) {
      ADClusterService service = OBProvider.getInstance().get(ADClusterService.class);
      service.setOrganization(OBDal.getInstance().getProxy(Organization.class, "0"));
      service.setClient(OBDal.getInstance().getProxy(Client.class, "0"));
      service.setService(serviceName);
      service.setNode(manager.nodeName);
      OBDal.getInstance().save(service);
      OBDal.getInstance().commitAndClose();
      log.info("Node {} registered in charge of service {}", manager.nodeName, serviceName);
    }

    private void updateLastPing(ADClusterService service) {
      service.setUpdated(new Date());
      OBDal.getInstance().commitAndClose();
      log.debug("Current node {} still in charge of service {}", manager.nodeName,
          service.getService());
    }

    private void replaceNodeOfService(ADClusterService service) {
      String formerLeader = service.getNode();
      log.info("Replacing node {} in charge of service {} ", formerLeader, service.getService());
      service.setNode(manager.nodeName);
      // the last ping (updated) will be updated automatically by the OBInterceptor
      OBDal.getInstance().commitAndClose();
      log.info("Node {} is now in charge of service {} ", manager.nodeName, service.getService());
    }
  }
}