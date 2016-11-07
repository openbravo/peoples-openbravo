/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class ListReference extends ProcessHQLQuery {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    JSONObject parameters = jsonsent.getJSONObject("parameters");
    Map<String, Object> paramValues = new HashMap<String, Object>();
    paramValues.put("reference", parameters.getJSONObject("reference").getString("value"));
    return paramValues;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    return Arrays.asList(new String[] { "select list.searchKey as id, coalesce("
        + " (select trl.name from list.aDListTrlList trl where  trl.language = '"
        + OBContext.getOBContext().getLanguage().getLanguage()
        + "'), list.name) as name from ADList list " + "where list.reference.id = :reference"
        + " and list.$readableSimpleCriteria and list.$activeCriteria "
        + "order by list.sequenceNumber" });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
