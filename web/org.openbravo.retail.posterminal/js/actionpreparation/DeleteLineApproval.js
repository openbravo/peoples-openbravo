/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.App.StateAPI.Ticket.deleteLine.addActionPreparation(
    async (ticket, payload) => {
      const payloadWithApprovals = await checkApprovals(ticket, payload);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    200
  );

  async function checkApprovals(ticket, payload) {
    const { lineIds } = payload;
    let newPayload = { ...payload };

    const approvalNeeded = ticket.lines.some(
      l => !l.forceDeleteLine && lineIds.includes(l.id)
    );

    if (approvalNeeded) {
      newPayload = await OB.App.Security.requestApprovalForAction(
        'OBPOS_approval.deleteLine',
        newPayload
      );
    }
    return newPayload;
  }
})();
