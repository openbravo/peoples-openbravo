/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Declares a global action that creates converts the actual state.Ticket object
 * into a Sales Quotation
 */
OB.App.StateAPI.Global.registerAction('convertTicketIntoQuotation', state => {
  const newState = { ...state };
  newState.Ticket = { ...state.Ticket };

  const ticket = newState.Ticket;
  ticket.isQuotation = true;
  ticket.fullInvoice = false;
  ticket.generateInvoice = false;
  ticket.orderType = 0;
  ticket.documentType = OB.App.TerminalProperty.get(
    'terminal'
  ).terminalType.documentTypeForQuotations;
  newState.Ticket = ticket;
  return newState;
});
