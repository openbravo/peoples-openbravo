/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class CashMgmtDepositsDrops extends ProcessHQLQuery {

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    return "select distinct(t.id), t.paymentAmount as drop, t.depositAmount as deposit, p.commercialName as name, t.description as description from FIN_Finacc_Transaction t,"
        + "FIN_Financial_Account as a, OBPOS_App_Payment as p "
        + "where t.account=a and a=p.financialAccount and t.reconciliation is null and t.gLItem=p.glitemChanges";
  }
}
