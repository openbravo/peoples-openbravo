/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.List;

import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;

public abstract class CountSafeBookAbstractHook implements CountSafeboxHook {

  protected List<FIN_FinaccTransaction> paymentTransactions;
  protected List<FIN_FinaccTransaction> depositTransactions;

  public List<FIN_FinaccTransaction> getPaymentTransactions() {
    return paymentTransactions;
  }

  public void setPaymentTransactions(List<FIN_FinaccTransaction> paymentTransactions) {
    this.paymentTransactions = paymentTransactions;
  }

  public List<FIN_FinaccTransaction> getDepositTransactions() {
    return depositTransactions;
  }

  public void setDepositTransactions(List<FIN_FinaccTransaction> depositTransactions) {
    this.depositTransactions = depositTransactions;
  }

}
