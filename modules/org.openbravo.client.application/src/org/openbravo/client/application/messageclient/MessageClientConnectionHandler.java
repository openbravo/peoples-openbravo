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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

  static void handleMessage(String message, String messageClientSearchKey) {
    // TODO: Maybe implement how messages are handled by the infra, responses and so on.
    log.info(String.format("[MSG_CLIENT] Message received from client %s. Message: %s",
        messageClientSearchKey, message));
  }

  static void handleError(Throwable e) {
    // TODO: To be implemented
    log.error("Received error: ", e);
  }
}
