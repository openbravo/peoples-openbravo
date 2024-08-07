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
          msg.getExpirationdate());
    }).collect(Collectors.toList());
  }
}
