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
  // FIXME: Move to global complete ticket action.
  // It should read information from Terminal (sequence prefix and padding), Ticket (sequenceName) and DocumentSequence (sequenceNumber) models
  // It should update Ticket (sequenceNumber and documentNo) and DocumentSequence (sequenceNumber) models
  OB.App.StateAPI.DocumentSequence.registerAction(
    'increaseSequence',
    (state, payload) => {
      const newState = { ...state };
      const { sequenceName } = payload;

      if (newState[sequenceName] || newState[sequenceName] === 0) {
        newState[
          sequenceName
        ] = OB.App.State.DocumentSequence.Utils.getNextSequenceNumber(
          newState[sequenceName]
        );
      }

      return newState;
    }
  );
})();
