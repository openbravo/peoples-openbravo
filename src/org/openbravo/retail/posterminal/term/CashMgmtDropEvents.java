/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
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

public class CashMgmtDropEvents extends QueryTerminalProperty {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    return Arrays
        .asList(new String[] { "select c.id as id, c.name as name, c.paymentMethod.id as paymentmethod, 'drop' as type from OBRETCO_CashManagementEvents c "
            + "where  c.$naturalOrgCriteria and c.eventtype like '%OUT%' order by c.name " });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  public String getProperty() {
    return "cashMgmtDropEvents";
  }

  @Override
  public boolean returnList() {
    return false;
  }
}
