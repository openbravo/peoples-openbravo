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

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * WebSocket specific connection handler, it triggers the corresponding calls in
 * MessageClientConnectionHandler class.
 */
@ServerEndpoint(value = "/websocket", configurator = WebSocketConfigurator.class)
public class WebSocketConnectionHandler {
  private static final Logger log = LogManager.getLogger();

  @OnOpen
  public void onOpen(javax.websocket.Session session) {
    log.debug("Websocket - Connection accepted. Session: " + session.getId());
    String sessionId = (String) session.getUserProperties().get("sessionId");
    String userId = (String) session.getUserProperties().get("user_id");
    String roleId = (String) session.getUserProperties().get("role_id");
    String orgId = (String) session.getUserProperties().get("org_id");
    String clientId = (String) session.getUserProperties().get("client_id");

    List<String> supportedMessageTypes = session.getRequestParameterMap().get("supportedTopics");

    WebSocketClient webSocketClient = new WebSocketClient(sessionId, clientId, orgId, userId,
        roleId, session);
    webSocketClient.setSubscribedTopics(supportedMessageTypes);
    MessageClientConnectionHandler.connectionEstablished(webSocketClient);
  }

  @OnClose
  public void onClose(javax.websocket.Session session) {
    log.debug("Websocket - Connection terminated. Session: " + session.getId());
    String sessionId = (String) session.getUserProperties().get("sessionId");

    MessageClientConnectionHandler.connectionClosed(sessionId);
  }

  @OnMessage
  public void onMessage(String message, javax.websocket.Session session) {
    String sessionId = (String) session.getUserProperties().get("sessionId");
    String messageToSendBack = MessageClientConnectionHandler.handleMessage(message, sessionId);
    if (messageToSendBack != null) {
      session.getAsyncRemote().sendText(messageToSendBack);
    }
  }

  @OnError
  public void onError(javax.websocket.Session session, Throwable t) {
    MessageClientConnectionHandler.handleError(t);
  }
}
