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

import java.util.List;

import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;

public interface FIN_MatchingCandidatesAlgorithm_I {
  public List<FIN_CandidateRecord> getTransactionCandidates(
      FIN_BankStatementLine _bankStatementLine, List<FIN_FinaccTransaction> excluded);

  public List<FIN_CandidateRecord> getPaymentCandidates(FIN_BankStatementLine _bankStatementLine,
      List<FIN_Payment> excluded);

  public List<FIN_CandidateRecord> getInvoiceCandidates(FIN_BankStatementLine _bankStatementLine,
      List<Invoice> excluded);

  public List<FIN_CandidateRecord> getOrderCandidates(FIN_BankStatementLine _bankStatementLine,
      List<Order> excluded);
}
