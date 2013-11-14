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
    properties: ['id', 'payName', 'startingCash', 'totalTendered', 'rate', 'cashup_id'],
    propertyMap: {
      'id': 'paymentmethodcashup_id',
      'payName': 'payName',
      'startingCash': 'startingCash',
      'totalTendered': 'totalTendered',
      'rate': 'rate',
      'cashup_id': 'cashup_id'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS paymentmethodcashup (paymentmethodcashup_id TEXT PRIMARY KEY, payName TEXT, startingCash TEXT, totalTendered TEXT, rate TEXT, cashup_id TEXT)',
    dropStatement: 'DROP TABLE IF EXISTS paymentmethodcashup',
    insertStatement: 'INSERT INTO paymentmethodcashup(paymentmethodcashup_id, payName, startingCash, totalTendered, rate, cashup_id) VALUES (?,?,?,?,?,?)',
  });

  window.OB.Model.PaymentMethodCashUp = PaymentMethodCashUp;
}());