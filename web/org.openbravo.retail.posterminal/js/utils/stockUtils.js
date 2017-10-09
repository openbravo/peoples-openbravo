/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function () {
  OB.UTIL.StockUtils = {};

  OB.UTIL.StockUtils.getReceiptLineStock = function (productId, line, callback) {
    var serverCallStoreDetailedStock = new OB.DS.Process('org.openbravo.retail.posterminal.stock.StoreDetailedStock');
    serverCallStoreDetailedStock.exec({
      organization: OB.MobileApp.model.get('terminal').organization,
      product: productId ? productId : line.get('product').get('id'),
      line: line
    }, function (data) {
      callback(data);
    });
  };
}());