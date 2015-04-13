/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  var PriceList = OB.Data.ExtensibleModel.extend({
    modelName: 'PriceList',
    tableName: 'm_pricelist',
    entityName: 'PriceList',
    source: 'org.openbravo.retail.posterminal.master.PriceList',
    dataLimit: 300,
    includeTerminalDate: true,
    initialize: function () {

    }
  });

  PriceList.addProperties([{
    name: 'm_pricelist_id',
    column: 'm_pricelist_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'name',
    column: 'name',
    type: 'TEXT'
  }, {
    name: 'priceIncludesTax',
    column: 'priceIncludesTax',
    type: 'TEXT'
  }, {
    name: 'c_currency_id',
    column: 'c_currency_id',
    type: 'TEXT'
  }]);

  //Register the model in the application 
  OB.Data.Registry.registerModel(PriceList);
}());