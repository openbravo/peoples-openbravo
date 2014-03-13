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

  var CashManagement = OB.Data.ExtensibleModel.extend({
    modelName: 'CashManagement',
    tableName: 'cashmanagement',
    entityName: 'CashManagement',
    source: '',
    local: true
  });


  CashManagement.addProperties([{
    name: 'id',
    column: 'cashmanagement_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'description',
    column: 'description',
    type: 'TEXT'
  }, {
    name: 'amount',
    column: 'amount',
    type: 'NUMERIC'
  }, {
    name: 'origAmount',
    column: 'origAmount',
    type: 'NUMERIC'
  }, {
    name: 'type',
    column: 'type',
    type: 'TEXT'
  }, {
    name: 'reasonId',
    column: 'reasonId',
    type: 'TEXT'
  }, {
    name: 'paymentMethodId',
    column: 'paymentMethodId',
    type: 'TEXT'
  }, {
    name: 'user',
    column: 'user',
    type: 'TEXT'
  }, {
    name: 'userId',
    column: 'userId',
    type: 'TEXT'
  }, {
    name: 'time',
    column: 'time',
    type: 'TEXT'
  }, {
    name: 'isocode',
    column: 'isocode',
    type: 'TEXT'
  }, {
    name: 'cashup_id',
    column: 'cashup_id',
    type: 'TEXT'
  }, {
    name: 'glItem',
    column: 'glItem',
    type: 'TEXT'
  }, {
    name: 'isbeingprocessed',
    column: 'isbeingprocessed',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(CashManagement);
}());