/*
 ************************************************************************************
 * Copyright (C) 2015-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.process.HQLCriteriaProcess;

@ApplicationScoped
@Qualifier("ServicePriceRuleVersion_DateFilter")
public class ServicePriceRuleVersionHQLCriteria extends HQLCriteriaProcess {

  @Override
  public String getHQLFilter(String params) {
    String filter = "";
    try {
      JSONArray paramsArray = new JSONArray(params);
      if (paramsArray.length() > 0) {
        filter = " (sprv.validFromDate = (select max(sprv2.validFromDate)" //
            + "             from ServicePriceRuleVersion sprv2" //
            + "             left join sprv2.relatedProduct rp2" //
            + "             left join sprv2.relatedProductCategory rpc2" //
            + "             where sprv2.product = sprv.product" //
            + "             and to_date(sprv2.validFromDate) <= now()" //
            + "             and sprv2.active = true"
            + "             and ((sprv2.product.includedProducts = 'Y' and sprv2.product.includedProductCategories = 'Y') " //
            + "                  OR (sprv2.product.includedProducts = 'Y' and sprv2.product.includedProductCategories is null) " //
            + "                  OR (sprv2.product.includedProducts is null and sprv2.product.includedProductCategories = 'Y') " //
            + "                  OR (sprv2.product.includedProducts = 'N' and (rp2 is null OR rp2.relatedProduct.id = $1)) " //
            + "                  OR (sprv2.product.includedProductCategories = 'N' and (rpc2 is null OR rpc2.productCategory.id = $2)))))" //
            + "         and ((sprv.product.includedProducts = 'Y' and sprv.product.includedProductCategories = 'Y') " //
            + "                  OR (sprv.product.includedProducts = 'Y' and sprv.product.includedProductCategories is null) " //
            + "                  OR (sprv.product.includedProducts is null and sprv.product.includedProductCategories = 'Y') " //
            + "                  OR (sprv.product.includedProducts = 'N' and (rp is null OR rp.relatedProduct.id = $1)) " //
            + "                  OR (sprv.product.includedProductCategories = 'N' and (rpc is null OR rpc.productCategory.id = $2)))";

      } else {
        filter = " (to_date(sprv.validFromDate) <= now())";
      }
      return filter;
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }
}
