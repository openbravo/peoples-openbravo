/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function ReturnBlindTicketApprovalDefinition() {
  OB.App.StateAPI.Ticket.returnBlindTicket.addActionPreparation(
    async (ticket, payload) => {
      const payloadWithApprovals = await checkApprovals(ticket, payload);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    200
  );

  async function checkApprovals(ticket, payload) {
    const servicesToApprove = ticket.lines.filter(
      line => line.product.returnable && line.product.productType === 'S'
    );

    if (!servicesToApprove.length) {
      return payload;
    }

    const newPayload = await OB.App.Security.requestApprovalForAction(
      {
        approval: 'OBPOS_approval.returnService',
        message: 'OBPOS_approval.returnService',
        params: [
          servicesToApprove.map(
            line =>
              '<br>' +
              OB.I18N.getLabel('OBMOBC_Character')[1] +
              ' ' +
              line.product._identifier
          )
        ]
      },
      payload
    );

    return newPayload;
  }
})();
