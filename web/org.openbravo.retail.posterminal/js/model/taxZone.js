/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, Backbone */
(function () {

  var taxZone = OB.Data.ExtensibleModel.extend({
    modelName: 'TaxZone',
    tableName: 'c_tax_zone',
    entityName: 'FinancialMgmtTaxZone',
    source: 'org.openbravo.retail.posterminal.master.TaxZone'
  });

  taxZone.addProperties([{
    name: 'id',
    column: 'c_tax_zone_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'tax',
    column: 'c_tax_id',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'fromCountry',
    column: 'from_country_id',
    filter: true,
    type: 'TEXT'
  }, {
    name: 'fromRegion',
    column: 'from_region_id',
    type: 'TEXT'
  }, {
    name: 'destinationCountry',
    column: 'to_country_id',
    type: 'TEXT'
  }, {
    name: 'destinationRegion',
    column: 'to_region_id',
    type: 'TEXT'
  }]);

  taxZone.addIndex([{
    name: 'obpos_in_taxZone',
    columns: [{
      name: 'c_tax_id',
      sort: 'asc'
    }]
  }]);

  OB.Data.Registry.registerModel(taxZone);
}());