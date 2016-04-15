/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  var ProductPrice = OB.Data.ExtensibleModel.extend({
    modelName: 'ProductPrice',
    tableName: 'm_productprice',
    entityName: 'ProductPrice',
    source: 'org.openbravo.retail.posterminal.master.ProductPrice',
    includeTerminalDate: true,
    remote: 'OBPOS_remote.product'
  });

  ProductPrice.addProperties([{
    name: 'm_productprice_id',
    column: 'm_productprice_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'm_pricelist_id',
    column: 'm_pricelist_id',
    type: 'TEXT'
  }, {
    name: 'm_product_id',
    column: 'm_product_id',
    type: 'TEXT'
  }, {
    name: 'pricelist',
    column: 'pricelist',
    type: 'NUMERIC'
  }, {
    name: 'pricestd',
    column: 'pricestd',
    type: 'NUMERIC'
  }, {
    name: 'pricelimit',
    column: 'pricelimit',
    type: 'NUMERIC'
  }]);

  ProductPrice.addIndex([{
    name: 'obpos_product_price_list',
    columns: [{
      name: 'm_pricelist_id',
      sort: 'desc'
    }, {
      name: 'm_product_id',
      sort: 'desc'
    }]
  }]);

  //Register the model in the application 
  OB.Data.Registry.registerModel(ProductPrice);
}());