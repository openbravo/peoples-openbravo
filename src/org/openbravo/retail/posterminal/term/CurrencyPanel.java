/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class CurrencyPanel extends QueryTerminalProperty {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    return Arrays.asList(new String[] { "select lineNo as lineNo, currency.id as currency, "
        + "backcolor as backcolor, bordercolor as bordercolor, amount as amount from OBPOS_CurrencyPanel e "
        + "where (e.$incrementalUpdateCriteria) and $readableSimpleClientCriteria "
        + "and $activeCriteria order by lineNo asc" });
  }

  @Override
  public String getProperty() {
    return "currencyPanel";
  }

  @Override
  public boolean returnList() {
    return false;
  }
}
