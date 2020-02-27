/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Document Sequence utility functions
 */

(() => {
  OB.App.StateAPI.DocumentSequence.registerUtilityFunctions({
    /**
     * Returns the highest sequence between two sequences.
     *
     * @returns {number} The highest sequence.
     */
    getHighestSequence(sequence1, sequence2) {
      if (!sequence1) {
        return sequence2;
      }

      if (!sequence2) {
        return sequence1;
      }

      return sequence1 > sequence2 ? sequence1 : sequence2;
    }
  });
})();
