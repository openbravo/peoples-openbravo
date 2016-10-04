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
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.event;

import java.math.BigDecimal;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.erpCommon.businessUtility.CancelAndReplaceUtils;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

public class FINPaymentEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      FIN_Payment.ENTITY_NAME) };
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
    FIN_Payment payment = (FIN_Payment) event.getTargetInstance();
    final Entity paymentEntity = ModelProvider.getInstance().getEntity(FIN_Payment.ENTITY_NAME);
    final Property paymentAmountProperty = paymentEntity.getProperty(FIN_Payment.PROPERTY_AMOUNT);
    BigDecimal oldPaymentAmount = (BigDecimal) event.getPreviousState(paymentAmountProperty);
    String documentNo = payment.getDocumentNo();
    int documentNoLength = payment.getDocumentNo().length();
    if (payment.getAmount().compareTo(BigDecimal.ZERO) == 0) {
      // Payment has no already an *Z* at the end of the document number
      if (!CancelAndReplaceUtils.ZERO_PAYMENT_SUFIX.equals(documentNo
          .substring(documentNoLength - 3))) {
        String newDocumentNo = documentNo + CancelAndReplaceUtils.ZERO_PAYMENT_SUFIX;
        setDocumentNoToPayment(payment, event, newDocumentNo);
      }
    } else if (oldPaymentAmount.compareTo(BigDecimal.ZERO) == 0) {
      if (CancelAndReplaceUtils.ZERO_PAYMENT_SUFIX.equals(documentNo
          .substring(documentNoLength - 3))) {
        String newDocumentNo = documentNo.substring(0, documentNoLength - 3);
        setDocumentNoToPayment(payment, event, newDocumentNo);
      }
    }
  }

  public void onSave(@Observes
  EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    FIN_Payment payment = (FIN_Payment) event.getTargetInstance();
    final Entity paymentEntity = ModelProvider.getInstance().getEntity(FIN_Payment.ENTITY_NAME);
    final Property processedProperty = paymentEntity.getProperty(FIN_Payment.PROPERTY_PROCESSED);
    if (payment.getAmount().compareTo(BigDecimal.ZERO) == 0) {
      String newDocumentNo = payment.getDocumentNo();
      boolean processed = false;
      Object oProcessed = (processedProperty == null ? false : event
          .getCurrentState(processedProperty));
      if (oProcessed instanceof String) {
        processed = "Y".equals(oProcessed.toString());
      } else if (oProcessed instanceof Boolean) {
        processed = (Boolean) oProcessed;
      }
      if (newDocumentNo.startsWith("<") && newDocumentNo.endsWith(">") && !processed) {
        // Remove "<" and ">" characters from documentNo if payment is not processed
        newDocumentNo = newDocumentNo.substring(1, newDocumentNo.length() - 1);
      }
      newDocumentNo = newDocumentNo + CancelAndReplaceUtils.ZERO_PAYMENT_SUFIX;
      setDocumentNoToPayment(payment, event, newDocumentNo);
    }
  }

  public void onDelete(@Observes
  EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
  }

  private void setDocumentNoToPayment(FIN_Payment payment, EntityPersistenceEvent event,
      String newDocumentNo) {
    String truncatedDocumentNo = (newDocumentNo.length() > 30) ? newDocumentNo.substring(0, 30)
        : newDocumentNo.toString();
    final Entity paymentEntity = ModelProvider.getInstance().getEntity(FIN_Payment.ENTITY_NAME);
    final Property paymentDocumentNoProperty = paymentEntity
        .getProperty(FIN_Payment.PROPERTY_DOCUMENTNO);
    event.setCurrentState(paymentDocumentNoProperty, truncatedDocumentNo);
  }
}