package org.openbravo.client.application.messageclient;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.utility.org.openbravo.mode.ad.utility.WebsocketMsg;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class DatabaseMessageRegistryPersistence implements MessageRegistryPersistence {
  @Override
  public void persistMessage(MessageClientMsg messageClientMsg) {
    OBContext.setAdminMode();
    try {
      WebsocketMsg websocketMsg = OBProvider.getInstance().get(WebsocketMsg.class);
      websocketMsg.setNewOBObject(true);
      websocketMsg.setMessage(messageClientMsg.getPayload());
      websocketMsg.setUserContact(OBDal.getInstance().get(User.class, messageClientMsg.getId())); // TODO:
                                                                                                  // Remove
                                                                                                  // this
                                                                                                  // testing
                                                                                                  // code
      OBDal.getInstance().save(websocketMsg);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
    // TODO: Implement
  }

  @Override
  public List<MessageClientMsg> getPendingMessages() {
    // TODO: Implement
    List<WebsocketMsg> websocketMsgs = new ArrayList<>();
    try {
      OBContext.setAdminMode();
      OBCriteria<WebsocketMsg> criteria = OBDal.getInstance().createCriteria(WebsocketMsg.class);
      criteria.setFilterOnActive(true);
      criteria.add(Restrictions.eq(WebsocketMsg.PROPERTY_SENT, false));
      websocketMsgs = criteria.list();
    } finally {
      OBContext.restorePreviousMode();
    }
    return websocketMsgs.stream()
        .map(msg -> new MessageClientMsg(msg.getId(), "type", "context", msg.getMessage(), null))
        .collect(Collectors.toList());
  }
}
