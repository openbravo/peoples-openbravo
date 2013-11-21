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

  var TaxCashUp = Backbone.Model.extend({
    modelName: 'TaxCashUp',
    tableName: 'taxcashup',
    entityName: '',
    source: '',
    local: true,
    properties: ['id', 'tax_id', 'name', 'amount', 'orderType', 'cashup_id'],
    propertyMap: {
      'id': 'taxcashup_id',
      'tax_id': 'tax_id',
      'name': 'name',
      'amount': 'amount',
      'orderType': 'orderType',
      'cashup_id': 'cashup_id'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS taxcashup (taxcashup_id TEXT PRIMARY KEY, tax_id TEXT, name TEXT, amount TEXT, orderType TEXT, cashup_id TEXT)',
    dropStatement: 'DROP TABLE IF EXISTS taxcashup',
    insertStatement: 'INSERT INTO taxcashup(taxcashup_id, tax_id, name, amount, orderType, cashup_id) VALUES (?,?,?,?,?,?)',
  });

  window.OB.Model.TaxCashUp = TaxCashUp;
}());