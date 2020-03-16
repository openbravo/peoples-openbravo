/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Document Sequence Initialize Sequence action that updates state sequences with the sequences present in the payload.
 * The highest sequence number between the one in the state and the one in the payload will be keep.
 * In case sequence prefix has been changed, sequence number will be reset to zero.
 */

(() => {
  OB.App.StateAPI.DocumentSequence.registerAction(
    'initializeSequence',
    (state, payload) => {
      const newState = { ...state };
      const { sequences } = payload;

      sequences.forEach(sequence => {
        const newStateSequence = { ...newState[sequence.sequenceName] };
        newStateSequence.sequencePrefix = sequence.sequencePrefix;
        newStateSequence.sequenceNumber =
          newState[sequence.sequenceName] &&
          newState[sequence.sequenceName].sequencePrefix ===
            sequence.sequencePrefix
            ? Math.max(
                newState[sequence.sequenceName].sequenceNumber || 0,
                sequence.sequenceNumber || 0
              )
            : 0;
        newState[sequence.sequenceName] = newStateSequence;
      });

      return newState;
    }
  );
})();
