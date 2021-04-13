/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Ticket action that creates a cancel ticket in the state
 */

(() => {
  OB.App.StateAPI.Ticket.registerAction(
    'createCancelTicket',
    (ticket, payload, options) => {
      const { globalState } = options;
      const newTicket = {
        ...ticket,
        id: OB.App.UUID.generate(),
        documentNo: `${ticket.documentNo}*R*`,
        isEditable: false,
        cancelLayaway: true,
        fromLayaway: ticket.isLayaway,
        isLayaway: false,
        isPaid: false,
        orderType: 3,
        posTerminal: payload.terminal.id,
        documentType: OB.App.State.Ticket.Utils.isCrossStore(ticket, payload)
          ? ticket.documentType
          : payload.terminal.terminalType.documentType,
        obposAppCashup: globalState.Cashup.id,
        orderDate: new Date(),
        timezoneOffset: new Date().getTimezoneOffset(),
        payments: [],
        nettingPayment: OB.DEC.sub(ticket.payment, ticket.grossAmount),
        paidOnCredit: false,
        paidPartiallyOnCredit: false,
        creditAmount: OB.DEC.Zero,
        canceledorder: ticket,
        lines: ticket.lines.map(line => ({
          ...line,
          id: OB.App.UUID.generate(),
          qty: -line.qty
        }))
      };

      // lines
      // me.set('forceCalculateTaxes', true);
      // me.calculateReceipt(function() {
      // me.getPrepaymentAmount(function() {

      return newTicket;
    }
  );
})();
