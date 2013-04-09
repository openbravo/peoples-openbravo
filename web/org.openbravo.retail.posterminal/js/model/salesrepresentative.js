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

  var SalesRepresentative = Backbone.Model.extend({
    modelName: 'SalesRepresentative',
    tableName: 'ad_sales_representative',
    entityName: 'SalesRepresentative',
    source: 'org.openbravo.retail.posterminal.master.SalesRepresentative',
    dataLimit: 300,
    properties: ['id', 'name', 'username', '_identifier', '_idx'],
    propertyMap: {
      'id': 'ad_sales_representative_id',
      'name': 'name',
      'username': 'username',
      '_identifier': '_identifier',
      '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS ad_sales_representative (ad_sales_representative_id TEXT PRIMARY KEY, username TEXT , name TEXT, _identifier TEXT, _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS ad_sales_representative',
    insertStatement: 'INSERT INTO ad_sales_representative(ad_sales_representative_id, name, username, _identifier, _idx)  VALUES (?, ?, ?, ?, ?)'
  });

  OB.Data.Registry.registerModel(SalesRepresentative);
}());