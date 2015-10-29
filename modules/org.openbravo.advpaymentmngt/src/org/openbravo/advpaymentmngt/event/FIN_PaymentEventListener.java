/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012-2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.event;

import java.util.List;

import javax.enterprise.event.Observes;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.APRMPendingPaymentFromInvoice;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.service.db.DalConnectionProvider;

public class FIN_PaymentEventListener extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      FIN_Payment.ENTITY_NAME) };
  private static AdvPaymentMngtDao dao;

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final FIN_Payment payment = (FIN_Payment) event.getTargetInstance();
    final Entity paymentEntity = ModelProvider.getInstance().getEntity(FIN_Payment.ENTITY_NAME);
    final Property paymentMethodProperty = paymentEntity
        .getProperty(FIN_Payment.PROPERTY_PAYMENTMETHOD);
    final Property paymentStatusProperty = paymentEntity.getProperty(FIN_Payment.PROPERTY_STATUS);
    if (!((FIN_PaymentMethod) event.getCurrentState(paymentMethodProperty))
        .equals((FIN_PaymentMethod) event.getPreviousState(paymentMethodProperty))
        && ((String) event.getCurrentState(paymentStatusProperty)).equals("RPAE")) {

      dao = new AdvPaymentMngtDao();
      PaymentExecutionProcess executionProcess = dao.getExecutionProcess(payment);

      if (executionProcess != null) {
        OBCriteria<APRMPendingPaymentFromInvoice> ppfiCriteria = OBDal.getInstance()
            .createCriteria(APRMPendingPaymentFromInvoice.class);
        ppfiCriteria.add(Restrictions.eq(APRMPendingPaymentFromInvoice.PROPERTY_PAYMENT, payment));
        List<APRMPendingPaymentFromInvoice> ppfiList = ppfiCriteria.list();
        if (!ppfiList.isEmpty()) {
          for (APRMPendingPaymentFromInvoice ppfi : ppfiList) {
            if (!ppfi.getPaymentExecutionProcess().equals(executionProcess)) {
              ppfi.setPaymentExecutionProcess(executionProcess);
              OBDal.getInstance().save(ppfi);
            }
          }
        }
      }
    }

  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    FIN_Payment pay = OBDal.getInstance().get(FIN_Payment.class, event.getTargetInstance().getId());
    List<FIN_PaymentDetail> pdList = pay.getFINPaymentDetailList();
    if (pdList.size() > 0) {
      String language = OBContext.getOBContext().getLanguage().getLanguage();
      ConnectionProvider conn = new DalConnectionProvider(false);
      throw new OBException(Utility.messageBD(conn, "ForeignKeyViolation", language));
    }
  }
}
