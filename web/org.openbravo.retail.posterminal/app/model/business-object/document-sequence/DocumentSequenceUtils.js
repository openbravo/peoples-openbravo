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
     * @param {object} documentSequence - The document sequence whose sequence will be increased
     * @param {object} payload - The calculation payload, which include:
     *             * sequenceName - Name of the sequence to increase
     *
     * @returns {number} The new document sequence.
     */
    increaseSequence(documentSequence, payload) {
      const newDocumentSequence = { ...documentSequence };

      if (newDocumentSequence[payload.sequenceName]) {
        const newStateSequence = {
          ...newDocumentSequence[payload.sequenceName]
        };
        newStateSequence.sequenceNumber =
          newDocumentSequence[payload.sequenceName].sequenceNumber + 1;
        newDocumentSequence[payload.sequenceName] = newStateSequence;
      }

      return newDocumentSequence;
    },

    /**
     * Generates a document number based on given prefix and sequence number.
     *
     * @param {object} payload - The calculation payload, which include:
     *             * sequencePrefix - Prefix of the document number
     *             * documentNumberSeparator - Character to separate prefix and suffix in document number
     *             * sequenceNumber - Suffix of the document number
     *             * documentNumberPadding - Padding to use in the the suffix of document number
     *
     * @returns {number} The document number.
     */
    calculateDocumentNumber(payload) {
      return (
        payload.sequencePrefix +
        payload.documentNumberSeparator +
        payload.sequenceNumber
          .toString()
          .padStart(payload.documentNumberPadding, '0')
      );
    },

    /**
     * Calculates the sequence name to be used by the order based on ticket properties.
     *
     * @param {object} ticket - The ticket whose order sequence name will be calculated
     * @param {object} payload - The calculation payload, which include:
     *             * terminal.returnDocNoPrefix - Return document sequence prefix
     *             * terminal.quotationDocNoPrefix - Quotation document sequence prefix
     *             * preferences.salesWithOneLineNegativeAsReturns - OBPOS_SalesWithOneLineNegativeAsReturns preference value
     *
     * @returns {string} The order sequence name.
     */
    getOrderSequenceName(ticket, payload) {
      const isReturn = OB.App.State.Ticket.Utils.isReturnSale(ticket, payload);
      if (ticket.isQuotation && payload.terminal.quotationDocNoPrefix) {
        return 'quotationslastassignednum';
      }
      if (isReturn && payload.terminal.returnDocNoPrefix) {
        return 'returnslastassignednum';
      }
      return 'lastassignednum';
    },

    /**
     * Calculates the sequence name to be used by the invoice based on ticket properties.
     *
     * @param {object} ticket - The ticket whose invoice sequence name will be calculated
     * @param {object} payload - The calculation payload, which include:
     *             * terminal.fullReturnInvoiceDocNoPrefix - Full return invoice document sequence prefix
     *             * terminal.simplifiedReturnInvoiceDocNoPrefix - Simplified return invoice document sequence prefix
     *             * preferences.salesWithOneLineNegativeAsReturns - OBPOS_SalesWithOneLineNegativeAsReturns preference value
     *
     * @returns {string} The invoice sequence name.
     */
    getInvoiceSequenceName(ticket, payload) {
      const isReturn = OB.App.State.Ticket.Utils.isReturnSale(ticket, payload);
      if (
        !ticket.fullInvoice &&
        isReturn &&
        payload.terminal.simplifiedReturnInvoiceDocNoPrefix
      ) {
        return 'simplifiedreturninvoiceslastassignednum';
      }
      if (
        ticket.fullInvoice &&
        isReturn &&
        payload.terminal.fullReturnInvoiceDocNoPrefix
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
     * @param {object} ticket - The ticket whose document sequence will be generated
     * @param {object} documentSequence - The ticket whose type will be updated
     * @param {object} payload - The calculation payload, which include:
     *             * documentNumberSeparator - Character to separate prefix and suffix in document number
     *             * terminal.returnDocNoPrefix - Return document sequence prefix
     *             * terminal.quotationDocNoPrefix - Quotation document sequence prefix
     *             * terminal.fullReturnInvoiceDocNoPrefix - Full return invoice document sequence prefix
     *             * terminal.simplifiedReturnInvoiceDocNoPrefix - Simplified return invoice document sequence prefix
     *             * terminal.documentnoPadding - Padding to use in the the suffix of document number
     *             * preferences.salesWithOneLineNegativeAsReturns - OBPOS_SalesWithOneLineNegativeAsReturns preference value
     *
     * @returns {object} The new state of Ticket and DocumentSequence after document number generation.
     */
    generateDocumentNumber(ticket, documentSequence, payload) {
      if (ticket.documentNo) {
        return { ticket, documentSequence };
      }

      const sequenceName = ticket.isInvoice
        ? OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
            ticket,
            payload
          )
        : OB.App.State.DocumentSequence.Utils.getOrderSequenceName(
            ticket,
            payload
          );
      const newDocumentSequence = OB.App.State.DocumentSequence.Utils.increaseSequence(
        documentSequence,
        { sequenceName }
      );
      const { sequencePrefix, sequenceNumber } = newDocumentSequence[
        sequenceName
      ];
      const newTicket = { ...ticket };
      newTicket.obposSequencename = sequenceName;
      newTicket.obposSequencenumber = sequenceNumber;
      newTicket.documentNo = OB.App.State.DocumentSequence.Utils.calculateDocumentNumber(
        {
          sequencePrefix,
          documentNumberSeparator: payload.documentNumberSeparator,
          documentNumberPadding: payload.terminal.documentnoPadding,
          sequenceNumber
        }
      );

      return { ticket: newTicket, documentSequence: newDocumentSequence };
    }
  });
})();
