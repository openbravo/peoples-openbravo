/*global Backbone */

(function () {
  var TaxRate = Backbone.Model.extend({
    modelName: 'TaxRate',
    tableName: 'c_tax',
    entityName: 'FinancialMgmtTaxRate'
  });

  var TaxRates = Backbone.Collection.extend({
    model: TaxRate
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.TaxRate = TaxRate;
  window.OB.Collection.TaxRates = TaxRates;
}());