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

  var ServicePriceRuleRangePrices = OB.Data.ExtensibleModel.extend({
    modelName: 'ServicePriceRuleRangePrices',
    tableName: 'm_servicepricerule_rangeprices',
    entityName: 'ServicePriceRuleRangePrices',
    source: 'org.openbravo.retail.posterminal.master.ServicePriceRuleRangePrices',
    dataLimit: 100,
    includeTerminalDate: true,
    hgvol: 'OBPOS_highVolume.product'
  });

  ServicePriceRuleRangePrices.addProperties([{
    name: 'id',
    column: 'id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'priceList',
    column: 'priceList',
    type: 'TEXT'
  }, {
    name: 'product',
    column: 'product',
    type: 'TEXT'
  }, {
    name: 'listPrice',
    column: 'listPrice',
    type: 'NUMBER'
  }]);

  OB.Data.Registry.registerModel(ServicePriceRuleRangePrices);
}());