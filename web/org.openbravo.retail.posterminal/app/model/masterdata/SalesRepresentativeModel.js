/*
 ************************************************************************************
 * Copyright (C) 2013-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function SalesRepresentativeDefinition() {
  class SalesRepresentative extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: '_identifier',
          properties: [{ property: '_identifier' }]
        })
      ];
    }

    getName() {
      return 'SalesRepresentative';
    }
  }
  OB.App.MasterdataController.registerModel(SalesRepresentative);
})();
