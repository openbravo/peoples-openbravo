/*
 ************************************************************************************
 * Copyright (C) 2015-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.math.BigDecimal;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.process.HQLCriteriaProcess;

@ApplicationScoped
@Qualifier("Services_Filter_Multi")
public class ServicesMultiselectionHQLCriteria extends HQLCriteriaProcess {

  public String getHQLFilter(String params) {
    try {
      JSONArray paramsArray = new JSONArray(params);
      BigDecimal totalAmountSelected = new BigDecimal(paramsArray.getLong(5));
      BigDecimal minimumSelected = new BigDecimal(paramsArray.getLong(6));
      BigDecimal maximumSelected = new BigDecimal(paramsArray.getLong(7));
      return " (product.productType = 'S' and product.linkedToProduct = 'Y' and product.obposIsmultiselectable = 'Y' and "
          + "((product.includedProducts = 'Y' and not exists (select 1 from ServiceProduct sp where product.id = sp.product.id and sp.relatedProduct.id in ('$1') )) "
          + "or (product.includedProducts = 'N' and $3 = (select count(*) from ServiceProduct sp where product.id = sp.product.id and sp.relatedProduct.id in ('$1') )) "
          + "or product.includedProducts is null) "
          + "and ((product.includedProductCategories = 'Y' and not exists (select 1 from ServiceProductCategory spc where product.id = spc.product.id and spc.productCategory.id in ('$2') )) "
          + "or (product.includedProductCategories = 'N' and $4 = (select count(*) from ServiceProductCategory spc where product.id = spc.product.id and spc.productCategory.id in ('$2') )) "
          + "or product.includedProductCategories is null) "
          + "and product.id not in ('$5') and (product.ispricerulebased = 'N' or exists" //
          + "(select 1" //
          + "     from ServicePriceRuleVersion sprv" //
          + "     where sprv.product.id = product.id" //
          + "     and sprv.validFromDate =" //
          + "             (select max(sprv2.validFromDate)" //
          + "             from ServicePriceRuleVersion sprv2" //
          + "             where sprv2.product.id = product.id" //
          + "             and sprv2.validFromDate <= now()" //
          + "             and sprv2.active = true)" //
          + "     and"
          + "	          ((product.quantityRule = 'UQ'" //
          + "             and" //
          + "                  (sprv.obposMinimum is null"  //
          + "                  or sprv.obposMinimum <= " + totalAmountSelected + ")"//
          + "             and" //
          + "			       (sprv.obposMaximum is null" //
          + "                  or sprv.obposMaximum >= " + totalAmountSelected + "))" //
          + "             or"
          + "	          (product.quantityRule = 'PP'" //
          + "             and" //
          + "                  (sprv.obposMinimum is null"  //
          + "                  or sprv.obposMinimum <= " + minimumSelected + ")"//
          + "             and" //
          + "			       (sprv.obposMaximum is null" //
          + "                  or sprv.obposMaximum >= " + maximumSelected + ")))" //
          + "      and sprv.active = true))" //
          + ")"; //
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }
}