/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Declares a Ticket action that creates converts the actual Ticket object
 * into a Sales Quotation
 */
OB.App.StateAPI.Ticket.registerAction(
  'convertTicketIntoQuotation',
  (ticket, payload) => {
    const newTicket = { ...ticket };
    newTicket.isQuotation = true;
    newTicket.fullInvoice = false;
    newTicket.generateInvoice = false;
    newTicket.orderType = 0;
    newTicket.documentType =
      payload.terminal.terminalType.documentTypeForQuotations;
    return newTicket;
  }
);
