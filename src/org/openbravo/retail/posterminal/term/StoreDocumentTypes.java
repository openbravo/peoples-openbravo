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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;

public class StoreDocumentTypes extends QueryTerminalProperty {

  @Override
  public String getProperty() {
    return "OBRDM_storeDocumentTypes";
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
    return Arrays.asList("select dt.id as id" //
        + " from DocumentType dt where dt.organization.id in :orgIds" //
        + " and dt.return = false and dt.sOSubType <> 'OB'" //
        + " and dt.documentCategory = 'SOO'");
  }

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    Map<String, Object> result = new HashMap<>();
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    Set<String> orgIds = OBContext.getOBContext()
        .getOrganizationStructureProvider()
        .getNaturalTree(orgId);
    result.put("orgIds", orgIds);
    return result;
  }
}
