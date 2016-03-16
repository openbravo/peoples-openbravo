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
@Qualifier("Chv_Filter")
public class ChvHQLCriteria extends HQLCriteriaProcess {
  @Deprecated
  public String getHQLFilter() {
    return null;
  }

  @Override
  public String getHQLFilter(String params) {
    return " exists (select 1  from ProductCharacteristicValue as pchv  where cv.id = pchv.characteristicValue.id  and exists  (select 1 "
        + " from ProductCharacteristicValue p  where p.product.id = pchv.product.id and  p.characteristicValue.id in ($1) ))";
  }
}