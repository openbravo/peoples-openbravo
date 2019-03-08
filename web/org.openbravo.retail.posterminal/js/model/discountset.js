/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function () {

  var DiscountBusinessPartnerSet = OB.Data.ExtensibleModel.extend({
    modelName: 'DiscountBusinessPartnerSet',
    tableName: 'm_offer_bp_set',
    entityName: 'DiscountBusinessPartnerSet',
    source: 'org.openbravo.retail.posterminal.master.DiscountBusinessPartnerSet'
  });

  DiscountBusinessPartnerSet.addProperties([{
    name: 'id',
    column: 'm_offer_bp_set_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'discount',
    column: 'm_offer_id',
    type: 'TEXT'
  }, {
    name: 'bpSet',
    column: 'c_bp_set_id',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(DiscountBusinessPartnerSet);
}());