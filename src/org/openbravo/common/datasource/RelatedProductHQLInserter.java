/*
 ************************************************************************************
 * Copyright (C) 2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.common.datasource;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.Product;
import org.openbravo.service.datasource.hql.HQLInserterQualifier;
import org.openbravo.service.datasource.hql.HqlInserter;

@HQLInserterQualifier.Qualifier(tableId = "629F3BB39497465AB4C280B01700A202", injectionId = "0")
/**
 * HQL Inserter for the HQL table M_Related_Product visible from the Related Services and
 * Contributions tab in the Product window. It is used to view products of type Service and
 * Contribution linked to the selected product of type Item either via the ServiceProduct or
 * ServiceProductCategory tables.
 */
public class RelatedProductHQLInserter extends HqlInserter {

  @Override
  public String insertHql(Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    // ensures query won't return results if no parent product is selected
    String whereClause = "1<>1";

    final String relatedProductId = requestParameters.get("@Product.id@");

    if (relatedProductId != null && !StringUtils.equals(relatedProductId, "null")) {
      final Product relatedProduct = OBDal.getInstance().get(Product.class, relatedProductId);
      final String relatedProductCategoryId = relatedProduct.getProductCategory().getId();

      queryNamedParameters.put("relatedProductId", relatedProductId);
      queryNamedParameters.put("relatedProductCategoryId", relatedProductCategoryId);

      whereClause = "(sp.relatedProduct.id = :relatedProductId OR spc.productCategory.id = :relatedProductCategoryId)";
    }
    return whereClause;
  }
}
