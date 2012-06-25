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

public class CloseCashPayments extends ProcessHQLQuery {

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    return "select p.id as id, p.searchKey as _id, p.commercialName as name, f.currentBalance as expected from OBPOS_App_Payment as p, FIN_Financial_Account as f "
        + "where p.financialAccount=f and p.obposApplications.id = :pos order by p.commercialName";
  }
}
