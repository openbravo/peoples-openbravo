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

  var PaymentMethodCashUp = Backbone.Model.extend({
    modelName: 'PaymentMethodCashUp',
    tableName: 'paymentmethodcashup',
    entityName: '',
    source: '',
    local: true,
    properties: ['id', 'paymentmethod_id', 'searchKey', 'name', 'startingCash', 'totalSales', 'totalReturns', 'rate', 'cashup_id'],
    propertyMap: {
      'id': 'paymentmethodcashup_id',
      'paymentmethod_id': 'paymentmethod_id',
      'searchKey': 'searchKey',
      'name': 'name',
      'startingCash': 'startingCash',
      'totalSales': 'totalSales',
      'totalReturns': 'totalReturns',
      'rate': 'rate',
      'cashup_id': 'cashup_id'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS paymentmethodcashup (paymentmethodcashup_id TEXT PRIMARY KEY, paymentmethod_id TEXT, searchKey TEXT, name TEXT, startingCash TEXT, totalSales TEXT, totalReturns TEXT, rate TEXT, cashup_id TEXT)',
    dropStatement: 'DROP TABLE IF EXISTS paymentmethodcashup',
    insertStatement: 'INSERT INTO paymentmethodcashup(paymentmethodcashup_id, paymentmethod_id, searchKey, name, startingCash, totalSales, totalReturns, rate, cashup_id) VALUES (?,?,?,?,?,?,?,?,?)',
  });

  window.OB.Model.PaymentMethodCashUp = PaymentMethodCashUp;
}());