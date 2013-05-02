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

  var ProductChValue = Backbone.Model.extend({
    modelName: 'ProductChValue',
    tableName: 'm_ch_value',
    entityName: 'ProductChValue',
    source: 'org.openbravo.retail.posterminal.master.ProductChValue',
    dataLimit: 300,
    properties: ['id', 'name', 'characteristic_id', 'parent', '_identifier', '_idx'],
    propertyMap: {
      'id': 'id',
      'name': 'name',
      'characteristic_id': 'characteristic_id',
      'parent': 'parent',
      '_identifier': '_identifier',
      '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS m_ch_value (id TEXT PRIMARY KEY, name TEXT , characteristic_id TEXT, parent TEXT,  _identifier TEXT, _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS m_ch_value',
    insertStatement: 'INSERT INTO m_ch_value(id, name, characteristic_id, parent, _identifier, _idx)  VALUES (?, ?, ?, ?, ?, ?)'
  });

  OB.Data.Registry.registerModel(ProductChValue);
}());