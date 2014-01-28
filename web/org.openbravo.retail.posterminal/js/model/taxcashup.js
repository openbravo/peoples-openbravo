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

  var TaxCashUp = OB.Data.ExtensibleModel.extend({
    modelName: 'TaxCashUp',
    tableName: 'taxcashup',
    entityName: '',
    source: '',
    local: true
  });

  TaxCashUp.addProperties([{
    name: 'id',
    column: 'taxcashup_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'tax_id',
    column: 'tax_id',
    type: 'TEXT'
  }, {
    name: 'name',
    column: 'name',
    type: 'TEXT'
  }, {
    name: 'amount',
    column: 'amount',
    type: 'TEXT'
  }, {
    name: 'orderType',
    column: 'orderType',
    type: 'TEXT'
  }, {
    name: 'cashup_id',
    column: 'cashup_id',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(TaxCashUp);
}());