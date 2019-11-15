/*
 ************************************************************************************
 * Copyright (C) 2013-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class SalesRepresentative extends OB.MasterdataModelDefinition {
    constructor() {
      super();
      this.indices = [
        {
          indexName: '_identifier',
          keyPath: '_identifier'
        }
      ];
    }
  }
  OB.MasterdataController.registerModel(SalesRepresentative);
})();
