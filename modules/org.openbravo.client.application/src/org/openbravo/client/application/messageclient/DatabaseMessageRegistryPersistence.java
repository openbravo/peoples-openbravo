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

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.MessagePersisted;
import org.openbravo.model.common.enterprise.Organization;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the persistence of MessageClientMsg objects in the database and their retrieval
 */
@ApplicationScoped
public class DatabaseMessageRegistryPersistence implements MessageRegistryPersistence {
  @Override
  public void persistMessage(MessageClientMsg messageClientMsg) {
    OBContext.setAdminMode();
    try {
      MessagePersisted persistedMessage = OBProvider.getInstance().get(MessagePersisted.class);
      persistedMessage.setNewOBObject(true);
      persistedMessage.setPayload(messageClientMsg.getPayload());
      persistedMessage.setType(messageClientMsg.type);
      persistedMessage.setExpirationdate(messageClientMsg.getExpirationDate());
      messageClientMsg.getContext().forEach((key, value) -> {
        switch (key) {
          case "user":
            persistedMessage.setUserContact(OBDal.getInstance().get(User.class, value));
            break;
          case "role":
            persistedMessage.setRole(OBDal.getInstance().get(Role.class, value));
            break;
          case "organization":
            persistedMessage.setOrganization(OBDal.getInstance().get(Organization.class, value));
            break;
          case "client":
            persistedMessage.setClient(OBDal.getInstance().get(Client.class, value));
            break;
          default:
            // No other context info is supported on the persistence layer
            break;
        }
      });
      OBDal.getInstance().save(persistedMessage);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public List<MessageClientMsg> getPendingMessages() {
    List<MessagePersisted> persistedMsgs = getPendingMessagesPersisted();
    return mapMessagePersistedToMessageClientMsg(persistedMsgs);
  }

  private List<MessagePersisted> getPendingMessagesPersisted() {
    List<MessagePersisted> persistedMsgs = new ArrayList<>();
    try {
      OBContext.setAdminMode();
      OBCriteria<MessagePersisted> criteria = OBDal.getInstance()
          .createCriteria(MessagePersisted.class);
      criteria.setFilterOnReadableOrganization(false);
      criteria.setFilterOnReadableClients(false);
      criteria.setFilterOnActive(true);
      criteria.add(Restrictions.ge(MessagePersisted.PROPERTY_EXPIRATIONDATE, new Date()));
      persistedMsgs = criteria.list();
    } finally {
      OBContext.restorePreviousMode();
    }
    return persistedMsgs;
  }

  private List<MessageClientMsg> mapMessagePersistedToMessageClientMsg(
      List<MessagePersisted> persistedMsgs) {
    return persistedMsgs.stream().map(msg -> {
      Map<String, String> context = new HashMap<>();
      if (msg.getClient() != null) {
        context.put("client", msg.getClient().getId());
      }
      if (msg.getOrganization() != null) {
        context.put("organization", msg.getOrganization().getId());
      }
      if (msg.getUserContact() != null) {
        context.put("user", msg.getUserContact().getId());
      }
      if (msg.getRole() != null) {
        context.put("role", msg.getRole().getId());
      }
      return new MessageClientMsg(msg.getId(), msg.getType(), context, msg.getPayload(),
          msg.getExpirationdate(), msg.getCreationDate());
    }).collect(Collectors.toList());
  }
}
