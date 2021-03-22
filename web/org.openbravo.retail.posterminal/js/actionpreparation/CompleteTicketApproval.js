/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.App.StateAPI.Global.completeTicket.addActionPreparation(
    async (ticket, payload) => {
      const payloadWithApprovals = await checkApprovals(payload);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    200
  );

  OB.App.StateAPI.Global.completeMultiTicket.addActionPreparation(
    async (ticket, payload) => {
      const payloadWithApprovals = await checkApprovals(payload);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    200
  );

  OB.App.StateAPI.Global.cancelTicket.addActionPreparation(
    async (ticket, payload) => {
      const payloadWithApprovals = await checkApprovals(payload);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    200
  );

  OB.App.StateAPI.Global.replaceTicket.addActionPreparation(
    async (ticket, payload) => {
      const payloadWithApprovals = await checkApprovals(payload);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    200
  );

  async function checkApprovals(payload) {
    if (payload.checkApproval && payload.checkApproval.prepaymentUnderLimit) {
      const newPayload = await OB.App.Security.requestApprovalForAction(
        'OBPOS_approval.prepaymentUnderLimit',
        payload
      );
      return newPayload;
    }
    return payload;
  }
})();
