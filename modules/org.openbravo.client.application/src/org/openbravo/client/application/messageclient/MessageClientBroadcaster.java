package org.openbravo.client.application.messageclient;

import java.util.List;

/**
 * Broadcasts messages to a list of MessageClient recipients
 */
public class MessageClientBroadcaster {
  public static void send(MessageClientMsg message, List<MessageClient> recipients) {
    recipients.forEach(recipient -> recipient.sendMessage(message.getPayload()));
  }
}
