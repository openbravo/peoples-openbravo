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
     * Returns the highest sequence number between two sequence numbers.
     *
     * @returns {number} The highest sequence number.
     */
    getHighestSequenceNumber(sequenceNumber1, sequenceNumber2) {
      if (!sequenceNumber1 && sequenceNumber1 !== 0) {
        return sequenceNumber2;
      }

      if (!sequenceNumber2 && sequenceNumber2 !== 0) {
        return sequenceNumber1;
      }

      return sequenceNumber1 > sequenceNumber2
        ? sequenceNumber1
        : sequenceNumber2;
    },

    /**
     * Returns the sequence number increased in one or zero if it is undefined.
     *
     * @returns {number} The next sequence number.
     */
    getNextSequenceNumber(sequenceNumber) {
      if (!sequenceNumber && sequenceNumber !== 0) {
        return 0;
      }

      return sequenceNumber + 1;
    },

    /**
     * Returns the sequence number increased in one or zero if it is undefined.
     *
     * @returns {number} The next sequence number.
     */
    calculateDocumentNumber(
      documentNumberPrefix,
      documentNumberPadding,
      sequenceNumber
    ) {
      return (
        documentNumberPrefix +
        '/' +
        OB.UTIL.padNumber(sequenceNumber, documentNumberPadding)
      );
    }
  });
})();
