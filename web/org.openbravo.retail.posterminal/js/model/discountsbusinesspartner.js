/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  var DiscountFilterBusinessPartner = OB.Data.ExtensibleModel.extend({
    modelName: 'DiscountFilterBusinessPartner',
    tableName: 'm_offer_bpartner',
    entityName: 'PricingAdjustmentBusinessPartner',
    source:
      'org.openbravo.retail.posterminal.master.DiscountFilterBusinessPartner',
    dataLimit: OB.Dal.DATALIMIT,
    remoteDataLimit: OB.Dal.REMOTE_DATALIMIT,
    remote: 'OBPOS_remote.discount.bp',
    indexDBModel: OB.App.MasterdataModels.DiscountFilterBusinessPartner.getName(),
    legacyModel: true
  });

  DiscountFilterBusinessPartner.addProperties([
    {
      name: 'id',
      column: 'm_offer_bpartner_id',
      primaryKey: true,
      type: 'TEXT'
    },
    {
      name: 'priceAdjustment',
      column: 'm_offer_id',
      type: 'TEXT'
    },
    {
      name: 'businessPartner',
      column: 'c_bpartner_id',
      filter: true,
      type: 'TEXT'
    }
  ]);

  OB.Data.Registry.registerModel(DiscountFilterBusinessPartner);
})();
