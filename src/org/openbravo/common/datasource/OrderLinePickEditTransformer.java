/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.common.datasource;

import java.util.Map;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.util.Check;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.Product;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;

@ComponentProvider.Qualifier("213FE8C4AC5E4C95AFC09C80D5C3B663")
public class OrderLinePickEditTransformer extends HqlQueryTransformer {

  @Override
  public String transformHqlQuery(String _hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    final String strOrderId = requestParameters.get("@Order.id@");
    final String strBusinessPartnerId = requestParameters.get("@Order.businessPartner@");
    final String strOrderLineId = requestParameters.get("@OrderLine.id@");
    final String strProductId = requestParameters.get("@OrderLine.product@");

    Check.isTrue(IsIDFilter.instance.accept(strOrderId), "Value " + strOrderId
        + " is not a valid id.");
    Check.isTrue(IsIDFilter.instance.accept(strBusinessPartnerId), "Value " + strBusinessPartnerId
        + " is not a valid id.");
    Check.isTrue(IsIDFilter.instance.accept(strOrderLineId), "Value " + strOrderLineId
        + " is not a valid id.");
    Check.isTrue(IsIDFilter.instance.accept(strProductId), "Value " + strProductId
        + " is not a valid id.");

    final Product product = OBDal.getInstance().get(Product.class, strProductId);
    String includedCategories = product.getIncludedProductCategories();
    String includedProducts = product.getIncludedProducts();

    StringBuffer whereClause = new StringBuffer();

    whereClause.append(" o.salesTransaction = true ");
    whereClause.append(" and (o.processed = true or o.id = :orderId) ");
    whereClause.append(" and o.businessPartner.id = :businessPartnerId ");
    whereClause.append(" and e.id <> :orderLineId ");
    whereClause.append(" and e.product.productType <> 'S' ");

    if ("N".equals(includedCategories)) {
      whereClause
          .append(" and exists ( select 1 from ServiceProductCategory spc where spc.productCategory = e.product.productCategory and spc.product.id = :productId) ");
    } else if ("Y".equals(includedCategories)) {
      whereClause
          .append(" and not exists ( select 1 from ServiceProductCategory spc where spc.productCategory = e.product.productCategory and spc.product.id = :productId) ");
    }
    if ("N".equals(includedProducts)) {
      whereClause
          .append(" and exists (select 1 from ServiceProduct p where p.relatedProduct.id = e.product.id and p.product.id = :productId) ");
    } else if ("Y".equals(includedProducts)) {
      whereClause
          .append(" and not exists (select 1 from ServiceProduct p where p.relatedProduct.id = e.product.id and p.product.id = :productId) ");
    }

    queryNamedParameters.put("orderId", strOrderId);
    queryNamedParameters.put("businessPartnerId", strBusinessPartnerId);
    queryNamedParameters.put("orderLineId", strOrderLineId);
    queryNamedParameters.put("productId", strProductId);

    String transformedHql = _hqlQuery.replace("@whereClause@", whereClause.toString());
    // Replace filter clause, not working automatically due to a bug
    transformedHql = transformedHql.replace("@Order.id@ ", "'" + strOrderId + "' ");

    return transformedHql;
  }
}
