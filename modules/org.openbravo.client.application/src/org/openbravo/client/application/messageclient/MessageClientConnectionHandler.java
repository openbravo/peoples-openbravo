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

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;

/**
 * Defines a common API for Connection Handlers to trigger connection calls(established/closed) and
 * handle messages and errors
 */
public class MessageClientConnectionHandler {
  private static final Logger log = LogManager.getLogger();

  static void connectionEstablished(MessageClient messageClient) {
    MessageClientRegistry.getInstance().registerClient(messageClient);
  }

  static void connectionClosed(String searchKey) {
    MessageClientRegistry.getInstance().removeClient(searchKey);
  }

  static String handleMessage(String message, String messageClientSearchKey) {
    log.debug(String.format("[Message Client] Message received from client %s. Message: %s",
        messageClientSearchKey, message));

    try {
      JSONObject jsonMessage = new JSONObject(message);
      String topic = jsonMessage.getString("topic");
      String messageToBeHandled = jsonMessage.getString("data");
      List<MessageHandler> messageHandlers = WeldUtils.getInstances(MessageHandler.class,
          new MessageHandler.Selector(topic));
      MessageClient messageClient = MessageClientRegistry.getInstance()
          .getBySearchKey(messageClientSearchKey);
      if (!messageHandlers.isEmpty() && messageClient != null) {
        String messageToSendBack = messageHandlers.get(0)
            .handleReceivedMessage(messageToBeHandled, messageClient);
        if (messageToSendBack != null && !messageToSendBack.isBlank()) {
          JSONObject jsonMessageToSendBack = new JSONObject(
              Map.of("data", messageToSendBack, "topic", topic));
          return jsonMessageToSendBack.toString();
        }
      }
    } catch (JSONException e) {
      throw new OBException("Could not handle non-json message.", e);
    }
    return null;
  }

  static void handleError(String sessionId, Throwable e) {
    log.error("Message Client with SessionID({}) - Received error: {}", sessionId, e);
    // Error always triggers a connection closed event, which is properly handled in the
    // connectionClosed method
  }
}
