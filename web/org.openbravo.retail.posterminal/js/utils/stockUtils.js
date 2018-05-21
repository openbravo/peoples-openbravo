/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _ */

(function () {
  OB.UTIL.StockUtils = {};

  OB.UTIL.StockUtils.getReceiptLineStock = function (productId, line, successCallback, errorCallback) {
    var serverCallStoreDetailedStock = new OB.DS.Process('org.openbravo.retail.posterminal.stock.StoreDetailedStock');
    serverCallStoreDetailedStock.exec({
      organization: OB.MobileApp.model.get('terminal').organization,
      product: productId ? productId : line.get('product').get('id'),
      line: line
    }, function (data) {
      successCallback(data);
    }, function (data) {
      errorCallback(data);
    });
  };

  OB.UTIL.StockUtils.checkOrderLinesStock = function (orders, callback) {
    var checkOrderStock, checkOrderLineStock;
    checkOrderLineStock = function (idxOrderLine, order, orderCallback) {
      if (idxOrderLine === order.get('lines').length) {
        orderCallback();
        return;
      }
      var line = order.get('lines').at(idxOrderLine),
          productStatus = OB.UTIL.ProductStatusUtils.getProductStatus(line.get('product')),
          qtyInOtherOrders = OB.DEC.Zero;
      // Get the quantity if the other editable orders for this line
      _.each(orders, function (currentOrder) {
        if (order.id !== currentOrder.id && currentOrder.get('isEditable')) {
          _.each(currentOrder.get('lines').models, function (l) {
            if ((l.get('product').get('id') === line.get('product').get('id') && l.get('warehouse').id === line.get('warehouse').id)) {
              qtyInOtherOrders += l.get('qty');
            }
          });
        }
      });
      if (OB.DEC.compare(line.get('qty')) === 1 && productStatus && productStatus.restrictsaleoutofstock) {
        var options = {
          line: line
        };
        order.getStoreStock(line.get('product'), qtyInOtherOrders, options, null, function (hasStock) {
          if (hasStock) {
            checkOrderLineStock(idxOrderLine + 1, order, orderCallback);
          } else {
            callback(false);
          }
        });
      } else {
        checkOrderLineStock(idxOrderLine + 1, order, orderCallback);
      }
    };
    checkOrderStock = function (idxOrder) {
      if (idxOrder === orders.length) {
        callback(true);
        return;
      }
      var order = orders[idxOrder];
      if (order.get('isEditable')) {
        checkOrderLineStock(0, order, function (hasStock) {
          checkOrderStock(idxOrder + 1);
        });
      } else {
        checkOrderStock(idxOrder + 1);
      }
    };
    // Check stock for the lines that are not allowed to be sold without stock
    checkOrderStock(0);
  };
}());