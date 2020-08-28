/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function servicePriceRuleRangePricesModel() {
  class ServicePriceRuleRangePrices extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'product_priceList_idx',
          properties: [{ property: 'product' }, { property: 'priceList' }]
        })
      ];
    }
  }

  OB.App.MasterdataController.registerModel(ServicePriceRuleRangePrices);
})();