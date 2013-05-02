/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.process.ProcessHQLQuery;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;

public class ProductChValue extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);
    List<String> hqlQueries = new ArrayList<String>();

    // standard product categories
    hqlQueries
        .add("select cv.id as id, cv.name as name, cv.characteristic.id as characteristic_id, node.reportSet as parent, cv.name as _identifier "
            + "from CharacteristicValue cv, ADTreeNode node "
            + "where cv.characteristic.tree =  node.tree and cv.id = node.node "
            + "and cv.$naturalOrgCriteria and cv.$readableClientCriteria and (cv.$incrementalUpdateCriteria)");

    return hqlQueries;
  }
}