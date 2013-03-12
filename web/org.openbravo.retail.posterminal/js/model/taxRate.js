(function () {

  var taxRate = Backbone.Model.extend({
    modelName: 'TaxRate',
    generatedStructure: true,
    entityName: 'FinancialMgmtTaxRate',
    source: 'org.openbravo.retail.posterminal.master.TaxRate',
  });

  OB.Data.Registry.registerModel(taxRate);
}());