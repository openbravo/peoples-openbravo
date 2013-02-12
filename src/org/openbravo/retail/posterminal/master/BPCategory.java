/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
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
import org.openbravo.mobile.core.process.ProcessHQLQuery;

public class BPCategory extends ProcessHQLQuery {

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    List<String> hqlQueries = new ArrayList<String>();

    // standard product categories
    hqlQueries
        .add("select bpcat.id as id, bpcat.searchKey as searchKey,bpcat.name as name, bpcat.name as _identifier "
            + "from BusinessPartnerCategory bpcat "
            + "where "
            + "(bpcat.$incrementalUpdateCriteria) AND (bpcat.$incrementalUpdateCriteria)) order by bpcat.name");

    return hqlQueries;
  }
}