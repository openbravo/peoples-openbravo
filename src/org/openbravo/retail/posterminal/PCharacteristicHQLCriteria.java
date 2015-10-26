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
@Qualifier("PCH_Filter")
public class PCharacteristicHQLCriteria extends HQLCriteriaProcess {

  @Override
  public String getHQLFilter(String params) {
    String[] array_params = getParams(params);
    String sql = null;
    if (array_params[1].equals("__all__") && !array_params[0].equals("")) {
      sql = " ch.id in (select pchv.characteristic.id from ProductCharacteristicValue as pchv where ch.id = pchv.characteristic.id and upper(pchv.product.name) like upper('$1')) ";
    } else if (array_params[0].equals("")) {
      sql = " ch.id in (select pchv.characteristic.id from ProductCharacteristicValue as pchv where ch.id = pchv.characteristic.id and pchv.product.productCategory.id in ('$2')) ";
    } else {
      sql = " ch.id in (select pchv.characteristic.id from ProductCharacteristicValue as pchv where ch.id = pchv.characteristic.id and upper(pchv.product.name) like upper('$1') and pchv.product.productCategory.id in ( '$2') ) ";
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
}
