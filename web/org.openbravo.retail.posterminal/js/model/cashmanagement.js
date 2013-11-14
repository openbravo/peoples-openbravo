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

  var CashManagement = Backbone.Model.extend({
    modelName: 'CashManagement',
    tableName: 'cashmanagement',
    entityName: '',
    source: '',
    local: true,
    properties: ['id', 'description', 'amount', 'origAmount', 'type', 'reasonId', 'paymentMethodId', 'user', 'time', 'isocode', 'isbeingprocessed'],
    propertyMap: {
      'id': 'cashmanagement_id',
      'description': 'description',
      'amount': 'amount',
      'origAmount': 'origAmount',
      'type': 'type',
      'reasonId': 'reasonId',
      'paymentMethodId': 'paymentMethodId',
      'user': 'user',
      'time': 'time',
      'isocode': 'isocode',
      'isbeingprocessed': 'isbeingprocessed'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS cashmanagement (cashmanagement_id TEXT PRIMARY KEY, description TEXT, amount TEXT, origAmount TEXT, type TEXT, reasonId TEXT, paymentMethodId TEXT, user TEXT, time TEXT, isocode TEXT, isbeingprocessed TEXT)',
    dropStatement: 'DROP TABLE IF EXISTS cashmanagement',
    insertStatement: 'INSERT INTO cashmanagement(cashmanagement_id, description, amount, origAmount, type, reasonId, paymentMethodId, user, time, isocode, isbeingprocessed) VALUES (?,?,?,?,?,?,?,?,?,?,?)',
  });

  window.OB.Model.CashManagement = CashManagement;
}());