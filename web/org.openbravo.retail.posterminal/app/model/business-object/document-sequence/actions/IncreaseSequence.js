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
      const newDocumentSequence = { ...documentSequence };
      const { sequenceName } = payload;

      if (newDocumentSequence[sequenceName]) {
        const newStateSequence = { ...newDocumentSequence[sequenceName] };
        newStateSequence.sequenceNumber =
          newDocumentSequence[sequenceName].sequenceNumber + 1;
        newDocumentSequence[sequenceName] = newStateSequence;
      }

      return newDocumentSequence;
    }
  );
})();
