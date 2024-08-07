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

import javax.inject.Inject;
import java.util.List;

import org.openbravo.base.weld.WeldUtils;

/**
 * Common registry for Messages that should be sent through the MessageClient infrastructure
 */
public class MessageRegistry {

  static MessageRegistry instance;

  @Inject
  MessageRegistryPersistence messageRegistryPersistence;

  MessageRegistry() {
  }

  public static MessageRegistry getInstance() {
    if (instance == null) {
      instance = WeldUtils.getInstanceFromStaticBeanManager(MessageRegistry.class);
    }

    return instance;
  }

  /**
   * Registers a given message to then be sent to the corresponding MessageClients
   * 
   * @param message
   *          Message to be sent
   */
  public void sendMessage(MessageClientMsg message) {
    messageRegistryPersistence.persistMessage(message);
  }

  /**
   * Returns a list of messages that are pending to be sent, it must exclude expired messages.
   * 
   * @return non-expired messages
   */
  public List<MessageClientMsg> getPendingMessages() {
    return messageRegistryPersistence.getPendingMessages();
  }
}
