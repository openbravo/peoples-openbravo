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
     * Generates a document number based on given prefix and sequence number.
     *
     * @returns {number} The document number.
     */
    calculateDocumentNumber(
      sequencePrefix,
      includeSeparator,
      documentNumberPadding,
      sequenceNumber
    ) {
      return (
        sequencePrefix +
        (includeSeparator ? '/' : '') +
        sequenceNumber.toString().padStart(documentNumberPadding, '0')
      );
    }
  });
})();
