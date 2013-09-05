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

  var BPCategory = OB.Data.ExtensibleModel.extend({
    modelName: 'BPCategory',
    tableName: 'c_bp_group',
    entityName: 'BPCategory',
    source: 'org.openbravo.retail.posterminal.master.BPCategory',
    dataLimit: 300
  });

  BPCategory.addProperties([{
    name: 'id',
    column: 'c_bp_group_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'searchKey',
    column: 'value',
    type: 'TEXT'
  }, {
    name: 'name',
    column: 'name',
    type: 'TEXT'
  }, {
    name: '_identifier',
    column: '_identifier',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(BPCategory);
}());