/*
 ************************************************************************************
 * Copyright (C) 2013-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global _, Backbone */
(function () {

  var taxRate = OB.Data.ExtensibleModel.extend({
    modelName: 'TaxRate',
    generatedStructure: true,
    entityName: 'FinancialMgmtTaxRate',
    source: 'org.openbravo.retail.posterminal.master.TaxRate'
  });

  OB.Data.Registry.registerModel(taxRate);
}());