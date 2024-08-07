package org.openbravo.client.application.messageclient;

import java.util.List;

public interface MessageRegistryPersistence {
  void persistMessage(MessageClientMsg messageClientMsg);
  List<MessageClientMsg> getPendingMessages();
}
