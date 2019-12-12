/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
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

public class DataQualityProvider extends QueryTerminalProperty {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    return Arrays.asList(new String[] {
        "select dataqualitypro as dataQualityProvider from OBPOS_Ad_Org_Dqm where 1=1 and $naturalOrgCriteria and $readableClientCriteria order by priority" });
  }

  @Override
  public String getProperty() {
    return "dataQualityProviders";
  }

  @Override
  public boolean returnList() {
    return false;
  }

}