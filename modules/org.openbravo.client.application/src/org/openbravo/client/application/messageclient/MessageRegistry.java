package org.openbravo.client.application.messageclient;

import org.openbravo.base.weld.WeldUtils;

import javax.inject.Inject;
import java.util.List;

public class MessageRegistry {

  static MessageRegistry instance;

  @Inject
  MessageRegistryPersistence messageRegistryPersistence;

  MessageRegistry(){
  }

  public static MessageRegistry getInstance() {
    if (instance == null) {
      instance = WeldUtils.getInstanceFromStaticBeanManager(MessageRegistry.class);
    }

    return instance;
  }

  public void sendMessage(MessageClientMsg message) {
    messageRegistryPersistence.persistMessage(message);
  }

  public List<MessageClientMsg> getPendingMessages() {
    return messageRegistryPersistence.getPendingMessages();
  }
}
