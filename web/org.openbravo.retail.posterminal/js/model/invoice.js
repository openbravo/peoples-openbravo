/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, OB */

(function () {

  // Sales Invoice Model.
  var Invoice = OB.Model.Order.extend({
    modelName: 'Invoice',
    tableName: 'c_invoice',
    entityName: 'Invoice',
    source: '',
    dataLimit: OB.Dal.DATALIMIT,
    properties: ['id', 'json', 'session', 'isbeingprocessed'],
    propertyMap: {
      'id': 'c_invoice_id',
      'json': 'json',
      'session': 'ad_session_id',
      'isbeingprocessed': 'isbeingprocessed'
    },

    defaults: {
      isbeingprocessed: 'N',
      isInvoice: true
    },

    createStatement: 'CREATE TABLE IF NOT EXISTS c_invoice (c_invoice_id TEXT PRIMARY KEY, json CLOB, ad_session_id TEXT, isbeingprocessed TEXT)',
    dropStatement: 'DROP TABLE IF EXISTS c_invoice',
    insertStatement: 'INSERT INTO c_invoice(c_invoice_id, json, ad_session_id, isbeingprocessed) VALUES (?,?,?,?)',
    local: true
  });

  window.OB.Model.Invoice = Invoice;

}());