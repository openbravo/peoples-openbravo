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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.advpaymentmngt.buildvalidation;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.database.ConnectionProvider;

public class ZeroPaymentamtDepositAmtTransactions extends BuildValidation {

  @Override
  public List<String> execute() {
    ConnectionProvider cp = getConnectionProvider();
    ArrayList<String> errors = new ArrayList<String>();
    try {
      // Prevent error when upgrading from a pure 2.50
      if (ZeroPaymentamtDepositAmtTransactionsData.existAPRMbasetables(cp)) {

        ZeroPaymentamtDepositAmtTransactionsData[] listofTransactions = ZeroPaymentamtDepositAmtTransactionsData
            .selectZeroPaymentamtDepositamtTransactions(cp);
        if (listofTransactions != null && listofTransactions.length > 0) {
          String message = "You cannot apply this Advanced Payables and Receivables Management module version because your instance fails in a pre-validation. "
              + "It is not allowed to upgrade to this version having transactions with Payment Amount and Deposit Amount of ZERO. "
              + "To fix this problem in your instance, have a look to generated alerts (Transactions with Deposit Amount and Payment Amount of Zero) and identify the affected transactions. "
              + "If you have a transaction like that, fix the problem updating Payment Amount or Deposit Amount to an Amount different to ZERO. ";
          errors.add(message);
        }

        for (ZeroPaymentamtDepositAmtTransactionsData transaction : listofTransactions) {
          processAlert(cp, transaction);
        }

      }
    } catch (Exception e) {
      return handleError(e);
    }
    return errors;
  }

  private void processAlert(ConnectionProvider cp, ZeroPaymentamtDepositAmtTransactionsData transaction)
      throws Exception {
    final String TRANSACTION_TAB = "23691259D1BD4496BCC5F32645BCA4B9";
    final String FINANCIAL_ACCOUNT_WINDOW = "94EAA455D2644E04AB25D93BE5157B6D";

    String ALERT_RULE_NAME = "Transactions with Deposit Amount and Payment Amount of Zero";
    String alertDescription = "Transaction has Deposit Amount and Payment Amount of Zero, at least one of them has to be different to Zero. "
        + "Update Payment Amount or Deposit Amount to an Amount different to Zero";
    
    String alertRuleId = "";

    String ALERT_RULE_SQL = "SELECT distinct fin_finacc_transaction_id as referencekey_id, "
        + " ad_column_identifier('fin_finacc_transaction', fin_finacc_transaction_id, 'en_US') as record_id, 0 as ad_role_id, null as ad_user_id,"
        + " '"
        + alertDescription
        + "' as description,"
        + " 'Y' as isActive, ad_org_id, ad_client_id,"
        + " now() as created, 0 as createdBy, now() as updated, 0 as updatedBy"
        + " FROM fin_finacc_transaction"
        + " WHERE depositamt = 0"
        + " AND paymentamt = 0";

    // Check if exists the alert rule
    if (!ZeroPaymentamtDepositAmtTransactionsData.existsAlertRule(cp, ALERT_RULE_NAME, transaction.adClientId)) {
      ZeroPaymentamtDepositAmtTransactionsData.insertAlertRule(cp, transaction.adClientId, transaction.adOrgId,
          ALERT_RULE_NAME, TRANSACTION_TAB, ALERT_RULE_SQL);

      alertRuleId = ZeroPaymentamtDepositAmtTransactionsData.getAlertRuleId(cp, ALERT_RULE_NAME,
          transaction.adClientId);
      ZeroPaymentamtDepositAmtTransactionsData[] roles = ZeroPaymentamtDepositAmtTransactionsData.getRoleId(cp,
          FINANCIAL_ACCOUNT_WINDOW, transaction.adClientId);
      for (ZeroPaymentamtDepositAmtTransactionsData role : roles) {
        ZeroPaymentamtDepositAmtTransactionsData.insertAlertRecipient(cp, transaction.adClientId,
            transaction.adOrgId, alertRuleId, role.adRoleId);
      }
    } else {
      alertRuleId = ZeroPaymentamtDepositAmtTransactionsData.getAlertRuleId(cp, ALERT_RULE_NAME,
          transaction.adClientId);
    }

    // Check if exist the concrete alert for the payment
    if (!ZeroPaymentamtDepositAmtTransactionsData.existsAlert(cp, alertRuleId, transaction.finFinaccTransactionId)) {
      ZeroPaymentamtDepositAmtTransactionsData.insertAlert(cp, transaction.adClientId, alertDescription,
          alertRuleId, transaction.documentno, transaction.finFinaccTransactionId);
    }

  }

}
