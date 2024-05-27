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
package org.openbravo.client.application.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCharacteristicValue;

public class UpdateMultiVariantPurchaseOrderLines extends BaseActionHandler {

  public static final String NO_COLUMN_CHARACTERISTIC = "NO_COLUMN_CHARACTERISTIC";
  public static final String NO_ROW_CHARACTERISTIC = "NO_ROW_CHARACTERISTIC";
  int lastLineNo = 0;

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject result = new JSONObject();

    try {
      JSONObject jsonContent = new JSONObject(content);
      String orderId = jsonContent.getJSONArray("recordIdList").getString(0);
      JSONArray productRows = jsonContent.getJSONArray("productRows");
      JSONObject productRowsData = jsonContent.getJSONObject("productRowsData");

      Order order = OBDal.getInstance().get(Order.class, orderId);
      List<OrderLine> orderLines = order.getOrderLineList();

      List<String> productIdsFromProcess = new ArrayList<>();
      for (int i = 0; i < productRows.length(); i++) {
        productIdsFromProcess.add(productRows.getJSONObject(i).getString("product"));
      }

      List<String> removedProductIds = new ArrayList<>();
      List<String> genericProductIdsFromOrderLines = new ArrayList<>();
      for (OrderLine orderLine : orderLines) {
        Product genericProduct = orderLine.getProduct().getGenericProduct();
        if (genericProduct != null) {
          genericProductIdsFromOrderLines.add(genericProduct.getId());
          if (!productIdsFromProcess.contains(genericProduct.getId())) {
            removedProductIds.add(genericProduct.getId());
          }
        }
      }

      removeAllOrderLinesRelatedToGenericProduct(order, removedProductIds);

      List<String> changedGenericProductIds = productIdsFromProcess.stream()
          .filter(genericProductIdsFromOrderLines::contains)
          .collect(Collectors.toList());
      updateOrderLinesForExistingGenericProduct(order, productRowsData, changedGenericProductIds);

      List<String> newGenericProductIds = productIdsFromProcess.stream()
          .filter(productId -> !genericProductIdsFromOrderLines.contains(productId))
          .collect(Collectors.toList());

      generateOrderLinesForNewGenericProducts(newGenericProductIds, productRowsData, order);
    } catch (JSONException e) {
      // TODO: Probably explain that it was not possible to handle the update
      throw new RuntimeException(e);
    }

    return result;
  }

  private void generateOrderLinesForNewGenericProducts(List<String> newGenericProductIds,
      JSONObject productRowsData, Order order) {
    for (String newGenericProductId : newGenericProductIds) {
      Product newGenericProduct = OBDal.getInstance().get(Product.class, newGenericProductId);
      try {
        JSONArray productRowData = productRowsData.getJSONArray(newGenericProductId);
        Map<String, Map<String, Integer>> mappedProductRowData = getProductRowDataMapped(
            productRowData);

        List<Product> allVariantsOfGenericProduct = getAllVariantsOfGenericProduct(
            newGenericProductId);

        for (Product variantProduct : allVariantsOfGenericProduct) {
          // Check product characteristic value against mappedProductRowData row/column
          // characteristic values
          String productRowCharacteristicValue = "";
          String productColumnCharacteristicValue = "";

          for (ProductCharacteristicValue productCharacteristicValue : variantProduct
              .getProductCharacteristicValueList()) {
            if (productCharacteristicValue.getCharacteristic()
                .getId()
                .equals(newGenericProduct.getRowCharacteristic().getId())) {
              productRowCharacteristicValue = productCharacteristicValue.getCharacteristicValue()
                  .getId();
            } else if (productCharacteristicValue.getCharacteristic()
                .getId()
                .equals(newGenericProduct.getColumnCharacteristic().getId())) {
              productColumnCharacteristicValue = productCharacteristicValue.getCharacteristicValue()
                  .getId();
            }
          }

          int quantity = getQuantityFromProductRowData(newGenericProduct, mappedProductRowData,
              productRowCharacteristicValue, productColumnCharacteristicValue);

          if (quantity != 0) {
            // A new orderline should be created with this variant product
            createOrderLine(order, variantProduct, quantity);
          }
        }
      } catch (JSONException e) {
        // TODO: No product row data for line, some log might be required
      }
    }
  }

  private static int getQuantityFromProductRowData(Product genericProduct,
      Map<String, Map<String, Integer>> mappedProductRowData, String productRowCharacteristicValue,
      String productColumnCharacteristicValue) {
    boolean hasRowCharacteristic = genericProduct.getRowCharacteristic() != null;
    boolean hasColumnCharacteristic = genericProduct.getColumnCharacteristic() != null;
    int quantity = 0;
    if (hasRowCharacteristic) {
      if (hasColumnCharacteristic) {
        quantity = mappedProductRowData.get(productRowCharacteristicValue)
            .get(productColumnCharacteristicValue);
      } else {
        quantity = mappedProductRowData.get(productRowCharacteristicValue)
            .get(NO_COLUMN_CHARACTERISTIC);
      }
    } else if (hasColumnCharacteristic) {
      quantity = mappedProductRowData.get(productColumnCharacteristicValue)
          .get(NO_ROW_CHARACTERISTIC);
    }
    return quantity;
  }

  private static List<Product> getAllVariantsOfGenericProduct(String newGenericProductId) {
    OBCriteria<Product> obc = OBDal.getInstance().createCriteria(Product.class);
    obc.setFilterOnActive(true);
    obc.setFilterOnReadableClients(true);
    obc.setFilterOnReadableOrganization(true);

    obc.add(Restrictions.eq(Product.PROPERTY_GENERICPRODUCT + ".id", newGenericProductId));
    obc.add(Restrictions.eq(Product.PROPERTY_ISGENERIC, false));
    return obc.list();
  }

  /**
   * Finds each order line that contains a given generic product id, and removes it
   * 
   * @param genericProductIds
   *          - List of generic product ids, related orderlines will be removed
   */
  private void removeAllOrderLinesRelatedToGenericProduct(Order order,
      List<String> genericProductIds) {
    if (genericProductIds.isEmpty()) {
      return;
    }
    List<OrderLine> orderLines = order.getOrderLineList();
    List<OrderLine> orderLinesToRemove = new ArrayList<>();
    for (OrderLine orderLine : orderLines) {
      Product orderLineGenericProduct = orderLine.getProduct().getGenericProduct();
      if (orderLineGenericProduct != null
          && genericProductIds.contains(orderLineGenericProduct.getId())) {
        // Remove product from both list and DAL
        orderLinesToRemove.add(orderLine);
      }
    }

    orderLinesToRemove.forEach(orderLine -> removeOrderLine(order, orderLine));
  }

  private void removeOrderLine(Order order, OrderLine orderLine) {
    List<OrderLine> orderLines = order.getOrderLineList();
    orderLines.remove(orderLine);
    OBDal.getInstance().remove(orderLine);
    OBDal.getInstance().save(order);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(order);
  }

  private void updateOrderLinesForExistingGenericProduct(Order order, JSONObject productRowsData,
      List<String> genericProductsToUpdate) throws JSONException {
    for (String genericProductId : genericProductsToUpdate) {
      if (!productRowsData.has(genericProductId)) {
        // Skip generic products that have not been modified
        continue;
      }

      Map<String, Map<String, Integer>> mappedProductRowData = getProductRowDataMapped(
          productRowsData.getJSONArray(genericProductId));
      Product genericProductToUpdate = OBDal.getInstance().get(Product.class, genericProductId);
      List<Product> variantProducts = getAllVariantsOfGenericProduct(genericProductId);
      for (Product variantProduct : variantProducts) {
        // Check product characteristic value against mappedProductRowData row/column
        // characteristic values
        String productRowCharacteristicValue = "";
        String productColumnCharacteristicValue = "";

        for (ProductCharacteristicValue productCharacteristicValue : variantProduct
            .getProductCharacteristicValueList()) {
          if (productCharacteristicValue.getCharacteristic()
              .getId()
              .equals(genericProductToUpdate.getRowCharacteristic().getId())) {
            productRowCharacteristicValue = productCharacteristicValue.getCharacteristicValue()
                .getId();
          } else if (productCharacteristicValue.getCharacteristic()
              .getId()
              .equals(genericProductToUpdate.getColumnCharacteristic().getId())) {
            productColumnCharacteristicValue = productCharacteristicValue.getCharacteristicValue()
                .getId();
          }
        }

        int quantity = getQuantityFromProductRowData(genericProductToUpdate, mappedProductRowData,
            productRowCharacteristicValue, productColumnCharacteristicValue);
        // Find if OrderLine already exists for this variant product
        List<OrderLine> existingOrderLines = order.getOrderLineList()
            .stream()
            .filter(orderLine -> orderLine.getProduct().getId().equals(variantProduct.getId()))
            .collect(Collectors.toList());
        if (existingOrderLines.isEmpty()) {
          // OrderLine for this variant does not exist, one should be created if quantity > 0
          if (quantity > 0) {
            createOrderLine(order, variantProduct, quantity);
          }
        } else {
          // Assumed that there is only one existing orderLine for a given variant product
          OrderLine orderLineOfVariantProduct = existingOrderLines.get(0);
          if (quantity == 0) {
            // OrderLine must be removed
            removeOrderLine(order, orderLineOfVariantProduct);
          } else if (quantity != orderLineOfVariantProduct.getOrderedQuantity().intValue()) {
            // OrderLine should be update with the new quantity if it is different
            updateOrderLineQuantity(order, orderLineOfVariantProduct, quantity);
          }
        }
      }
    }
  }

  private static void updateOrderLineQuantity(Order order, OrderLine orderLineOfVariantProduct,
      int quantity) {
    orderLineOfVariantProduct.setOrderedQuantity(BigDecimal.valueOf(quantity));
    OBDal.getInstance().save(orderLineOfVariantProduct);
    OBDal.getInstance().save(order);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(order);
  }

  private Map<String, Map<String, Integer>> getProductRowDataMapped(JSONArray productRowData)
      throws JSONException {
    Map<String, Map<String, Integer>> mappedProductRowData = new HashMap<>();
    for (int i = 0; i < productRowData.length(); i++) {
      JSONObject productRow = productRowData.getJSONObject(i);
      if (productRow.has("rowCharacteristicValue")) {
        String rowCharacteristic = productRow.getString("rowCharacteristicValue");
        Map<String, Integer> row = mappedProductRowData.getOrDefault(rowCharacteristic,
            new HashMap<>());
        if (productRow.has("columnCharacteristicValue")) {
          row.put(productRow.getString("columnCharacteristicValue"), productRow.getInt("quantity"));
        } else {
          row.put(NO_COLUMN_CHARACTERISTIC, productRow.getInt("quantity"));
        }
        mappedProductRowData.put(rowCharacteristic, row);
      } else if (productRow.has("columnCharacteristicValue")) {
        String columnCharacteristic = productRow.getString("columnCharacteristicValue");
        Map<String, Integer> column = mappedProductRowData.getOrDefault(columnCharacteristic,
            new HashMap<>());
        column.put(NO_ROW_CHARACTERISTIC, productRow.getInt("quantity"));
        mappedProductRowData.put(columnCharacteristic, column);
      }
    }

    return mappedProductRowData;
  }

  private void createOrderLine(Order order, Product variantProduct, Integer quantity) {
    List<OrderLine> orderLines = order.getOrderLineList();
    OrderLine newOrderLine = OBProvider.getInstance().get(OrderLine.class);
    newOrderLine.setLineNo((long) getNextLineNo(order));
    newOrderLine.setProduct(variantProduct);
    newOrderLine.setOrderedQuantity(BigDecimal.valueOf(quantity));
    newOrderLine.setOrganization(order.getOrganization());
    newOrderLine.setSalesOrder(order);
    newOrderLine.setUOM(variantProduct.getUOM());
    newOrderLine.setOrderDate(new Date());
    newOrderLine.setWarehouse(order.getWarehouse());
    newOrderLine.setCurrency(order.getCurrency());

    // TODO: Get prices from pricelist to set the values of prices
    // TODO: Also include tax

    newOrderLine.setNewOBObject(true);
    orderLines.add(newOrderLine);
    OBDal.getInstance().save(newOrderLine);
    OBDal.getInstance().save(order);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(order); // TODO: Maybe this refresh is not actually required
  }

  private int getNextLineNo(Order order) {
    if (lastLineNo == 0) {
      order.getOrderLineList().forEach(orderLine -> {
        if (orderLine.getLineNo().intValue() > lastLineNo) {
          lastLineNo = orderLine.getLineNo().intValue();
        }
      });
    }
    lastLineNo += 10;
    return lastLineNo;
  }
}
