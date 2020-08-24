/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function discountFilterBusinessPartnerModel() {
  class DiscountFilterBusinessPartner extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'priceAdjustment_identifier_id',
          properties: [
            { property: 'priceAdjustment' },
            { property: '_identifier' },
            { property: 'id' }
          ]
        })
      ];
      this.setPaginationById(true);
    }

    // eslint-disable-next-line class-methods-use-this
    getName() {
      return 'DiscountFilterBusinessPartner';
    }

    // eslint-disable-next-line class-methods-use-this
    isRemote() {
      return OB.App.Security.hasPermission('OBPOS_remote.discount.bp');
    }
  }

  OB.App.MasterdataController.registerModel(DiscountFilterBusinessPartner);
})();
