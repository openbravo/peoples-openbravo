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
@Qualifier("Services_Filter_Multi")
public class ServicesMultiselectionHQLCriteria extends HQLCriteriaProcess {

  public String getHQLFilter(String params) {
    return " (product.productType = 'S' and product.linkedToProduct = 'Y' and product.obposIsmultiselectable = 'Y' and "
        + "((product.includedProducts = 'Y' and not exists (select 1 from ServiceProduct sp where product.id = sp.product.id and sp.relatedProduct.id in ('$1') )) "
        + "or (product.includedProducts = 'N' and $3 = (select count(*) from ServiceProduct sp where product.id = sp.product.id and sp.relatedProduct.id in ('$1') )) "
        + "or product.includedProducts is null) "
        + "and ((product.includedProductCategories = 'Y' and not exists (select 1 from ServiceProductCategory spc where product.id = spc.product.id and spc.productCategory.id in ('$2') )) "
        + "or (product.includedProductCategories = 'N' and $4 = (select count(*) from ServiceProductCategory spc where product.id = spc.product.id and spc.productCategory.id in ('$2') )) "
        + "or product.includedProductCategories is null) " + "and product.id not in ('$5'))";
  }
}