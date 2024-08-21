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
package org.openbravo.client.application.messageclient.alertexample;

import org.openbravo.client.application.messageclient.MessageClient;
import org.openbravo.client.application.messageclient.MessageClientMsg;
import org.openbravo.client.application.messageclient.MessageHandler;

/**
 * Implements an example of a message handler for MessageClientMsg type "example"
 */
@MessageHandler.Qualifier("example")
public class ExampleMessageHandler extends MessageHandler {
  @Override
  protected boolean isValidRecipient(MessageClientMsg messageClientMsg,
      MessageClient messageClient) {
    // TODO: Implement a real case instead of returning always true
    return true;
  }

  @Override
  protected String handleReceivedMessage(String message, MessageClient messageClient) {
    if ("ping".equals(message)) {
      return "pong";
    }

    return null;
  }

  @Override
  public boolean isAllowedToSubscribeToTopic(MessageClient messageClient) {
    // TODO: Implement a real case instead of returning always true
    return true;
  }
}
