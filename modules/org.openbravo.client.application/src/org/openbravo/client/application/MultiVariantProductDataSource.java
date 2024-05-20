/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;

/**
 * Datasource used by the Purchase Order view "Model Mode" process TODO: Requires further
 * development
 */
public class MultiVariantProductDataSource extends ReadOnlyDataSourceService {

  @Override
  public void checkFetchDatasourceAccess(Map<String, String> parameter) {
    // Product datasource is accessible by all roles.
    // TODO: Might need rechecking
  }

  @Override
  protected int getCount(Map<String, String> parameters) {
    return getData(parameters, 0, Integer.MAX_VALUE).size();
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    String orderRecordId = parameters.get("recordId");
    Order order = OBDal.getInstance().get(Order.class, orderRecordId);
    List<OrderLine> orderLineList = order.getOrderLineList();

    Set<String> genericProductIds = new HashSet<>();
    List<Map<String, Object>> genericProducts = new ArrayList<>();

    orderLineList.forEach(orderLine -> {
      Product orderLineProduct = orderLine.getProduct();
      if (orderLineProduct.getGenericProduct() != null
          && (orderLineProduct.getGenericProduct().getRowCharacteristic() != null
              || orderLineProduct.getGenericProduct().getColumnCharacteristic() != null)) {
        if (!genericProductIds.contains(orderLineProduct.getGenericProduct().getId())) {
          genericProductIds.add(orderLineProduct.getGenericProduct().getId());
          genericProducts.add( //
              Map.of("product", orderLineProduct.getGenericProduct(), //
                  "quantity", 0, //
                  "listOfItems", (new JSONArray()).put(new JSONArray()).put(new JSONArray()))); //
          // TODO: Include product data, computed using row/col characteristics + variant orderline
          // products + quantities
        }
      }
    });
    return genericProducts;
  }
}
