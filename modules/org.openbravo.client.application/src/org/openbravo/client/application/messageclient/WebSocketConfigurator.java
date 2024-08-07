package org.openbravo.client.application.messageclient;


import org.openbravo.base.exception.OBException;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Configurator that handles proper authentication on WebSocket handshake request
 */
public class WebSocketConfigurator extends ServerEndpointConfig.Configurator {
  @Override
  public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request,
                              HandshakeResponse response) {
    HttpSession session = (HttpSession) request.getHttpSession();
    if (!validateSession(session)) {
      throw new OBException("WebSocket authentication failed, not authenticated");
    }

    // TODO: To think if it might make sense to store some user properties information, for later
    // use in the WebSocket initialization

    // Add sessionId to the userProperties
    sec.getUserProperties().put("sessionId", session.getId());
    sec.getUserProperties().put("ad_user_id", session.getAttribute("#AD_USER_ID"));
  }

  private boolean validateSession(HttpSession session) {
    return session != null && session.getAttribute("#Authenticated_user") != null;
  }
}
