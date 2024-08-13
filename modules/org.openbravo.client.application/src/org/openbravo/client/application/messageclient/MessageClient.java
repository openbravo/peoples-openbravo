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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.messageclient;

import java.util.Date;
import java.util.List;

/**
 * Main message client class, which keeps track of the different properties of the Message Client
 * and also allows sending messages, which must be implemented by specific implementations (for
 * example, websockets)
 */
public abstract class MessageClient {
  String searchKey;
  String clientId;
  String organizationId;
  String userId;
  String roleId;
  Date timestampLastMsgSent;
  // TODO: Add listenedChannels/messageTypesSubscription
  List<String> subscribedTopics;

  public MessageClient(String searchKey, String clientId, String organizationId, String userId,
      String roleId) {
    this.searchKey = searchKey;
    this.clientId = clientId;
    this.organizationId = organizationId;
    this.userId = userId;
    this.roleId = roleId;
  }

  /**
   * Sends a given message to the MessageClient, should update the timestampLastMsgSent value
   * 
   * @param message
   *          Message to be sent
   */
  public abstract void sendMessage(String message);

  public void setSubscribedTopics(List<String> topics) {
    this.subscribedTopics = topics;
  }

  public String getSearchKey() {
    return searchKey;
  }

  public String getClientId() {
    return clientId;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public String getUserId() {
    return userId;
  }

  public String getRoleId() {
    return roleId;
  }

  public Date getTimestampLastMsgSent() {
    return timestampLastMsgSent;
  }

  public List<String> getSubscribedTopics() {
    return subscribedTopics;
  }
}
