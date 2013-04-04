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

  var promotions = Backbone.Model.extend({
    modelName: 'Discount',
    generatedStructure: true,
    entityName: 'PricingAdjustment',
    source: 'org.openbravo.retail.posterminal.master.Discount'
  });

  var promotionsBP = Backbone.Model.extend({
    modelName: 'DiscountFilterBusinessPartner',
    generatedStructure: true,
    entityName: 'PricingAdjustmentBusinessPartner',
    source: 'org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartner'
  });

  var promotionsBPCategory = Backbone.Model.extend({
    modelName: 'DiscountFilterBusinessPartnerGroup',
    generatedStructure: true,
    entityName: 'PricingAdjustmentBusinessPartnerGroup',
    source: 'org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartnerGroup'
  });

  var promotionsProduct = Backbone.Model.extend({
    modelName: 'DiscountFilterProduct',
    generatedStructure: true,
    entityName: 'PricingAdjustmentProduct',
    source: 'org.openbravo.retail.posterminal.master.DiscountFilterProduct'
  });

  var promotionsProductCategory = Backbone.Model.extend({
    modelName: 'DiscountFilterProductCategory',
    generatedStructure: true,
    entityName: 'PricingAdjustmentProductCategory',
    source: 'org.openbravo.retail.posterminal.master.DiscountFilterProductCategory'
  });

  OB.Data.Registry.registerModel(promotions);
  OB.Data.Registry.registerModel(promotionsBP);
  OB.Data.Registry.registerModel(promotionsBPCategory);
  OB.Data.Registry.registerModel(promotionsProduct);
  OB.Data.Registry.registerModel(promotionsProductCategory);
}());