/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  var BPCategory = Backbone.Model.extend({
    modelName: 'BPCategory',
    tableName: 'c_bp_group',
    entityName: 'BPCategory',
    source: 'org.openbravo.retail.posterminal.master.BPCategory',
    dataLimit: 300,
    properties: ['id', 'searchKey', 'name', '_identifier', '_idx'],
    propertyMap: {
      'id': 'c_bp_group_id',
      'searchKey': 'value',
      'name': 'name',
      '_identifier': '_identifier',
      '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS c_bp_group (c_bp_group_id TEXT PRIMARY KEY , value TEXT , name TEXT , _identifier TEXT , _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS c_bp_group',
    insertStatement: 'INSERT INTO c_bp_group (c_bp_group_id, value, name, _identifier, _idx)  VALUES (?, ?, ?, ?, ?)'
  });

  var BPCategoryList = Backbone.Collection.extend({
    model: BPCategory
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.BPCategory = BPCategory;
  window.OB.Collection.BPCategoryList = BPCategoryList;
}());