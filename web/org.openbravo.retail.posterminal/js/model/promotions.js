(function () {

  var promotions = Backbone.Model.extend({
    modelName: 'Discount',
    generatedStructure: true,
    entityName: 'PricingAdjustment',
    source: 'org.openbravo.retail.posterminal.master.Discount',
  });

  var promotionsBP = Backbone.Model.extend({
    modelName: 'DiscountFilterBusinessPartner',
    generatedStructure: true,
    entityName: 'PricingAdjustmentBusinessPartner',
    source: 'org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartner',
  });

  var promotionsBPCategory = Backbone.Model.extend({
    modelName: 'DiscountFilterBusinessPartnerGroup',
    generatedStructure: true,
    entityName: 'PricingAdjustmentBusinessPartnerGroup',
    source: 'org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartnerGroup',
  });

  var promotionsProduct = Backbone.Model.extend({
    modelName: 'DiscountFilterProduct',
    generatedStructure: true,
    entityName: 'PricingAdjustmentProduct',
    source: 'org.openbravo.retail.posterminal.master.DiscountFilterProduct',
  });

  var promotionsProductCategory = Backbone.Model.extend({
    modelName: 'DiscountFilterProductCategory',
    generatedStructure: true,
    entityName: 'PricingAdjustmentProductCategory',
    source: 'org.openbravo.retail.posterminal.master.DiscountFilterProductCategory',
  });

  OB.Data.Registry.registerModel(promotions);
  OB.Data.Registry.registerModel(promotionsBP);
  OB.Data.Registry.registerModel(promotionsBPCategory);
  OB.Data.Registry.registerModel(promotionsProduct);
  OB.Data.Registry.registerModel(promotionsProductCategory);
}());