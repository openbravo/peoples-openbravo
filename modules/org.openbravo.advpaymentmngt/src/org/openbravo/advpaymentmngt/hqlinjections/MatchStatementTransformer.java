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

package org.openbravo.advpaymentmngt.hqlinjections;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.openbravo.advpaymentmngt.dao.MatchTransactionDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.ConnectionProviderImpl;
import org.openbravo.exception.PoolNotFoundException;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Reconciliation;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;
import org.openbravo.service.db.DalConnectionProvider;

@ComponentProvider.Qualifier("BC21981DCF0846338D631887BEDFE7FA")
public class MatchStatementTransformer extends HqlQueryTransformer {
  final static String RDBMS = new DalConnectionProvider(false).getRDBMS();
  final static String TABLE_ID = "BC21981DCF0846338D631887BEDFE7FA";

  @Override
  public String transformHqlQuery(String _hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    StringBuffer whereClause = getWhereClause(requestParameters);
    // @FIN_Financial_Account.id@
    String transformedHql = _hqlQuery.replace("@whereClause@", whereClause.toString());
    return transformedHql;
  }

  private StringBuffer getWhereClause(Map<String, String> requestParameters) {
    StringBuffer selectClause = new StringBuffer();
    selectClause.append("1 in (1)");
    final String financialAccountId = requestParameters.get("@FIN_Financial_Account.id@");
    if (financialAccountId != null) {
      VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
          OBContext.getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
              .getCurrentOrganization().getId(), OBContext.getOBContext().getRole().getId());

      FIN_Reconciliation reconciliation = TransactionsDao.getLastReconciliation(OBDal.getInstance()
          .get(FIN_FinancialAccount.class, financialAccountId), "N");

      // TODO:
      // * Handle runingReconciliations
      // * Handle mixedLines
      if (reconciliation == null) {
        try {
          // TODO: It doesn't work
          reconciliation = MatchTransactionDao.addNewReconciliation(getConnectionProviderMy(),
              vars, financialAccountId);
        } catch (ServletException e) {
          // TODO: Handle exception
          // e.printStackTrace();
        }
      } else {
        // final String tabId = "";
        // TODO: get the proper tabId
        // updateReconciliation(reconciliation.getId(), financialAccountId, tabId, false);
      }
      if (reconciliation != null) {
        final String reconciliationId = reconciliation.getId();
        List<FIN_BankStatementLine> bankLines = MatchTransactionDao.getMatchingBankStatementLines(
            financialAccountId, reconciliationId, "ALL", "Y");
        System.out.println(bankLines);
      }
    }
    return selectClause;
  }

  private static ConnectionProvider getConnectionProviderMy() {
    try {
      final String propFile = OBConfigFileProvider.getInstance().getFileLocation();
      final ConnectionProvider conn = new ConnectionProviderImpl(propFile + "/Openbravo.properties");
      return conn;
    } catch (PoolNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }
}