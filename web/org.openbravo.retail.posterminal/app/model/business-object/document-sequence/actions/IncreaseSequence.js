/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Increase Sequence action that increases in one the sequence defined in the payload
 */

(() => {
  OB.App.StateAPI.DocumentSequence.registerAction(
    'increaseSequence',
    (documentSequence, payload) => {
      const { sequenceName } = payload;

      return OB.App.State.DocumentSequence.Utils.increaseSequence(
        documentSequence,
        sequenceName
      );
    }
  );
})();
