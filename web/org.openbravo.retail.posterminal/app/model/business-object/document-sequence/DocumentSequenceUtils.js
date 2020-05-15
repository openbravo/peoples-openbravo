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
     * Increases in one the sequence defined with the given sequence name.
     *
     * @returns {number} The new document sequence.
     */
    increaseSequence(documentSequence, sequenceName) {
      const newDocumentSequence = { ...documentSequence };

      if (newDocumentSequence[sequenceName]) {
        const newStateSequence = { ...newDocumentSequence[sequenceName] };
        newStateSequence.sequenceNumber =
          newDocumentSequence[sequenceName].sequenceNumber + 1;
        newDocumentSequence[sequenceName] = newStateSequence;
      }

      return newDocumentSequence;
    },

    /**
     * Generates a document number based on given prefix and sequence number.
     *
     * @returns {number} The document number.
     */
    calculateDocumentNumber(
      sequencePrefix,
      documentNumberSeparator,
      documentNumberPadding,
      sequenceNumber
    ) {
      return (
        sequencePrefix +
        documentNumberSeparator +
        sequenceNumber.toString().padStart(documentNumberPadding, '0')
      );
    },

    /**
     * Calculates the sequence name to be used by the order based on ticket properties.
     *
     * @returns {string} The order sequence name.
     */
    getOrderSequenceName(
      ticket,
      returnSequencePrefix,
      quotationSequencePrefix,
      salesWithOneLineNegativeAsReturns
    ) {
      const isReturnTicket = OB.App.State.Ticket.Utils.isReturnTicket(
        ticket,
        salesWithOneLineNegativeAsReturns
      );
      if (ticket.isQuotation && quotationSequencePrefix) {
        return 'quotationslastassignednum';
      }
      if (isReturnTicket && returnSequencePrefix) {
        return 'returnslastassignednum';
      }
      return 'lastassignednum';
    },

    /**
     * Calculates the sequence name to be used by the invoice based on ticket properties.
     *
     * @returns {string} The invoice sequence name.
     */
    getInvoiceSequenceName(
      ticket,
      fullReturnInvoiceSequencePrefix,
      simplifiedReturnInvoiceSequencePrefix,
      salesWithOneLineNegativeAsReturns
    ) {
      const isReturnTicket = OB.App.State.Ticket.Utils.isReturnTicket(
        ticket,
        salesWithOneLineNegativeAsReturns
      );
      if (
        !ticket.fullInvoice &&
        isReturnTicket &&
        simplifiedReturnInvoiceSequencePrefix
      ) {
        return 'simplifiedreturninvoiceslastassignednum';
      }
      if (
        ticket.fullInvoice &&
        isReturnTicket &&
        fullReturnInvoiceSequencePrefix
      ) {
        return 'fullreturninvoiceslastassignednum';
      }
      if (!ticket.fullInvoice) {
        return 'simplifiedinvoiceslastassignednum';
      }
      return 'fullinvoiceslastassignednum';
    }
  });
})();
