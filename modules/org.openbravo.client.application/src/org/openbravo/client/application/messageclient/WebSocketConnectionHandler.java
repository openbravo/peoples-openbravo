package org.openbravo.client.application.messageclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.client.application.WebSocketCustomConfigurator;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/websocket", configurator = WebSocketCustomConfigurator.class)
public class WebSocketConnectionHandler {
  private static final Logger log = LogManager.getLogger();

  @OnOpen
  public void onOpen(javax.websocket.Session session) {
    log.info("Websocket - Connection accepted. Session: " + session.getId());
    String sessionId = (String) session.getUserProperties().get("sessionId");
    String userId = (String) session.getUserProperties().get("ad_user_id");

    WebSocketClient webSocketClient = new WebSocketClient(sessionId, null, userId, session);
    MessageClientConnectionHandler.connectionEstablished(webSocketClient);
  }

  @OnClose
  public void onClose(javax.websocket.Session session) {
    log.info("Websocket - Connection terminated. Session: " + session.getId());
    String sessionId = (String) session.getUserProperties().get("sessionId");

    MessageClientConnectionHandler.connectionClosed(sessionId);
  }

  @OnMessage
  public void onMessage(String message, javax.websocket.Session session) {
    String sessionId = (String) session.getUserProperties().get("sessionId");

    // TODO: Remove top handling
    MessageClientConnectionHandler.handleMessage(message, sessionId);
  }

  @OnError
  public void onError(javax.websocket.Session session, Throwable t) {
    if (t instanceof IOException) {
      // Ignore IOExceptions in case of broken pipe from client side
    } else {
      MessageClientConnectionHandler.handleError(t);
    }
  }
}
