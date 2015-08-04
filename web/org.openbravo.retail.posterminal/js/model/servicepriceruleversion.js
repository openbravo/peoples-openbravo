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

  var ServicePriceRuleVersion = OB.Data.ExtensibleModel.extend({
    modelName: 'ServicePriceRuleVersion',
    tableName: 'm_servicepricerule_version',
    entityName: 'ServicePriceRuleVersion',
    source: 'org.openbravo.retail.posterminal.master.ServicePriceRuleVersion',
    dataLimit: 100,
    includeTerminalDate: true,
    hgvol: 'OBPOS_highVolume.product'
  });

  ServicePriceRuleVersion.addProperties([{
    name: 'id',
    column: 'm_servicepricerule_version_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'active',
    column: 'active',
    type: 'BOOL'
  }, {
    name: 'product',
    column: 'product',
    type: 'TEXT'
  }, {
    name: 'validFromDate',
    column: 'validFromDate',
    type: 'TEXT'
  }, {
    name: 'servicePriceRule',
    column: 'servicePriceRule',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(ServicePriceRuleVersion);
}());