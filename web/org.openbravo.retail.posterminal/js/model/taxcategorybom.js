/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, Backbone */
(function () {

  var TaxCategoryBOM = OB.Data.ExtensibleModel.extend({
    modelName: 'TaxCategoryBOM',
    tableName: 'c_taxcategory_bom',
    entityName: 'FinancialMgmtTaxCategory',
    source: 'org.openbravo.retail.posterminal.master.TaxCategoryBOM'
  });

  TaxCategoryBOM.addProperties([{
    name: 'id',
    column: 'id',
    primaryKey: true,
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(TaxCategoryBOM);
}());