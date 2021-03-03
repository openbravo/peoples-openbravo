/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function DeleteLineDefinition() {
  OB.App.StateAPI.Ticket.registerAction('deleteLine', (ticket, payload) => {
    const { lineIds } = payload;
    const newTicket = { ...ticket };

    // Check if it is necessary to restore the tax category of related products
    newTicket.lines = restoreTaxCategoryOfRelatedProducts(newTicket, lineIds);

    const linesToDelete = [...lineIds, ...getRelatedServices(ticket, lineIds)];
    newTicket.lines = newTicket.lines.filter(
      l => !linesToDelete.includes(l.id)
    );

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

  function restoreTaxCategoryOfRelatedProducts(ticket, lineIds) {
    if (!ticket.hasServices) {
      return ticket.lines;
    }

    const relatedLinesOfDeletedLines = ticket.lines
      .filter(l => lineIds.includes(l.id) && l.product.productServiceLinked)
      .map(l => l.relatedLines)
      .flat();

    if (relatedLinesOfDeletedLines.length === 0) {
      return ticket.lines;
    }

    return ticket.lines.map(l => {
      const relatedLine = relatedLinesOfDeletedLines.some(
        rl => l.id === rl.orderlineId
      );
      const price = l.previousPrice || l.previousBaseGrossUnitPrice;
      const { previousTaxCategory } = l.product;
      if (relatedLine && price && previousTaxCategory) {
        return {
          ...l,
          baseGrossUnitPrice: price,
          previousBaseGrossUnitPrice: null,
          product: {
            ...l.product,
            taxCategory: previousTaxCategory,
            previousTaxCategory: null
          }
        };
      }
      return l;
    });
  }

  function getRelatedServices(ticket, lineIds) {
    if (!ticket.hasServices) {
      return [];
    }

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

    return relatedLinesToRemove;
  }

  function getDeletedLinesToSave(ticket, removedLineIds) {
    return ticket.lines
      .filter(l => removedLineIds.includes(l.id))
      .map(l => {
        const deletedLine = {
          ...l,
          obposQtyDeleted: l.qty,
          obposIsDeleted: true,
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
    checkDeliveryQtyInCancelAndReplace(ticket, payload);
    validateServices(ticket, payload);
  }

  function checkIsEditable(ticket) {
    if (ticket.isEditable === false) {
      throw new OB.App.Class.ActionCanceled({
        title: 'OBPOS_modalNoEditableHeader',
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

  function checkDeliveryQtyInCancelAndReplace(ticket, payload) {
    const { lineIds } = payload;
    const deliveredQuantity = ticket.lines.find(
      l => l.deliveredQuantity && lineIds.includes(l.id)
    );
    if (ticket.replacedorder && deliveredQuantity) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_CancelReplaceDeleteLine'
      });
    }
  }

  function validateServices(ticket, payload) {
    if (!ticket.hasServices) {
      return;
    }

    const { lineIds } = payload;
    const unGroupedServiceLines = ticket.lines.filter(
      l =>
        lineIds.includes(l.id) &&
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
      const product = ticket.lines.find(l => relatedLinesIds.includes(l.id));
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
})();
