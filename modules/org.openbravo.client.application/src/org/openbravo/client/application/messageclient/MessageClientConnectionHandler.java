package org.openbravo.client.application.messageclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageClientConnectionHandler {
  private static final Logger log = LogManager.getLogger();
  static void connectionEstablished(MessageClient messageClient) {
    MessageClientRegistry.getInstance().registerClient(messageClient);
  }

  static void connectionClosed(String searchKey) {
    MessageClientRegistry.getInstance().removeClient(searchKey);
  }

  static void handleMessage(String message, String messageClientSearchKey) {
//    if ("ping".equals(message) || "\"ping\"".equals(message)) {
//      session.getAsyncRemote().sendText("pong");
//    }
    // TODO: Maybe implement how messages are handled by the infra, responses and so on.
    log.info(String.format("[MSG_CLIENT] Message received from client %s. Message: %s", messageClientSearchKey, message));
  }

  static void handleError(Throwable e) {
    // TODO: To be implemented
    log.error("Received error: ", e);
  }
}
