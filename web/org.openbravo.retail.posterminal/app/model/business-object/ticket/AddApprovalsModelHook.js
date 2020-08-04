/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * Defines a hook in Ticket model to add to the model all the approvals that might have been
 * generated in different action preparations.
 *
 * @see OB.App.Security.requestApprovalForAction
 */

OB.App.StateAPI.Ticket.addModelHook({
  generatePayload: (ticket, payload) => payload,

  hook: function addApprovalsHook(ticket, payload) {
    const newApprovals = payload.approvals;
    if (!newApprovals) {
      return ticket;
    }

    const newTicket = { ...ticket };

    const approvals = newTicket.approvals ? [...newTicket.approvals] : [];
    newTicket.approvals = approvals.concat(newApprovals);

    return newTicket;
  }
});
