/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
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
import org.openbravo.dal.core.OBContext;

public class OrderType extends QueryTerminalProperty {

  @Override
  public String getProperty() {
    return "orderType";
  }

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  public boolean returnList() {
    return false;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    return Arrays.asList(new String[] { "select list.searchKey as id, coalesce("
        + " (select trl.name from list.aDListTrlList trl where  trl.language = '"
        + OBContext.getOBContext().getLanguage().getLanguage()
        + "'), list.name) as name from ADList list "
        + " where list.reference.id = '1CFCF0C9BA35496FBEE9043168AF3662' "
        + " and list.$readableSimpleCriteria and list.$activeCriteria "
        + " order by list.sequenceNumber" });
  }

}