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

public class Product extends ProcessHQLQuery {

  @Override
  protected String getQuery(JSONObject jsonsent) throws JSONException {
    return "select p.id as id, p.name as _identifier, p.taxCategory.id as taxCategory, p.productCategory.id as productCategory, p.obposScale as obposScale, p.uOM.id as uOM, p.uPCEAN as uPCEAN, img.bindaryData as img "
        + "from Product p left outer join p.image img "
        + "where p.$readableClientCriteria and p.$naturalOrgCriteria and p.obposCatalog = true order by p.name";
  }
}
