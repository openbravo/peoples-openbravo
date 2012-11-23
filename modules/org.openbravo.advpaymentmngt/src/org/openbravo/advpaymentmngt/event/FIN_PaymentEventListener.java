package org.openbravo.advpaymentmngt.event;

import java.util.List;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.service.db.DalConnectionProvider;

public class FIN_PaymentEventListener extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      FIN_Payment.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onDelete(@Observes
  EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    FIN_Payment pay = OBDal.getInstance().get(FIN_Payment.class, event.getTargetInstance().getId());
    List<FIN_PaymentDetail> pdList = pay.getFINPaymentDetailList();
    if (pdList.size() > 0) {
      String language = OBContext.getOBContext().getLanguage().getLanguage();
      ConnectionProvider conn = new DalConnectionProvider(false);
      throw new OBException(Utility.messageBD(conn, "APRM_PaymentCanNotBeDeleted", language));
    }
  }
}
