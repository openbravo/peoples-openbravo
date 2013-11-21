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

  var CashUp = Backbone.Model.extend({
    modelName: 'CashUp',
    tableName: 'cashup',
    entityName: '',
    source: '',
    local: true,
    properties: ['id', 'netSales', 'grossSales', 'netReturns', 'grossReturns', 'totalRetailTransactions', 'isbeingprocessed'],
    propertyMap: {
      'id': 'cashup_id',
      'netSales': 'netSales',
      'grossSales': 'grossSales',
      'netReturns': 'netReturns',
      'grossReturns': 'grossReturns',
      'totalRetailTransactions': 'totalRetailTransactions',
      'isbeingprocessed': 'isbeingprocessed'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS cashup (cashup_id TEXT PRIMARY KEY, netSales TEXT, grossSales TEXT, netReturns TEXT, grossReturns TEXT, totalRetailTransactions TEXT, isbeingprocessed TEXT)',
    dropStatement: 'DROP TABLE IF EXISTS cashup',
    insertStatement: 'INSERT INTO cashup(cashup_id, netSales, grossSales, netReturns, grossReturns, totalRetailTransactions, isbeingprocessed) VALUES (?,?,?,?,?,?,?)',
  });

  window.OB.Model.CashUp = CashUp;
}());