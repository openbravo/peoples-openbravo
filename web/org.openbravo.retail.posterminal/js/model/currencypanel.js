/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _ */

(function () {

  var CurrencyPanel = Backbone.Model.extend({
    modelName: 'CurrencyPanel',
    tableName: 'obpos_currency_panel',
    entityName: 'OBPOS_CurrencyPanel',
    source: 'org.openbravo.retail.posterminal.master.CurrencyPanel',
    properties: ['id', 'currency', 'amount', 'backcolor', 'bordercolor', 'lineNo', '_identifier', '_idx'],
    propertyMap: {
      'id': 'obpos_currency_panel_id',
      'currency': 'c_currency_id',
      'amount': 'amount',
      'backcolor': 'backcolor',
      'bordercolor': 'bordercolor',
      'lineNo': 'line',
      '_identifier': '_identifier',
      '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS obpos_currency_panel (obpos_currency_panel_id TEXT PRIMARY KEY , c_currency_id TEXT , amount NUMERIC , backcolor TEXT , bordercolor TEXT , line NUMERIC , _identifier TEXT , _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS obpos_currency_panel',
    insertStatement: 'INSERT INTO obpos_currency_panel(obpos_currency_panel_id, c_currency_id, amount, backcolor, bordercolor, line, _identifier, _idx)  VALUES (?, ?, ?, ?, ?, ?, ?, ?)'
  });

  OB.Data.Registry.registerModel(CurrencyPanel);
}());