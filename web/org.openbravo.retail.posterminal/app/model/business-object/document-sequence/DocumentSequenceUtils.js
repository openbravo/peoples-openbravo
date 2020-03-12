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
     * Generates a document number based on given prefix and sequence number.
     *
     * @returns {number} The document number.
     */
    calculateDocumentNumber(
      documentNumberPrefix,
      includeSeparator,
      documentNumberPadding,
      sequenceNumber
    ) {
      return (
        documentNumberPrefix +
        (includeSeparator ? '/' : '') +
        OB.UTIL.padNumber(sequenceNumber, documentNumberPadding)
      );
    }
  });
})();
