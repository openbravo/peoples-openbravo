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

      const rejectQuotationMessage = OB.App.State.Messages.Utils.createNewMessage(
        'Reject Quotation',
        'org.openbravo.retail.posterminal.QuotationsReject',
        {
          id: state.Ticket.id,
          orderid: state.Ticket.id,
          rejectReasonId
        }
      );
      newState.Messages = [...newState.Messages, rejectQuotationMessage];
      return newState;
    }
  });
})();
