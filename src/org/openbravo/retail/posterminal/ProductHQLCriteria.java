/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.process.HQLCriteriaProcess;

@ApplicationScoped
@Qualifier("Product_Filter")
public class ProductHQLCriteria extends HQLCriteriaProcess {

  @Override
  public String getHQLFilter(String params) {
    String hql = "1=0";
    try {
      JSONArray paramsArray = new JSONArray(params);
      if (paramsArray.length() > 0) {
        for (int i = 0; i < paramsArray.length(); i++) {
          Object value = paramsArray.get(i);
          if (value instanceof JSONObject) {
            JSONObject json = (JSONObject) value;
            hql += " or upper(" + json.getString("name") + ")";
            hql += " $OP" + (i + 1) + " ";
            hql += "$" + (i + 1) + " ";
          }
        }
      }
    } catch (JSONException e) {

    }
    return hql;
  }
}
