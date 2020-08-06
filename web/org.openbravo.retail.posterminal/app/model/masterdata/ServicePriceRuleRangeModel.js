/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function servicePriceRuleRangeModel() {
  class ServicePriceRuleRange extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'servicepricerule_amountUpTo_idx',
          properties: [
            { property: 'servicepricerule' },
            { property: 'amountUpTo', isNullable: true }
          ]
        })
      ];
    }

    // eslint-disable-next-line class-methods-use-this
    getName() {
      return 'ServicePriceRuleRange';
    }
  }

  OB.App.MasterdataController.registerModel(ServicePriceRuleRange);
})();
