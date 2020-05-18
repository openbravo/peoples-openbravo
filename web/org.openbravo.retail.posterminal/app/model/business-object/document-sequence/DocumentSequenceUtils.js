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
     * FIXME: Move to TicketUtils
     * Checks whether a ticket is a return or a sale.
     *
     * @returns {boolean} true in case the ticket is a return, false in case it is a sale.
     */
    isReturnTicket(ticket, salesWithOneLineNegativeAsReturns) {
      if (!ticket.lines) {
        return false;
      }

      const negativeLines = ticket.lines.filter(line => line.qty < 0).length;
      return (
        negativeLines === ticket.lines.length ||
        (negativeLines > 0 && salesWithOneLineNegativeAsReturns)
      );
    },
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
      const isReturnTicket = OB.App.State.DocumentSequence.Utils.isReturnTicket(
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
      const isReturnTicket = OB.App.State.DocumentSequence.Utils.isReturnTicket(
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
    },

    /**
     * Generates a document number for given ticket increasing the corresponding sequence.
     *
     * @returns {object} The new state of Ticket and DocumentSequence after document number generation.
     */
    generateTicketDocumentSequence(
      ticket,
      documentSequence,
      returnSequencePrefix,
      quotationSequencePrefix,
      fullReturnInvoiceSequencePrefix,
      simplifiedReturnInvoiceSequencePrefix,
      documentNumberSeperator,
      documentNumberPadding,
      salesWithOneLineNegativeAsReturns
    ) {
      if (ticket.documentNo) {
        return { ticket, documentSequence };
      }

      const sequenceName = ticket.isInvoice
        ? OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
            ticket,
            fullReturnInvoiceSequencePrefix,
            simplifiedReturnInvoiceSequencePrefix,
            salesWithOneLineNegativeAsReturns
          )
        : OB.App.State.DocumentSequence.Utils.getOrderSequenceName(
            ticket,
            returnSequencePrefix,
            quotationSequencePrefix,
            salesWithOneLineNegativeAsReturns
          );
      const newDocumentSequence = OB.App.State.DocumentSequence.Utils.increaseSequence(
        documentSequence,
        sequenceName
      );
      const { sequencePrefix, sequenceNumber } = newDocumentSequence[
        sequenceName
      ];
      const newTicket = { ...ticket };
      newTicket.obposSequencename = sequenceName;
      newTicket.obposSequencenumber = sequenceNumber;
      newTicket.documentNo = OB.App.State.DocumentSequence.Utils.calculateDocumentNumber(
        sequencePrefix,
        documentNumberSeperator,
        documentNumberPadding,
        sequenceNumber
      );

      return { ticket: newTicket, documentSequence: newDocumentSequence };
    }
  });
})();
