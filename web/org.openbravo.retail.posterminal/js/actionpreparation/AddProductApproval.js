/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.App.StateAPI.Ticket.addProduct.addActionPreparation(
    async (ticket, payload) => {
      const newPayload = { ...payload };
      const payloadWithApprovals = await checkApprovals(ticket, newPayload);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    400
  );

  async function checkApprovals(ticket, payload) {
    const { products } = payload;
    let newPayload = { ...payload };

    const linesWithQuantityZero = products.some(pi => {
      const lineToEdit = OB.App.State.Ticket.Utils.getLineToEdit(ticket, pi);
      const qty = lineToEdit ? lineToEdit.qty + pi.qty : pi.qty;
      return qty === 0;
    });

    if (linesWithQuantityZero) {
      newPayload = await OB.App.Security.requestApprovalForAction(
        'OBPOS_approval.deleteLine',
        newPayload
      );
    }

    const servicesWithReturnApproval = products.filter(p => {
      const line = p.options.line
        ? ticket.lines.find(l => l.id === p.options.line)
        : null;
      return (
        (line ? line.qty + p.qty : p.qty) < 0 &&
        p.product.productType === 'S' &&
        !p.product.ignoreReturnApproval
      );
    });

    if (servicesWithReturnApproval.length === 0) {
      const service = findServiceForNegativeProduct(ticket, products);
      if (service) {
        servicesWithReturnApproval.push(service);
      }
    }

    if (servicesWithReturnApproval.length > 0) {
      const separator = `<br>${OB.App.SpecialCharacters.bullet()}`;
      const servicesToApprove = `
        ${separator} 
        ${servicesWithReturnApproval
          // eslint-disable-next-line no-underscore-dangle
          .map(p => p._identifier)
          .join(separator)}`;
      newPayload = await OB.App.Security.requestApprovalForAction(
        {
          approvalType: 'OBPOS_approval.returnService',
          message: 'OBPOS_approval.returnService',
          params: [servicesToApprove]
        },
        payload
      );
    }
    return newPayload;
  }

  function findServiceForNegativeProduct(ticket, products) {
    return products.find(pi => {
      const line = OB.App.State.Ticket.Utils.getLineToEdit(ticket, pi);
      let relatedLines = pi.attrs.relatedLines || [];
      if (line && line.relatedLines) {
        relatedLines = OB.App.ArrayUtils.union(relatedLines, line.relatedLines);
      }

      if (relatedLines.length === 0 || (line && line.originalOrderLineId)) {
        return false;
      }

      let newqtyminus = ticket.lines
        .filter(
          l => l.qty < 0 && relatedLines.some(rl => rl.orderlineId === l.id)
        )
        .reduce((t, l) => t + l.qty, 0);

      if (pi.product.quantityRule === 'UQ') {
        newqtyminus = newqtyminus ? -1 : 0;
      }

      const lineQty = line ? line.qty + pi.qty : pi.qty;
      return newqtyminus && lineQty > 0;
    });
  }
})();
