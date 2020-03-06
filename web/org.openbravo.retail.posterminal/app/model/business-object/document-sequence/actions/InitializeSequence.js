/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Document Sequence Initialize Sequence action that updates document sequence with payload information
 */

(() => {
  OB.App.StateAPI.DocumentSequence.registerAction(
    'initializeSequence',
    (state, payload) => {
      const newState = { ...state };

      const { orderSequence, returnSequence, quotationSequence } = payload;

      newState.orderSequence = OB.App.State.DocumentSequence.Utils.getHighestSequenceNumber(
        newState.orderSequence,
        orderSequence
      );
      newState.returnSequence = OB.App.State.DocumentSequence.Utils.getHighestSequenceNumber(
        newState.returnSequence,
        returnSequence
      );
      newState.quotationSequence = OB.App.State.DocumentSequence.Utils.getHighestSequenceNumber(
        newState.quotationSequence,
        quotationSequence
      );

      return newState;
    }
  );
})();
