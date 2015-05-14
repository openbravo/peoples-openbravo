package org.openbravo.event;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.service.db.DalConnectionProvider;

public class AttachmentConfigEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      AttachmentConfig.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes
  EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    isAnyActivated(event);

  }

  public void onSave(@Observes
  EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    isAnyActivated(event);

  }

  public void onDelete(@Observes
  EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    final AttachmentConfig deletedAttachmentConfig = (AttachmentConfig) event.getTargetInstance();
    final String hqlQuery = "FROM OBUIAPP_Parameter AS p where attachmentMethod.id='"
        + deletedAttachmentConfig.getAttachmentMethod().getId() + "'";
    final Query parameterQuery = OBDal.getInstance().getSession().createQuery(hqlQuery);
    parameterQuery.setFetchSize(1000);
    final ScrollableResults attachmentConfigScroller = parameterQuery
        .scroll(ScrollMode.FORWARD_ONLY);
    while (attachmentConfigScroller.next()) {
      String language = OBContext.getOBContext().getLanguage().getLanguage();
      ConnectionProvider conn = new DalConnectionProvider(false);
      throw new OBException(Utility.messageBD(conn,
          OBMessageUtils.getI18NMessage("AD_ExistingMetadata", null), language));
    }
    attachmentConfigScroller.close();

  }

  private void isAnyActivated(EntityPersistenceEvent event) {

    final AttachmentConfig newAttachmentConfig = (AttachmentConfig) event.getTargetInstance();

    final OBQuery<AttachmentConfig> attachmentConfigQuery = OBDal.getInstance().createQuery(
        AttachmentConfig.class, "id!=:id");
    attachmentConfigQuery.setNamedParameter("id", newAttachmentConfig.getId());

    if (!attachmentConfigQuery.list().isEmpty()) {
      if (newAttachmentConfig.isActive()) {
        String language = OBContext.getOBContext().getLanguage().getLanguage();
        ConnectionProvider conn = new DalConnectionProvider(false);
        throw new OBException(Utility.messageBD(conn,
            OBMessageUtils.getI18NMessage("AD_EnabledAttachmentMethod", null), language));
      }
    }

  }

}
