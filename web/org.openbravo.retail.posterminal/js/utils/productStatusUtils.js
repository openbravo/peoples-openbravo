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
  OB.UTIL.ProductStatusUtils = {};

  OB.UTIL.ProductStatusUtils.getProductStatus = function (product) {
    var productLineStatus = product.get('producAssortmentStatus') ? product.get('producAssortmentStatus') : (product.get('productStatus') ? product.get('productStatus') : null);
    if (productLineStatus) {
      return _.find(OB.MobileApp.model.get('productStatusList'), function (productStatus) {
        return productLineStatus === productStatus.id;
      });
    }
    return null;
  };
}());