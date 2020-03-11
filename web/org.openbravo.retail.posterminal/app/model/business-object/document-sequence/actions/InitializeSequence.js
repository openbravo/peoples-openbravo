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
 */

(() => {
  OB.App.StateAPI.DocumentSequence.registerAction(
    'initializeSequence',
    (state, payload) => {
      const newState = { ...state };
      const { sequences } = payload;

      sequences.forEach(sequence => {
        newState[
          sequence.sequenceName
        ] = OB.App.State.DocumentSequence.Utils.getHighestSequenceNumber(
          newState[sequence.sequenceName],
          sequence.sequenceNumber
        );
      });

      return newState;
    }
  );
})();
