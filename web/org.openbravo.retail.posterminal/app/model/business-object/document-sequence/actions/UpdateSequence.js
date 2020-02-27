/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Document Sequence Updates Sequence action that updates document sequence with payload information
 */

(() => {
  OB.App.StateAPI.DocumentSequence.registerAction(
    'updateSequence',
    (state, payload) => {
      const newState = { ...state };

      const {
        newOrderSequence,
        newReturnSequence,
        newQuotationSequence
      } = payload;

      newState.orderSequence = OB.App.State.DocumentSequence.Utils.getHighestSequence(
        newState.orderSequence,
        newOrderSequence
      );
      newState.returnSequence = OB.App.State.DocumentSequence.Utils.getHighestSequence(
        newState.returnSequence,
        newReturnSequence
      );
      newState.quotationSequence = OB.App.State.DocumentSequence.Utils.getHighestSequence(
        newState.quotationSequence,
        newQuotationSequence
      );

      return newState;
    }
  );
})();
