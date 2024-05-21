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
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCharacteristic;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;

public class ProductCharacteristicValuesRowColumnDataSource extends ReadOnlyDataSourceService {
  @Override
  protected int getCount(Map<String, String> parameters) {
    return getData(parameters, 0, Integer.MAX_VALUE).size();
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    List<Map<String, Object>> resultList = new ArrayList<>();

    String productId = parameters.get("productId");
    if (productId == null) {
      // TODO: Maybe add some log that the productId must be provided
      return resultList;
    }

    Product product = OBDal.getInstance().get(Product.class, productId);
    String prodRowCharacteristicId = product.getRowCharacteristic() != null
        ? product.getRowCharacteristic().getId()
        : null;
    String prodColumnCharacteristicId = product.getColumnCharacteristic() != null
        ? product.getColumnCharacteristic().getId()
        : null;

    if (prodRowCharacteristicId == null && prodColumnCharacteristicId == null) {
      // TODO: No product characteristics configured
      return resultList;
    }

    List<ProductCharacteristic> productCharacteristics = product.getProductCharacteristicList();
    JSONArray rowCharacteristics = new JSONArray();
    JSONArray columnCharacteristics = new JSONArray();

    for (ProductCharacteristic productCharacteristic : productCharacteristics) {
      if (productCharacteristic.getCharacteristic().getId().equals(prodRowCharacteristicId)) {
        productCharacteristic.getProductCharacteristicConfList().forEach(chConf -> {
          rowCharacteristics
              .put(new JSONObject(Map.of("id", chConf.getCharacteristicValue().getId(),
                  "_identifier", chConf.getCharacteristicValue().getIdentifier())));
        });
      } else if (productCharacteristic.getCharacteristic()
          .getId()
          .equals(prodColumnCharacteristicId)) {
        productCharacteristic.getProductCharacteristicConfList().forEach(chConf -> {
          columnCharacteristics
              .put(new JSONObject(Map.of("id", chConf.getCharacteristicValue().getId(),
                  "_identifier", chConf.getCharacteristicValue().getIdentifier())));
        });
      }
    }

    resultList.add(Map.of( //
        "rowCharacteristics", rowCharacteristics, //
        "columnCharacteristics", columnCharacteristics //
    ));

    return resultList;
  }
}
