/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* eslint-disable no-use-before-define */

(function DeleteLineDefinition() {
  window.newDeleteLine = true; // TODO: remove this testing code

  OB.App.StateAPI.Ticket.registerAction('deleteLine', (ticket, payload) => {
    const { lineIds } = payload;

    const linesToDelete = [...lineIds, ...getRelatedServices(ticket, lineIds)];

    const newTicket = {
      ...ticket,
      lines: ticket.lines.filter(l => !linesToDelete.includes(l.id))
    };

    const conf = payload.config || {};

    if (conf.saveRemoval) {
      newTicket.deletedLines = (newTicket.deletedLines || []).concat(
        getDeletedLinesToSave(ticket, linesToDelete)
      );
    }

    return newTicket;
  });

  // prepares the initial payload information and checks the restrictions
  OB.App.StateAPI.Ticket.deleteLine.addActionPreparation(
    async (ticket, payload) => {
      let newPayload = { ...payload };

      newPayload = prepareConfiguration(newPayload);
      checkRestrictions(ticket, newPayload);
      return newPayload;
    },
    async (ticket, payload) => payload,
    100
  );

  // check the approvals
  OB.App.StateAPI.Ticket.deleteLine.addActionPreparation(
    async (ticket, payload) => {
      const newPayload = { ...payload };
      const payloadWithApprovals = await checkApprovals(ticket, newPayload);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    200
  );

  function prepareConfiguration(payload) {
    const newPayload = {
      ...payload,
      config: {
        saveRemoval: OB.App.Security.hasPermission('OBPOS_remove_ticket')
      }
    };
    return newPayload;
  }

  function checkRestrictions(ticket, payload) {
    checkIsEditable(ticket);
    checkNonDeletableLines(ticket, payload);
    checkDeliveryQtyInCancelAndReplace(ticket);
    validateServices(ticket);
  }

  function checkIsEditable(ticket) {
    if (ticket.isEditable === false) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_modalNoEditableBody'
      });
    }
  }

  function checkNonDeletableLines(ticket, payload) {
    const { lineIds } = payload;
    const nonDeletableLine = ticket.lines.find(
      l => !l.isDeletable && lineIds.includes(l.id)
    );
    if (nonDeletableLine) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_NotDeletableLine',
        // eslint-disable-next-line no-underscore-dangle
        messageParams: [nonDeletableLine.product._identifier]
      });
    }
  }

  function checkDeliveryQtyInCancelAndReplace(ticket) {
    if (ticket.replacedorder && ticket.lines.some(l => l.deliveredQuantity)) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_CancelReplaceDeleteLine'
      });
    }
  }

  function validateServices(ticket) {
    if (!ticket.hasServices) {
      return;
    }

    const unGroupedServiceLines = ticket.lines.filter(
      l =>
        l.product.productType === 'S' &&
        l.product.quantityRule === 'PP' &&
        !l.groupService &&
        l.relatedLines &&
        l.relatedLines.length > 0 &&
        l.isEditable
    );

    if (unGroupedServiceLines.length === 0) {
      return;
    }

    const uniqueServices = unGroupedServiceLines.reduce(
      (unique, item) =>
        unique.some(
          ul =>
            ul.product.id + ul.relatedLines[0].orderlineId ===
            item.product.id + item.relatedLines[0].orderlineId
        )
          ? unique
          : [...unique, item],
      []
    );

    const getServiceQty = service => {
      return unGroupedServiceLines.filter(
        l =>
          l.product.id === service.product.id &&
          l.relatedLines[0].orderlineId === service.relatedLines[0].orderlineId
      ).length;
    };

    const getProductQty = service => {
      const relatedLinesIds = service.relatedLines.map(rl => rl.orderlineId);
      const product = ticket.lines.find(l => relatedLinesIds.includes(l));
      return product ? product.qty : undefined;
    };

    const canNotDelete = uniqueServices.some(us => {
      const serviceQty = getServiceQty(us);
      const productQty = getProductQty(us);
      return productQty && productQty !== serviceQty;
    });

    if (canNotDelete) {
      throw new OB.App.Class.ActionCanceled({
        title: 'OBPOS_LineCanNotBeDeleted',
        errorConfirmation: 'OBPOS_AllServiceLineMustSelectToDelete'
      });
    }
  }

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

  function getRelatedServices(ticket, lineIds) {
    if (!ticket.hasServices) {
      // TODO: is this check necessary?
      return [];
    }

    // TODO: Check if it is necessary to restore the tax category of related products

    let relatedLinesToRemove = ticket.lines
      .filter(
        l =>
          l.relatedLines &&
          !lineIds.includes(l.id) &&
          l.relatedLines.some(rl => lineIds.includes(rl.orderlineId))
      )
      .map(l => l.id);

    if (relatedLinesToRemove.length > 0) {
      // check again for related lines of the related just added
      relatedLinesToRemove = relatedLinesToRemove.concat(
        getRelatedServices(ticket, [...lineIds, ...relatedLinesToRemove])
      );
    }
    // TODO: handle lines related to serveral

    return relatedLinesToRemove;
  }

  function getDeletedLinesToSave(ticket, removedLineIds) {
    return ticket.lines
      .filter(l => removedLineIds.includes(l.id))
      .map(l => {
        // TODO: check the correct properties to reset
        const deletedLine = {
          ...l,
          obposQtyDeleted: l.qty,
          qty: 0,
          netUnitPrice: 0,
          grossUnitPrice: 0,
          netUnitAmount: 0,
          grossUnitAmount: 0,
          taxes: { ...l.taxes },
          promotions: []
        };

        Object.keys(deletedLine.taxes).forEach(k => {
          deletedLine.taxes[k] = {
            ...deletedLine.taxes[k],
            amount: 0,
            net: 0
          };
        });
        return deletedLine;
      });
  }
})();
