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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.utility;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

public class FIN_CandidateRecord {
  private BaseOBObject baseOBObject;
  private FIN_FinaccTransaction transaction;
  private FIN_Payment payment;
  private Invoice invoice;
  private Order order;
  private Date date;
  private BusinessPartner businessPartner;
  private BigDecimal amount;
  private String matchedDoc;
  private String matchType = APRMConstants.CANDIDATE_MATCH_TYPE__AUTO;
  private String reference;
  private String affinity;

  public FIN_CandidateRecord(final BaseOBObject baseOBObject,
      final FIN_BankStatementLine bankStatementLine) {
    if (baseOBObject == null || bankStatementLine == null) {
      throw new InvalidParameterException(
          "FIN_CandidateRecord: baseOBObject and bankStatementLine must not be null");
    }

    this.baseOBObject = baseOBObject;
    if (baseOBObject instanceof FIN_FinaccTransaction) {
      this.transaction = (FIN_FinaccTransaction) baseOBObject;
      this.matchedDoc = APRMConstants.CANDIDATE_MATCHED_DOCUMENT__TRANSACTION;
      this.date = transaction.getDateAcct();
      this.amount = transaction.getPaymentAmount();
      this.reference = transaction.getDescription();
    } else if (baseOBObject instanceof FIN_Payment) {
      this.payment = (FIN_Payment) baseOBObject;
      this.matchedDoc = APRMConstants.CANDIDATE_MATCHED_DOCUMENT__PAYMENT;
      this.date = payment.getPaymentDate();
      this.amount = payment.getAmount();
      this.reference = payment.getReferenceNo();
    } else if (baseOBObject instanceof Invoice) {
      this.invoice = (Invoice) baseOBObject;
      this.matchedDoc = APRMConstants.CANDIDATE_MATCHED_DOCUMENT__INVOICE;
      this.date = invoice.getInvoiceDate();
      this.amount = invoice.getGrandTotalAmount();
      this.reference = invoice.getDocumentNo();
    } else if (baseOBObject instanceof Order) {
      this.order = (Order) baseOBObject;
      this.matchedDoc = APRMConstants.CANDIDATE_MATCHED_DOCUMENT__ORDER;
      this.date = order.getOrderDate();
      this.amount = order.getGrandTotalAmount();
      this.reference = order.getDocumentNo();
    } else {
      throw new OBException(
          "The FIN_CandidateRecord only accepts instances of FIN_FinaccTransaction/FIN_Payment/Invoice/Order, and the this object is instance of "
              + baseOBObject.getClass().toString());
    }
    try {
      this.businessPartner = (BusinessPartner) (baseOBObject.getClass()).getMethod(
          "getBusinessPartner", new Class[0]).invoke(baseOBObject, new Object[0]);
    } catch (Exception e) {
      throw new OBException("Error while getting the FIN_CandidateRecord business partner");
    }
    this.affinity = calculateAffinity(bankStatementLine);
  }

  public BaseOBObject getBaseOBObject() {
    return baseOBObject;
  }

  public FIN_FinaccTransaction getTransaction() {
    return transaction;
  }

  public FIN_Payment getPayment() {
    return payment;
  }

  public Invoice getInvoice() {
    return invoice;
  }

  public Order getOrder() {
    return order;
  }

  public String getAffinity() {
    return affinity;
  }

  public String getMatchedDoc() {
    return matchedDoc;
  }

  public String getMatchType() {
    return matchType;
  }

  public BusinessPartner getBusinessPartner() {
    return businessPartner;
  }

  public Date getDate() {
    return date;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getReference() {
    return reference;
  }

  @Override
  public boolean equals(Object other) {
    try {
      return ((FIN_CandidateRecord) other).getBaseOBObject().getId()
          .equals(getBaseOBObject().getId());
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    HashCodeBuilder hcb = new HashCodeBuilder();
    hcb.append(getBaseOBObject().getId());
    return hcb.toHashCode();
  }

  public String toString() {
    return matchedDoc + "-" + baseOBObject.getId();
  }

  public String calculateAffinity(final FIN_BankStatementLine bankStatementLine) {
    final BusinessPartner bankLineBP = bankStatementLine.getBusinessPartner();
    final Date bankLineDate = bankStatementLine.getTransactionDate();
    final BigDecimal bankLineAmt = bankStatementLine.getCramount().subtract(
        bankStatementLine.getDramount());
    final String bankLineReference = bankStatementLine.getReferenceNo();

    int affinityPoints = 0;
    if (bankLineBP != null && StringUtils.equals(bankLineBP.getId(), businessPartner.getId())) {
      affinityPoints++;
    }
    if (bankLineDate != null && bankLineDate.equals(date)) {
      affinityPoints++;
    }
    if (bankLineAmt != null && bankLineAmt.compareTo(amount) == 0) {
      affinityPoints++;
    }
    if (StringUtils.contains(reference, bankLineReference)) {
      affinityPoints++;
    }

    return Integer.toString(affinityPoints);
  }

  public Map<String, Object> toMap() {
    final Map<String, Object> map = new HashMap<String, Object>();
    map.put("_identifier", toString());
    map.put("id", baseOBObject.getId());
    map.put("affinity", getAffinity());
    map.put("date", getDate());
    map.put("businessPartner", getBusinessPartner().getId());
    map.put("businessPartner$_identifier", getBusinessPartner().getIdentifier());
    map.put("amount", getAmount());
    map.put("date", getDate());
    map.put("matchedDoc", getMatchedDoc());
    map.put("matchType", getMatchType());
    map.put("reference", getReference());
    return map;
  }
}
