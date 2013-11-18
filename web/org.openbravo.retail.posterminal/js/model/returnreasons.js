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

  var ReturnReason = OB.Data.ExtensibleModel.extend({
    modelName: 'ReturnReason',
    tableName: 'c_return_reason',
    entityName: 'ReturnReason',
    source: 'org.openbravo.retail.posterminal.master.ReturnReason',
    dataLimit: 300
  });

  ReturnReason.addProperties([{
    name: 'id',
    column: 'c_return_reason_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'searchKey',
    column: 'searchKey',
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

  OB.Data.Registry.registerModel(ReturnReason);
}());