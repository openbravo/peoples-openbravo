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

  var ServicePriceRuleRange = OB.Data.ExtensibleModel.extend({
    modelName: 'ServicePriceRuleRange',
    tableName: 'm_servicepricerule_range',
    entityName: 'ServicePriceRuleRange',
    source: 'org.openbravo.retail.posterminal.master.ServicePriceRuleRange',
    dataLimit: 100,
    includeTerminalDate: true,
    remote: 'OBPOS_remote.product'
  });

  ServicePriceRuleRange.addProperties([{
    name: 'id',
    column: 'm_servicepricerule_range_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'active',
    column: 'active',
    type: 'BOOL'
  }, {
    name: 'afterdiscounts',
    column: 'afterdiscounts',
    type: 'BOOL'
  }, {
    name: 'amountUpTo',
    column: 'amountUpTo',
    type: 'NUMBER'
  }, {
    name: 'percentage',
    column: 'percentage',
    type: 'NUMBER'
  }, {
    name: 'priceList',
    column: 'priceList',
    type: 'TEXT'
  }, {
    name: 'ruleType',
    column: 'ruleType',
    type: 'TEXT'
  }, {
    name: 'servicepricerule',
    column: 'servicepricerule',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(ServicePriceRuleRange);
}());