package org.openbravo.client.application.messageclient;

import javax.inject.Inject;
import java.util.List;

public class MessageRegistry {

  @Inject
  MessageRegistryPersistence messageRegistryPersistence;

  public void sendMessage(MessageClientMsg message) {
    messageRegistryPersistence.persistMessage(message);
  }

  public List<MessageClientMsg> getPendingMessages() {
    return messageRegistryPersistence.getPendingMessages();
  }
}
