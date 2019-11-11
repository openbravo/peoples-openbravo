/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class BPCategory extends OB.MasterdataModelDefinition {
    constructor() {
      super();

      this._indices = [
        {
          indexName: 'name',
          keyPath: 'name',
          objectParameters: { unique: false }
        }
      ];
      this._endPoint = 'org.openbravo.retail.posterminal.master.BPCategory';
    }
  }
  OB.MasterdataController.registerModel(BPCategory);
})();
