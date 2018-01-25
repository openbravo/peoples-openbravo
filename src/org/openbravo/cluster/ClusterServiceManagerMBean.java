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
import java.util.Map;

/**
 * This interface allows to define the {@link ClusterServiceManager} class as an standard MBean that
 * allows to manage some of the cluster services settings through JMX.
 */
public interface ClusterServiceManagerMBean {

  /**
   * @return the name of the current cluster node.
   */
  public String getCurrentNodeName();

  /**
   * @return the Date of the last ping done (for any service) by the current node.
   */
  public Date getLastPingOfCurrentNode();

  /**
   * @return a Map with information (leader and last ping) per cluster service.
   */
  public Map<String, String> getClusterServiceLeaders();

  /**
   * @return a Map with information of the settings for each cluster service.
   */
  public Map<String, String> getClusterServiceSettings();

  /**
   * Forces the current node to be the node in charge of handling the service whose name is passed
   * as parameter.
   * 
   * @param serviceName
   *          the name of the service to be handled by the current node.
   */
  public void registerCurrentNodeForService(String serviceName);
}
