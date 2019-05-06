/*
 ************************************************************************************
 * Copyright (C) 2015-2019 Openbravo S.L.U.
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
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.HQLCriteriaProcess;
import org.openbravo.model.common.plm.Product;

@ApplicationScoped
@Qualifier("Services_Filter_Multi")
public class ServicesMultiselectionHQLCriteria extends HQLCriteriaProcess {

  @Override
  public String getHQLFilter(String params) {
    try {
      String filterResult;
      JSONArray paramsArray = new JSONArray(params);
      BigDecimal totalAmountSelected = new BigDecimal(paramsArray.getString(5));
      BigDecimal minimumSelected = new BigDecimal(paramsArray.getString(6));
      BigDecimal maximumSelected = new BigDecimal(paramsArray.getString(7));
      filterResult = " (product.productType = 'S' and product.linkedToProduct = 'Y' and product.obposIsmultiselectable = 'Y' and "
          + "((product.includedProducts = 'Y' and not exists (select 1 from ServiceProduct sp where product.id = sp.product.id and sp.relatedProduct.id in ('$1') )) "
          + "or (product.includedProducts = 'N' and $3 = (select count(*) from ServiceProduct sp where product.id = sp.product.id and sp.relatedProduct.id in ('$1') )) "
          + "or product.includedProducts is null) "
          + "and ((product.includedProductCategories = 'Y' and not exists (select 1 from ServiceProductCategory spc where product.id = spc.product.id and spc.productCategory.id in ('$2') )) "
          + "or (product.includedProductCategories = 'N' and $4 = (select count(*) from ServiceProductCategory spc where product.id = spc.product.id and spc.productCategory.id in ('$2') )) "
          + "or product.includedProductCategories is null) "
          + "and product.id not in ('$5') and (product.ispricerulebased = 'N' or (";
      JSONArray productArray = paramsArray.getJSONArray(0);
      boolean includeAnd = false;
      for (int i = 0; i < productArray.length(); i++) {
        Product product = OBDal.getInstance().get(Product.class, productArray.get(i));
        if (includeAnd) {
          filterResult += " and ";
        } else {
          includeAnd = true;
        }
        filterResult += "exists" //
            + "(select 1" //
            + "     from ServicePriceRuleVersion sprv" //
            + "     where sprv.product.id = product.id" //
            + "     and sprv.validFromDate =" //
            + "             (select max(sprv2.validFromDate)" //
            + "             from ServicePriceRuleVersion sprv2" //
            + "             left join sprv2.relatedProduct rp" //
            + "             left join sprv2.relatedProductCategory rpc" //
            + "             where sprv2.product.id = product.id" //
            + "             and sprv2.validFromDate <= now()" //
            + "             and sprv2.active = true"
            + "             and ((product.includedProducts = 'Y' and product.includedProductCategories = 'Y') " //
            + "                  OR (product.includedProducts = 'Y' and  product.includedProductCategories is null) " //
            + "                  OR (product.includedProducts is null and  product.includedProductCategories = 'Y') " //
            + "                  OR (product.includedProducts = 'N' and (rp is null OR rp.relatedProduct.id = '"
            + product.getId()
            + "'))               OR (product.includedProductCategories = 'N' and (rpc is null OR rpc.productCategory.id = '"
            + product.getProductCategory().getId() + "'))))" //
            + "     and ((product.quantityRule = 'UQ'" //
            + "             and" //
            + "                  (sprv.obposMinimum is null" //
            + "                  or sprv.obposMinimum <= " + totalAmountSelected + ")"//
            + "             and" //
            + "            (sprv.obposMaximum is null" //
            + "                  or sprv.obposMaximum >= " + totalAmountSelected + "))" //
            + "             or" + "           (product.quantityRule = 'PP'" //
            + "             and" //
            + "                  (sprv.obposMinimum is null" //
            + "                  or sprv.obposMinimum <= " + minimumSelected + ")"//
            + "             and" //
            + "            (sprv.obposMaximum is null" //
            + "                  or sprv.obposMaximum >= " + maximumSelected + ")))" //
            + "      and sprv.active = true)";
      }
      filterResult += ")))"; //
      return filterResult;
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }
}
