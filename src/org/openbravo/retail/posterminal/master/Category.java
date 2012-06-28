/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Category extends ProcessHQLQuery {

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    return "select c.id as id, c.searchKey as searchKey, c.name as name, c.name as _identifier, "
        + "img.bindaryData as obposImage "
        + "from ProductCategory as c left outer join c.obposImage img "
        + "where c.$readableCriteria and c.oBPOSIsCatalog = true "
        + "order by c.oBPOSPOSLine, c.name";
  }
}
