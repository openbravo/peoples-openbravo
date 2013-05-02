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

  var ProductCharacteristic = Backbone.Model.extend({
    modelName: 'ProductCharacteristic',
    tableName: 'm_product_ch',
    entityName: 'ProductCharacteristic',
    source: 'org.openbravo.retail.posterminal.master.ProductCharacteristic',
    dataLimit: 300,
    properties: ['m_product_ch_id', 'm_product', 'characteristic_id', 'characteristic', 'ch_value_id', 'ch_value', '_identifier', '_idx'],
    propertyMap: {
      'm_product_ch_id': 'm_product_ch_id',
      'm_product': 'm_product',
      'characteristic_id': 'characteristic_id',
      'characteristic': 'characteristic',
      'ch_value_id': 'ch_value_id',
      'ch_value': 'ch_value',
      '_identifier': '_identifier',
      '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS m_product_ch (m_product_ch_id TEXT PRIMARY KEY, m_product TEXT , characteristic_id TEXT, characteristic TEXT, ch_value_id TEXT, ch_value TEXT, _identifier TEXT, _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS m_product_ch',
    insertStatement: 'INSERT INTO m_product_ch(m_product_ch_id, m_product, characteristic_id, characteristic, ch_value_id, ch_value, _identifier, _idx)  VALUES (?, ?, ?, ?, ?, ?, ?, ?)'
  });

  OB.Data.Registry.registerModel(ProductCharacteristic);
}());