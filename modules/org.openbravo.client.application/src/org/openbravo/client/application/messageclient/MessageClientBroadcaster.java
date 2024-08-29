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

import org.codehaus.jettison.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Broadcasts messages to a list of MessageClient recipients
 */
public class MessageClientBroadcaster {
  static void send(MessageClientMsg message, List<MessageClient> recipients) {
    recipients.forEach(recipient -> {
      recipient.sendMessage(getMessageToBeSent(message));
    });
  }

  private static String getMessageToBeSent(MessageClientMsg messageClientMsg) {
    String payload = messageClientMsg.getPayload();
    String type = messageClientMsg.getTopic();

    JSONObject jsonMessage = new JSONObject(Map.of("data", payload, "topic", type));
    return jsonMessage.toString();
  }
}
