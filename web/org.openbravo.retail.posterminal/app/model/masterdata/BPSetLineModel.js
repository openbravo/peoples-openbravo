/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function bpSetLineModel() {
  class BPSetLine extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.setPaginationById(true);
    }

    getName() {
      return 'BPSetLine';
    }
  }

  OB.App.MasterdataController.registerModel(BPSetLine);
})();