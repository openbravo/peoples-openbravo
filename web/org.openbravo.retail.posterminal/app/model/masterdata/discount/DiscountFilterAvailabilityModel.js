/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function discountFilterAvailabilityModel() {
  class DiscountFilterAvailability extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'priceAdjustment_id',
          properties: [{ property: 'priceAdjustment' }, { property: 'id' }]
        })
      ];
      this.setPaginationById(true);
    }

    getName() {
      return 'DiscountFilterAvailability';
    }
  }

  OB.App.MasterdataController.registerModel(DiscountFilterAvailability);
})();
