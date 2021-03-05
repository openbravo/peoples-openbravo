/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function ReturnTicketDefinition() {
  OB.App.StateAPI.Ticket.registerAction('returnTicket', (ticket, payload) => {
    let newTicket = { ...ticket };

    newTicket.lines = newTicket.lines.map(line => {
      const newLine = { ...line };
      newLine.qty = -newLine.qty;
      return newLine;
    });
    newTicket = OB.App.State.Ticket.Utils.updateTicketType(newTicket, payload);

    // FIXME: add service approval -> order.setOrderType()
    // FIXME: check line.promotions.obdiscAllowinnegativelines -> order.returnLine()

    return newTicket;
  });

  OB.App.StateAPI.Ticket.returnTicket.addActionPreparation(
    async (ticket, payload) => {
      checkTicketRestrictions(ticket);
      ticket.lines.forEach(line => {
        checkTicketLineRestrictions(ticket, line);
      });
      return payload;
    }
  );

  function checkTicketRestrictions(ticket) {
    checkIsEditable(ticket);
  }

  function checkIsEditable(ticket) {
    if (ticket.isEditable === false) {
      throw new OB.App.Class.ActionCanceled({
        title: 'OBPOS_modalNoEditableHeader',
        errorConfirmation: 'OBPOS_modalNoEditableBody'
      });
    }
  }

  function checkTicketLineRestrictions(ticket, line) {
    checkReturnable(ticket, line);
  }

  function checkReturnable(ticket, line) {
    if (!line.product.returnable) {
      throw new OB.App.Class.ActionCanceled({
        title: 'OBPOS_UnreturnableProduct',
        errorConfirmation: 'OBPOS_UnreturnableProductMessage',
        // eslint-disable-next-line no-underscore-dangle
        messageParams: [line.product._identifier]
      });
    }
  }
})();
