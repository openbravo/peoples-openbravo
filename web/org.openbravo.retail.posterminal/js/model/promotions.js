/*
 ************************************************************************************
 * Copyright (C) 2013 - 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone */
(function() {
  var promotionsBP = Backbone.Model.extend({
    modelName: 'DiscountFilterBusinessPartner',
    generatedStructure: true,
    entityName: 'PricingAdjustmentBusinessPartner',
    source:
      'org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartner',
    remote: 'OBPOS_remote.discount.bp'
  });

  OB.Data.Registry.registerModel(promotionsBP);
})();
