/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.App.StateAPI.Ticket.setLinePrice.addActionPreparation(
    async (ticket, payload) => {
      const { lineIds } = payload;
      const lines = ticket.lines.filter(l => lineIds.includes(l.id));
      const payloadWithApprovals = await checkApprovals(payload, lines);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    200
  );

  async function checkApprovals(payload, lines) {
    // TODO: check if this logic is correct (moved from old implementation):
    // request approval if there is at least one Item product, what has services to do with this?
    if (
      !OB.App.Security.hasPermission('OBPOS_ChangeServicePriceNeedApproval') &&
      lines.some(l => l.product.productType === 'I')
    ) {
      const newPayload = await OB.App.Security.requestApprovalForAction(
        'OBPOS_approval.setPrice',
        payload
      );
      return newPayload;
    }
    return payload;
  }
})();
