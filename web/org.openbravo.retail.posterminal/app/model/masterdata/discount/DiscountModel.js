/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function discountModel() {
  class Discount extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'name',
          properties: [{ property: 'name' }]
        })
      ];
    }

    getName() {
      return 'Discount';
    }
  }

  OB.App.MasterdataController.registerModel(Discount);
})();
