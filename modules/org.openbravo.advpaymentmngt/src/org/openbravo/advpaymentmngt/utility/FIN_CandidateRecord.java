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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

public class FIN_CandidateRecord {
  private BaseOBObject baseOBObject;
  private FIN_FinaccTransaction transaction;
  private FIN_Payment payment;
  private Invoice invoice;
  private Order order;
  private String affinity;

  public FIN_CandidateRecord(final BaseOBObject baseOBObject) {
    this.baseOBObject = baseOBObject;
    if (baseOBObject instanceof FIN_FinaccTransaction) {
      this.transaction = (FIN_FinaccTransaction) baseOBObject;
    } else if (baseOBObject instanceof FIN_Payment) {
      this.payment = (FIN_Payment) baseOBObject;
    } else if (baseOBObject instanceof Invoice) {
      this.invoice = (Invoice) baseOBObject;
    } else if (baseOBObject instanceof Order) {
      this.order = (Order) baseOBObject;
    } else {
      throw new OBException(
          "The FIN_CandidateRecord only accepts instances of FIN_FinaccTransaction/FIN_Payment/Invoice/Order, and the this object is instance of "
              + baseOBObject.getClass().toString());
    }
    this.affinity = calculateAffinity(getBusinessPartner(), getDate(), getAmount());
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

  public BusinessPartner getBusinessPartner() {
    try {
      return (BusinessPartner) (baseOBObject.getClass()).getMethod("getBusinessPartner",
          new Class[0]).invoke(baseOBObject, new Object[0]);
    } catch (Exception e) {
      throw new OBException("Error while getting the FIN_CandidateRecord business partner");
    }
  }

  public Date getDate() {
    if (baseOBObject instanceof FIN_FinaccTransaction) {
      return transaction.getDateAcct();
    } else if (baseOBObject instanceof FIN_Payment) {
      return payment.getPaymentDate();
    } else if (baseOBObject instanceof Invoice) {
      return invoice.getInvoiceDate();
    } else if (baseOBObject instanceof Order) {
      return order.getOrderDate();
    } else {
      throw new OBException(
          "The FIN_CandidateRecord only accepts instances of FIN_FinaccTransaction/FIN_Payment/Invoice/Order, and the this object is instance of "
              + baseOBObject.getClass().toString());
    }
  }

  public BigDecimal getAmount() {
    if (baseOBObject instanceof FIN_FinaccTransaction) {
      return transaction.getPaymentAmount();
    } else if (baseOBObject instanceof FIN_Payment) {
      return payment.getAmount();
    } else if (baseOBObject instanceof Invoice) {
      return invoice.getGrandTotalAmount();
    } else if (baseOBObject instanceof Order) {
      return order.getGrandTotalAmount();
    } else {
      throw new OBException(
          "The FIN_CandidateRecord only accepts instances of FIN_FinaccTransaction/FIN_Payment/Invoice/Order, and the this object is instance of "
              + baseOBObject.getClass().toString());
    }
  }

  public String calculateAffinity(final BusinessPartner bpBankLine, final Date dateBankLine,
      final BigDecimal amountBankLine) {
    return calculateAffinity(bpBankLine, getBusinessPartner(), dateBankLine, getDate(),
        amountBankLine, getAmount());
  }

  public static String calculateAffinity(final BusinessPartner bpBankLine,
      final BusinessPartner bpRecord, final Date dateBankLine, final Date dateRecord,
      final BigDecimal amountBankLine, final BigDecimal amountRecord) {
    int i = 0;
    if (bpBankLine != null && bpBankLine.getId().equals(bpRecord.getId())) {
      i++;
    }
    if (dateBankLine != null && dateBankLine.equals(dateRecord)) {
      i++;
    }
    if (amountBankLine != null && amountBankLine.compareTo(amountRecord) == 0) {
      i++;
    }

    // TODO define a better algorithm
    return "TODO";
  }

  public Map<String, Object> toMap() {
    // TODO other columns
    final Map<String, Object> map = new HashMap<String, Object>();

    map.put("id", baseOBObject.getId());
    map.put("ad_client_id", "23C59575B9CF467C9620760EB255B389");
    map.put("ad_org_id", "B843C30461EA4501935CB1D125C9C25A");
    map.put("createdby", "100");
    map.put("updatedby", "100");
    map.put("created", new Date());
    map.put("updated", new Date());
    map.put("isactive", "Y");
    // map.put("affinity", affinity);
    map.put("affinity", "dummy3");
    map.put("date", getDate());
    map.put("c_bpartner_id", getBusinessPartner().getId());
    map.put("amount", getAmount());
    map.put("date", new Date());

    return map;
  }
}
