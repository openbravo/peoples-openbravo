/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function servicePriceRuleVersionModel() {
  class ServicePriceRuleVersion extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'product_validFromDate_idx',
          properties: [{ property: 'product' }, { property: 'validFromDate' }]
        })
      ];
    }

    // eslint-disable-next-line class-methods-use-this
    getName() {
      return 'ServicePriceRuleVersion';
    }

    // eslint-disable-next-line class-methods-use-this
    isRemote() {
      return OB.App.Security.hasPermission('OBPOS_remote.product');
    }
  }

  OB.App.MasterdataController.registerModel(ServicePriceRuleVersion);
})();