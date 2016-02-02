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

  var ServicePriceRule = OB.Data.ExtensibleModel.extend({
    modelName: 'ServicePriceRule',
    tableName: 'm_servicepricerule',
    entityName: 'ServicePriceRule',
    source: 'org.openbravo.retail.posterminal.master.ServicePriceRule',
    dataLimit: 100,
    includeTerminalDate: true,
    remote: 'OBPOS_remote.product'
  });

  ServicePriceRule.addProperties([{
    name: 'id',
    column: 'm_servicepricerule_id',
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
    name: 'percentage',
    column: 'percentage',
    type: 'NUMBER'
  }, {
    name: 'ruletype',
    column: 'ruletype',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(ServicePriceRule);
}());