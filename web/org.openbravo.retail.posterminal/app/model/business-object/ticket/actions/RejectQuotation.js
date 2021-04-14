/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function RejectQuotation() {
  OB.App.StateAPI.Global.registerActions({
    rejectQuotation(state, payload) {
      const newState = { ...state };
      const { rejectReasonId } = payload;
      let newTicketList = [...newState.TicketList];
      let newTicket = { ...newState.Ticket };

      const rejectQuotationMessage = OB.App.State.Messages.Utils.createNewMessage(
        'OBPOS_Order',
        'org.openbravo.retail.posterminal.QuotationsReject',
        {
          id: state.Ticket.id,
          orderid: state.Ticket.id,
          rejectReasonId
        },
        {
          ...payload.extraProperties,
          name: 'OBPOS_RejectQuotation'
        }
      );
      newState.Messages = [...newState.Messages, rejectQuotationMessage];

      // TicketList update
      ({
        ticketList: newTicketList,
        ticket: newTicket
      } = OB.App.State.TicketList.Utils.removeTicket(
        newTicketList,
        newTicket,
        payload
      ));

      newState.TicketList = newTicketList;
      newState.Ticket = newTicket;
      return newState;
    }
  });
})();
