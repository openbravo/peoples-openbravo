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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;

/**
 * Provides a common Registry for MessageClients
 */
public class MessageClientRegistry implements OBSingleton {
  private static final Logger logger = LogManager.getLogger();
  Map<String, MessageClient> messageClientsBySessionId;
  Map<String, MessageClient> messageClientsByUserId;

  private static MessageClientRegistry instance;

  public static MessageClientRegistry getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(MessageClientRegistry.class);
      instance.messageClientsBySessionId = new HashMap<>();
      instance.messageClientsByUserId = new HashMap<>();
    }
    return instance;
  }

  /**
   * Registers a connected message client in the MessageClientRegistry
   *
   * @param messageClient
   *          - Message client to be registered
   */
  public void registerClient(MessageClient messageClient) {
    String messageClientSearchKey = messageClient.getSearchKey();
    if (messageClientsBySessionId.containsKey(messageClientSearchKey)) {
      logger.warn(
          "A message client already registered for searchKey: {}, overwriting it with the new one.",
          messageClientSearchKey);
    }

    messageClientsBySessionId.put(messageClientSearchKey, messageClient);
    messageClientsByUserId.put(messageClient.getUserId(), messageClient);
  }

  /**
   * Removes a message client from the Registry
   *
   * @param searchKey
   *          - searchKey of the message client to be removed
   */
  public void removeClient(String searchKey) {
    if (!messageClientsBySessionId.containsKey(searchKey)) {
      logger.warn("Trying to remove a non registered message client: {}. Ignoring.", searchKey);
    }

    MessageClient messageClientRemoved = messageClientsBySessionId.get(searchKey);
    messageClientsByUserId.remove(messageClientRemoved.getUserId());
    messageClientsBySessionId.remove(searchKey);
  }

  /**
   * Internal use, returns all the connected MessageClients.
   *
   * @return all the connected MessageClients.
   */
  protected List<MessageClient> getAllClients() {
    return new ArrayList<>(messageClientsBySessionId.values());
  }
}
