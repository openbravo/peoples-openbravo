/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.process.HQLCriteriaProcess;

@ApplicationScoped
@Qualifier("ProductCH_Filter")
public class ProductCharacteristicHQLCriteria extends HQLCriteriaProcess {

  @Override
  public String getHQLFilter(String params) {
    String[] array_params = getParams(params);
    String sql = null;
    if (array_params[1].equals("__all__")) {
      sql = getAllQuery();
    } else if (array_params[1].equals("Best sellers")) {
      sql = getBestsellers();
    } else {
      sql = getProdCategoryQuery();
    }
    return sql;
  }

  private String[] getParams(String params) {
    String[] array_params = new String[params.length()];
    String[] array = (params.substring(1, params.length() - 1)).split(",");
    for (int i = 0; i < array.length; i++) {
      array_params[i] = array[i].substring(1, array[i].length() - 1);
    }
    return array_params;
  }

  public String getAllQuery() {
    return "   exists (select 1 from ProductCharacteristicValue as pchv where cv.characteristic = pchv.characteristic and  cv.id = pchv.characteristicValue.id "
        + " and upper(pchv.product.name) like upper('$1')) ";
  }

  public String getProdCategoryQuery() {
    return "   exists (select 1 from ProductCharacteristicValue as pchv where cv.characteristic = pchv.characteristic and  cv.id = pchv.characteristicValue.id"
        + " and upper(pchv.product.name) like upper('$1') and pchv.product.productCategory.id in ( '$2') ) ";
  }

  public String getBestsellers() {
    return "  exists (select 1 from ProductCharacteristicValue as pchv , OBRETCO_Prol_Product pli where pchv.product.id=pli.product.id "
        + " and cv.characteristic = pchv.characteristic  and  cv.id = pchv.characteristicValue.id  and pli.bestseller = true "
        + " and upper(pchv.product.name) like upper('$1')) ";
  }

}