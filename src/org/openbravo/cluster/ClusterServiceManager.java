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
 * All portions are Copyright (C) 2017-2018 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.cluster;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.axis.utils.StringUtils;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.ConfigParameters;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.jmx.MBeanRegistry;
import org.openbravo.model.ad.system.ADClusterService;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class in charge of registering the node that should handle a particular service when working in a
 * clustered environment.
 */
@ApplicationScoped
public class ClusterServiceManager implements ClusterServiceManagerMBean {
  private static final Logger log = LoggerFactory.getLogger(ClusterServiceManager.class);
  private static final String UNKNOWN = "Unknown";
  private static boolean isCluster = OBPropertiesProvider.getInstance().getBooleanProperty(
      "cluster");

  private boolean isShutDown;
  private String nodeId;
  private String nodeName;
  private Date lastPing;
  private ExecutorService executorService;

  @Inject
  @Any
  private Instance<ClusterService> clusterServices;

  /**
   * Initializes the ClusterServiceManager and starts the thread in charge of registering the node
   * in charge of a particular service. This method has no effect if the application is not running
   * in a clustered environment.
   */
  public void start() {
    if (!isCluster()) {
      return;
    }
    nodeId = SequenceIdData.getUUID();
    nodeName = getNodeName();
    isShutDown = false;
    log.info("Starting Cluster Service Manager - Node ID: {}, Node Name: {}", nodeId, nodeName);
    // register as JMX Bean
    MBeanRegistry.registerMBean(this.getClass().getSimpleName(), this);
    // start the ping thread
    executorService = createExecutorService();
    ClusterServiceThread thread = new ClusterServiceThread(this);
    executorService.execute(thread);
  }

  /**
   * @return a single-threaded ExecutorService that creates threads which have daemon set to true.
   */
  private ExecutorService createExecutorService() {
    return Executors.newSingleThreadExecutor(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable runnable) {
        SecurityManager s = System.getSecurityManager();
        ThreadGroup group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
            .getThreadGroup();
        final Thread thread = new Thread(group, runnable, "Cluster Service Leader Registrator");
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
          thread.setPriority(Thread.NORM_PRIORITY);
        }
        thread.setDaemon(true);
        return thread;
      }
    });
  }

  /**
   * Stops the thread in charge of registering the node in charge of a particular service. This
   * method has no effect if the application is not running in a clustered environment.
   */
  public void shutdown() {
    if (!isCluster() || executorService == null) {
      return;
    }
    deRegisterServicesForCurrentNode();
    isShutDown = true;
    log.info("Shutting down Cluster Service Manager");
    executorService.shutdownNow();
    executorService = null;
  }

  private void deRegisterServicesForCurrentNode() {
    try {
      OBContext.setAdminMode(false); // allow to delete, the current context does not matter
      OBCriteria<ADClusterService> criteria = OBDal.getInstance().createCriteria(
          ADClusterService.class);
      criteria.add(Restrictions.eq(ADClusterService.PROPERTY_NODEID, nodeId));
      for (ADClusterService service : criteria.list()) {
        log.info("Degeristering node {} in charge of service {}", nodeId, service.getService());
        OBDal.getInstance().remove(service);
      }
      OBDal.getInstance().commitAndClose();
    } catch (Exception ex) {
      log.error("Could not deregister node {}", nodeId);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * @return {@code true} if the application is running in clustered environment, {@code false}
   *         otherwise.
   */
  protected static boolean isCluster() {
    return isCluster;
  }

  /**
   * @return a {@code String} with the name that identifies the current cluster node.
   */
  private String getNodeName() {
    String name = ConfigParameters.getMachineName();
    if (StringUtils.isEmpty(name)) {
      name = UNKNOWN;
    }
    return name;
  }

  /**
   * @param serviceName
   *          The name that identifies a service
   * @return {@code true} if the current cluster node should handle the service passed as parameter,
   *         {@code false} otherwise. Note that if we are not in a clustered environment, this
   *         method is always returning true.
   */
  public boolean isServiceHandledInCurrentNode(String serviceName) {
    if (!isCluster()) {
      return true;
    }
    ClusterService service = getClusterService(serviceName);
    if (service == null) {
      return false;
    }
    return service.isHandledInCurrentNode();
  }

  private ClusterService getClusterService(String serviceName) {
    for (ClusterService service : clusterServices) {
      if (serviceName.equals(service.getServiceName())) {
        return service;
      }
    }
    return null;
  }

  @Override
  public String getCurrentNodeId() {
    return nodeId;
  }

  @Override
  public String getCurrentNodeName() {
    return nodeName;
  }

  @Override
  public Date getLastPingOfCurrentNode() {
    return lastPing;
  }

  @Override
  public Map<String, String> getClusterServiceLeaders() {
    Map<String, String> leaders = new HashMap<>();
    try {
      OBContext.setAdminMode(true);
      OBCriteria<ADClusterService> criteria = OBDal.getInstance().createCriteria(
          ADClusterService.class);
      for (ADClusterService service : criteria.list()) {
        StringBuilder serviceInfo = new StringBuilder();
        serviceInfo.append("leader ID: " + service.getNodeID());
        serviceInfo.append(", leader name: " + service.getNodeName());
        serviceInfo.append(", last ping: " + service.getUpdated());
        leaders.put(service.getService(), serviceInfo.toString());
      }
      OBDal.getInstance().commitAndClose();
    } catch (Exception ignore) {
    } finally {
      OBContext.restorePreviousMode();
    }
    return leaders;
  }

  @Override
  public Map<String, String> getClusterServiceSettings() {
    Map<String, String> leaders = new HashMap<>();
    for (ClusterService service : clusterServices) {
      String serviceSettings = "timeout: " + service.getTimeout() + " milliseconds";
      leaders.put(service.getServiceName(), serviceSettings);
    }
    return leaders;
  }

  @Override
  public void enablePingForService(String serviceName) {
    ClusterService clusterService = getClusterService(serviceName);
    if (clusterService == null) {
      log.info("Can't enable ping for non-existent service {} in node {}", serviceName, nodeId);
      return;
    }
    if (!clusterService.isDisabled()) {
      log.info("Ping for service {} in node {} is already enabled", serviceName, nodeId);
      return;
    }
    // Enable the ping for the service
    clusterService.setDisabled(false);
    log.info("Enabled ping for service {} in node {}", serviceName, nodeId);
  }

  @Override
  public void disablePingForService(String serviceName) {
    ClusterService clusterService = getClusterService(serviceName);
    if (clusterService == null) {
      log.info("Can't disable ping for non-existent service {} in node {}", serviceName, nodeId);
      return;
    }
    if (clusterService.isDisabled()) {
      log.info("Ping for service {} in node {} is already disabled", serviceName, nodeId);
      return;
    }
    try {
      OBContext.setAdminMode(false); // allow to delete, the current context does not matter

      // If the current node is the leader of the service, unregister it
      OBCriteria<ADClusterService> criteria = OBDal.getInstance().createCriteria(
          ADClusterService.class);
      criteria.add(Restrictions.eq(ADClusterService.PROPERTY_NODEID, nodeId));
      criteria.add(Restrictions.eq(ADClusterService.PROPERTY_SERVICE, serviceName));
      ADClusterService service = (ADClusterService) criteria.uniqueResult();
      if (service != null) {
        log.info("Degeristering node {} in charge of service {}", nodeId, serviceName);
        OBDal.getInstance().remove(service);
      }
      OBDal.getInstance().commitAndClose();

      // Disable the ping for the service
      clusterService.setDisabled(true);
      // force the service to go to the database to see the changes (if any)
      clusterService.setUseCache(false);

      log.info("Disabled ping for service {} in node {}", serviceName, nodeId);
    } catch (Exception ex) {
      log.error("Could not force node {} to be in charge of service {}", nodeId, serviceName);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static class ClusterServiceThread implements Runnable {
    private final ClusterServiceManager manager;

    public ClusterServiceThread(ClusterServiceManager manager) {
      this.manager = manager;
    }

    @Override
    public void run() {

      if (!isCluster()) {
        // don't even start, we are not in cluster
        return;
      }

      // make ourselves an admin
      OBContext.setOBContext("0", "0", "0", "0");

      if (!registerAvailableClusterServices()) {
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

    private boolean registerAvailableClusterServices() {
      if (manager.clusterServices == null) {
        return false;
      }
      long current = System.currentTimeMillis();
      boolean anyServiceRegistered = false;
      for (ClusterService service : manager.clusterServices) {
        if (service.init(manager.nodeId)) {
          // service initialized properly, register it
          registerOrUpdateService(service);
          service.setNextPing(current + service.getTimeout());
          anyServiceRegistered = true;
          if (service.isHandledInCurrentNode()) {
            log.info("Service {} handled by current node {}", service.getServiceName(),
                manager.nodeId);
          } else {
            log.info("Service {} handled by other node ({})", service.getServiceName(),
                service.getNodeHandlingService());
          }
        }
      }
      return anyServiceRegistered;
    }

    private Long doPingRound() {
      long nextSleep = 0L;
      long startTime = System.currentTimeMillis();
      for (ClusterService service : manager.clusterServices) {
        if (!service.isAlive() || !service.isInitialized() || service.isDisabled()) {
          // Do not update the last ping: the service is not working
          log.debug("Service {} is not working in node {}", service, manager.nodeId);
          continue;
        }
        long current = System.currentTimeMillis();
        long sleep;
        Long serviceNextPing = service.getNextPing();
        if (serviceNextPing <= current) {
          registerOrUpdateService(service);
          service.setNextPing(current + service.getTimeout());
          sleep = service.getTimeout();
        } else {
          sleep = serviceNextPing - current;
        }
        if (sleep < nextSleep || nextSleep == 0) {
          nextSleep = sleep;
        }
      }
      log.debug("Ping round completed in {} milliseconds", (System.currentTimeMillis() - startTime));
      if (nextSleep == 0L) {
        // No service available to update its last ping, wait 30 seconds for the next round
        nextSleep = 30_000L;
      }
      return nextSleep;
    }

    private void registerOrUpdateService(ClusterService clusterService) {
      String serviceName = clusterService.getServiceName();
      try {
        ADClusterService service = getService(serviceName);
        Long interval = clusterService.getTimeout() + clusterService.getThreshold();
        Date now = new Date();
        if (service == null) {
          // register the service for the first time
          log.info("Registering current node {} in charge of service {}", manager.nodeId,
              serviceName);
          registerService(serviceName);
        } else if (manager.nodeId.equals(service.getNodeID())) {
          // current node is charge of handling the service, just update the last ping
          log.debug("Current node {} still in charge of service {}", manager.nodeId, serviceName);
          updateLastPing(serviceName, now);
        } else if (shouldReplaceNodeOfService(service, interval)) {
          // try to register the current node as the one in charge of handling the service
          log.info("Node {} in charge of service {} should be replaced", service.getNodeID(),
              serviceName);
          updateNodeOfService(service.getNodeID(), serviceName, now);
        } else {
          log.debug("Node {} still in charge of service {}", service.getNodeID(), serviceName);
        }
        manager.lastPing = now;
        OBDal.getInstance().commitAndClose();
        // force the service to go to the database to see the changes (if any)
        clusterService.setUseCache(false);
      } catch (Exception ex) {
        log.warn("Node {} could not complete register/update task of service {}", manager.nodeId,
            serviceName);
      }
    }

    private ADClusterService getService(String serviceName) {
      OBCriteria<ADClusterService> criteria = OBDal.getInstance().createCriteria(
          ADClusterService.class);
      criteria.add(Restrictions.eq(ADClusterService.PROPERTY_SERVICE, serviceName));
      return (ADClusterService) criteria.uniqueResult();
    }

    private ADClusterService registerService(String serviceName) {
      ADClusterService service = OBProvider.getInstance().get(ADClusterService.class);
      service.setOrganization(OBDal.getInstance().getProxy(Organization.class, "0"));
      service.setClient(OBDal.getInstance().getProxy(Client.class, "0"));
      service.setService(serviceName);
      service.setNodeID(manager.nodeId);
      service.setNodeName(manager.nodeName);
      OBDal.getInstance().save(service);
      return service;
    }

    private boolean shouldReplaceNodeOfService(ADClusterService service, Long intervalAmount) {
      long leaderLostTime = service.getUpdated().getTime() + intervalAmount;
      long now = new Date().getTime();
      return leaderLostTime < now;
    }

    private void updateNodeOfService(String formerNodeId, String serviceName, Date now) {
      StringBuilder hql = new StringBuilder();
      hql.append("UPDATE ADClusterService ");
      hql.append("SET nodeID = :newNodeId, nodeName = :newNodeName, updated = :updated ");
      hql.append("WHERE service = :service AND nodeID = :formerNodeId");
      Query updateQuery = OBDal.getInstance().getSession().createQuery(hql.toString());
      updateQuery.setParameter("newNodeId", manager.nodeId);
      updateQuery.setParameter("newNodeName", manager.nodeName);
      updateQuery.setParameter("updated", now);
      updateQuery.setParameter("service", serviceName);
      updateQuery.setParameter("formerNodeId", formerNodeId);
      int rowCount = updateQuery.executeUpdate();
      if (rowCount == 1) {
        log.info("Changed node in charge of service {}", serviceName);
        log.info("Replaced node {} with node {}", formerNodeId, manager.nodeId);
      }
    }

    private void updateLastPing(String serviceName, Date now) {
      StringBuilder hql = new StringBuilder();
      hql.append("UPDATE ADClusterService SET updated = :updated ");
      hql.append("WHERE service = :service AND nodeID = :currentNodeId");
      Query updateQuery = OBDal.getInstance().getSession().createQuery(hql.toString());
      updateQuery.setParameter("updated", now);
      updateQuery.setParameter("service", serviceName);
      updateQuery.setParameter("currentNodeId", manager.nodeId);
      updateQuery.executeUpdate();
    }
  }
}