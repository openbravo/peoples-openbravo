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
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.cluster;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.service.db.DalConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will be extended by those classes that implements a service which supports working in
 * a clustered environment.
 */
@ApplicationScoped
public abstract class ClusterService {
  private static final Logger log = LoggerFactory.getLogger(ClusterService.class);
  private static final Long DEFAULT_TIMEOUT = 10_000L;
  private static final Long MIN_THRESHOLD = 1000L;
  private static final Long MAX_THRESHOLD = 5000L;

  private Long timeout;
  private Long threshold;
  private Long nextPing;
  private String nodeName;
  private boolean handledInCurrentNode;
  private boolean initialized = false;
  private boolean useCache = false;

  protected boolean init(String currentNodeName) {
    if (!isEnabled()) {
      return false;
    }
    this.nodeName = currentNodeName;
    this.timeout = getClusterServiceTimeout();
    this.threshold = calculateThreshold(timeout);
    this.initialized = true;
    return true;
  }

  private Long getClusterServiceTimeout() {
    DalConnectionProvider dcp = new DalConnectionProvider(false);
    Connection connection = null;
    try {
      connection = dcp.getTransactionConnection();
      String serviceTimeout = ClusterServiceData.getServiceTimeout(connection, dcp, getName());
      if (serviceTimeout == null) {
        return DEFAULT_TIMEOUT;
      }
      // the timeout is defined in the AD in seconds, convert to milliseconds
      return Long.parseLong(serviceTimeout) * 1000;
    } catch (Exception ex) {
      log.error("Could not retrieve the settings for service {}", getName(), ex);
      return DEFAULT_TIMEOUT;
    } finally {
      try {
        if (connection != null) {
          dcp.releaseCommitConnection(connection);
        }
      } catch (SQLException ex) {
        log.error("Error closing connection", ex);
      }
    }
  }

  private Long calculateThreshold(Long amount) {
    long result = amount * 10 / 100;
    if (result < MIN_THRESHOLD) {
      return MIN_THRESHOLD;
    } else if (result > MAX_THRESHOLD) {
      return MAX_THRESHOLD;
    } else {
      return result;
    }
  }

  protected Long getTimeout() {
    return timeout;
  }

  protected Long getThreshold() {
    // The threshold is an extra amount of time added to the timeout that helps to avoid
    // unnecessarily switching the node that should handle a service on every ping round.
    return threshold;
  }

  protected Long getNextPing() {
    return nextPing;
  }

  protected void setNextPing(Long nextPing) {
    this.nextPing = nextPing;
  }

  protected boolean isInitialized() {
    return initialized;
  }

  protected boolean isHandledInCurrentNode() {
    if (!ClusterServiceManager.isCluster()) {
      return true;
    }
    if (!initialized || nextPing == null) {
      return false;
    }
    long lastPingTime = nextPing + timeout;
    long now = new Date().getTime();
    if (!useCache || now > lastPingTime) {
      // check in the database if this service is being handled in the current node
      handledInCurrentNode = isHandlingService();
      setUseCache(true);
    }
    return handledInCurrentNode;
  }

  private boolean isHandlingService() {
    DalConnectionProvider dcp = new DalConnectionProvider(false);
    Connection connection = null;
    try {
      connection = dcp.getTransactionConnection();
      String nodeInCharge = ClusterServiceData.getNodeHandlingService(connection, dcp, getName());
      if (nodeInCharge == null) {
        return false;
      }
      return nodeName.equals(nodeInCharge);
    } catch (Exception ex) {
      log.error("Could not retrieve node in charge of service {}", getName(), ex);
      return false;
    } finally {
      try {
        if (connection != null) {
          dcp.releaseCommitConnection(connection);
        }
      } catch (SQLException ex) {
        log.error("Error closing connection", ex);
      }
    }
  }

  protected void setUseCache(boolean useCache) {
    this.useCache = useCache;
  }

  /**
   * @return a {@code String} that uniquely identifies the service.
   */
  public abstract String getName();

  /**
   * @return {@code true} if the service currently is running in the present cluster node,
   *         {@code false} otherwise.
   */
  public abstract boolean isAlive();

  /**
   * @return {@code true} if it is allowed to execute this service in the present cluster node,
   *         {@code false} otherwise.
   */
  public abstract boolean isEnabled();

}
