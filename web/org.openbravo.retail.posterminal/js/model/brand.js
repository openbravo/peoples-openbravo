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

  var Brand = Backbone.Model.extend({
    modelName: 'Brand',
    tableName: 'm_brand',
    entityName: 'Brand',
    source: 'org.openbravo.retail.posterminal.master.Brand',
    dataLimit: 300,
    properties: ['id', 'name', '_identifier', '_idx'],
    propertiesFilter: ['_identifier'],
    propertyMap: {
      'id': 'id',
      'name': 'name',
      '_identifier': '_identifier',
      '_filter': '_filter',
      '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS m_brand (id TEXT PRIMARY KEY, name TEXT, _identifier TEXT, _filter TEXT, _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS m_brand',
    insertStatement: 'INSERT INTO m_brand(id, name,  _identifier,  _filter, _idx)  VALUES (?, ?, ?, ?, ?)'
  });

  OB.Data.Registry.registerModel(Brand);
}());