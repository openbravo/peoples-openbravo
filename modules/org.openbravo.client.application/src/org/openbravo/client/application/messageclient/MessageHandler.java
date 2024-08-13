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

import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles to whom a message should be sent, it is expected to be extended from by each
 * MessageHandler by type
 */
public abstract class MessageHandler {
  public List<MessageClient> getRecipients(MessageClientMsg messageClientMsg) {
    List<MessageClient> connectedClients = MessageClientRegistry.getInstance().getAllClients();

    // Filters those connectedClients who already received the message
    List<MessageClient> relevantClients = connectedClients.stream().filter(messageClient -> {
      if (!messageClient.getSubscribedTopics().contains(messageClientMsg.getType())) {
        // Filter non-subscribed-for topics
        return false;
      }
      if (messageClient.getTimestampLastMsgSent() == null) {
        return true;
      }
      return messageClient.getTimestampLastMsgSent().before(messageClientMsg.getCreationDate());
    }).collect(Collectors.toList());

    return getRecipientsByContext(messageClientMsg, relevantClients);
  }

  public abstract boolean isAllowedToSubscribeToTopic(MessageClient messageClient);

  /**
   * Must return the recipients of the messageClientMsg by using the provided context in that same
   * object. It should only check the provided connected message clients, as those are the relevant
   * ones
   * 
   * @param messageClientMsg
   *          Message that also contains the context
   * @param connectedClients
   *          Relevant connected clients, function should filter those
   * @return List of MessageClient that should receive the message
   */
  public abstract List<MessageClient> getRecipientsByContext(MessageClientMsg messageClientMsg,
      List<MessageClient> connectedClients);

  /**
   * Defines the qualifier used to register a message handler type.
   */
  @javax.inject.Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
  public @interface Qualifier {
    String value();
  }

  /**
   * A class used to select the correct message handler type.
   */
  @SuppressWarnings("all")
  public static class Selector extends AnnotationLiteral<MessageHandler.Qualifier>
      implements MessageHandler.Qualifier {
    private static final long serialVersionUID = 1L;

    final String value;

    public Selector(String value) {
      this.value = value;
    }

    @Override
    public String value() {
      return value;
    }
  }
}
