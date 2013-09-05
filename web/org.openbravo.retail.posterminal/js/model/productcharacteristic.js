/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _ */

(function () {

  var ProductCharacteristic = OB.Data.ExtensibleModel.extend({
    modelName: 'ProductCharacteristic',
    tableName: 'm_product_ch',
    entityName: 'ProductCharacteristic',
    source: 'org.openbravo.retail.posterminal.master.ProductCharacteristic',
    dataLimit: 300
  });
  
  ProductCharacteristic.addProperties([{
    name: 'm_product_ch_id',
    column: 'm_product_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'm_product',
    column: 'm_product',
    type: 'TEXT'
  }, {
    name: 'characteristic_id',
    column: 'characteristic_id',
    type: 'TEXT'
  }, {
    name: 'characteristic',
    column: 'characteristic',
    type: 'TEXT'
  }, {
    name: 'ch_value_id',
    column: 'ch_value_id',
    type: 'TEXT'
  }, {
    name: 'ch_value',
    column: 'ch_value',
    type: 'TEXT'
  }, {
    name: '_identifier',
    column: '_identifier',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(ProductCharacteristic);
}());