/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
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
@Qualifier("Ch_Filter")
public class ChHQLCriteria extends HQLCriteriaProcess {
  @Deprecated
  public String getHQLFilter() {
    return null;
  }

  @Override
  public String getHQLFilter(String params) {
    String[] array = (params.substring(2, params.length() - 2)).split(";");
    String hql = "";
    for (int i = 0; i < array.length; i++) {
      if (i == 0) {
        hql = " exists (select 1  from ProductCharacteristicValue as pchv  where ch.id = pchv.characteristic.id "
            + " and  exists (select 1  from ProductCharacteristicValue p  where p.product.id = pchv.product.id and  p.characteristicValue.id in ('"
            + getIds(array, i) + "'))";
      } else {
        hql = hql
            + " and  exists (select 1  from ProductCharacteristicValue p  where p.product.id = pchv.product.id and  p.characteristicValue.id in ('"
            + getIds(array, i) + "'))  ";
      }
      if (i + 1 == array.length) {
        hql = hql + " )";
      }
    }
    return hql;
  }

  private String getIds(String[] array, int i) {
    return array[i].replace(",", "','");
  }
}
