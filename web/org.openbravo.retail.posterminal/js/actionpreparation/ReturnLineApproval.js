/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function ReturnLineApprovalDefinition() {
  OB.App.StateAPI.Ticket.returnLine.addActionPreparation(
    async (ticket, payload) => {
      const payloadWithApprovals = await checkApprovals(ticket, payload);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    200
  );

  async function checkApprovals(ticket, payload) {
    const servicesToApprove = payload.lineIds.flatMap(function(lineId) {
      const line = ticket.lines.find(l => l.id === lineId);
      if (!line.product.returnable || line.qty < 0) {
        return [];
      }

      if (line.product.productType === 'S') {
        const selectedRelatedProduct =
          !line.relatedLines ||
          line.relatedLines.some(relatedLine => {
            payload.lineIds.includes(relatedLine.orderlineId);
          });
        if (selectedRelatedProduct) {
          return line;
        }
      } else {
        const notSelectedRelatedService = ticket.lines.find(
          l =>
            !payload.lineIds.includes(l.id) &&
            l.relatedLines &&
            l.relatedLines.some(
              relatedLine => relatedLine.orderlineId === line.id
            )
        );
        if (notSelectedRelatedService && notSelectedRelatedService.qty > 0) {
          return notSelectedRelatedService;
        }
      }

      return [];
    });

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
