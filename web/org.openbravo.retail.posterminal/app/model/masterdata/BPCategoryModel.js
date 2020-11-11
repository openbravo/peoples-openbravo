/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function BPCategoryDefinition() {
  class BPCategory extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'name',
          properties: [{ property: 'name' }]
        })
      ];
    }

    // eslint-disable-next-line class-methods-use-this
    getName() {
      return 'BPCategory';
    }
  }
  OB.App.MasterdataController.registerModel(BPCategory);
})();