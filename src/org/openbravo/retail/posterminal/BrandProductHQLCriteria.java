/*
 ************************************************************************************
 * Copyright (C) 2015-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONTokener;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.process.HQLCriteriaProcess;

@ApplicationScoped
@Qualifier("PBrand_Filter")
public class BrandProductHQLCriteria extends HQLCriteriaProcess {

  @Override
  public String getHQLFilter(String params) {
    String[] array_params = getParams(params);
    String sql = null;
    if (array_params[1].equals("__all__")) {
      sql = getAllQuery(array_params[0]);
    } else if (array_params[1].equals("OBPOS_bestsellercategory")) {
      sql = getBestsellers(array_params[0]);
    } else {
      sql = getProdCategoryQuery(array_params[0]);
    }
    if (array_params.length >= 2 && !array_params[2].equals("")) {
      sql = " (brand.id in ('" + getIds(array_params, 2) + "') or " + sql + ")";
    }
    return sql;
  }

  private String[] getParams(String params) {
    try {
      JSONArray array = new JSONArray(new JSONTokener(params));
      String[] array_params = new String[array.length()];
      for (int i = 0; i < array.length(); i++) {
        array_params[i] = array.getString(i);
      }
      return array_params;
    } catch (JSONException e) {
      return new String[] { "%", "__all__" };
    }
  }

  private String getAllQuery(String param) {
    String sql = " exists (select 1 from Product as p where p.brand.id = brand.id ";
    if (!(param.equals("%") || param.equals("%%"))) {
      sql = sql + " and (upper(p.name) like upper('$1') or upper(p.uPCEAN) like upper('$1')) ";
    }
    sql = sql + ")";
    return sql;
  }

  public String getAllQuery() {
    String param = "";
    return getAllQuery(param);
  }

  private String getProdCategoryQuery(String param) {
    String sql = " exists (select 1 from Product as p where p.brand.id = brand.id ";
    if (!(param.equals("%") || param.equals("%%"))) {
      sql = sql + " and (upper(p.name) like upper('$1') or upper(p.uPCEAN) like upper('$1')) ";
    }
    sql = sql + " and p.productCategory.id in ( $2 ))";
    return sql;
  }

  public String getProdCategoryQuery() {
    String param = "";
    return getProdCategoryQuery(param);
  }

  public String getBestsellers(String param) {
    String sql = " exists (select 1 from OBRETCO_Prol_Product pli where pli.product.brand.id = brand.id  and pli.bestseller = true ";
    if (!(param.equals("%") || param.equals("%%"))) {
      sql = sql
          + " and (upper(pli.product.name) like upper('$1') or upper(pli.product.uPCEAN) like upper('$1')) ";
    }
    sql = sql + " )";
    return sql;
  }

  public String getBestsellers() {
    String param = "";
    return getBestsellers(param);
  }

  private String getIds(String[] array, int i) {
    return array[i].replace(",", "','");
  }

}
