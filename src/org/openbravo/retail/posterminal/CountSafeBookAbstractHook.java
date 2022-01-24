/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;

public abstract class CountSafeBookAbstractHook implements CountSafeboxHook {

  protected FIN_FinaccTransaction paymentTransaction;
  protected FIN_FinaccTransaction depositTransaction;

  public FIN_FinaccTransaction getPaymentTransaction() {
    return paymentTransaction;
  }

  public void setPaymentTransaction(FIN_FinaccTransaction paymentTransaction) {
    this.paymentTransaction = paymentTransaction;
  }

  public FIN_FinaccTransaction getDepositTransaction() {
    return depositTransaction;
  }

  public void setDepositTransaction(FIN_FinaccTransaction depositTransaction) {
    this.depositTransaction = depositTransaction;
  }

}
