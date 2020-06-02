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

    const newTicket = {
      ...ticket,
      lines: ticket.lines.filter(l => !lineIds.includes(l.id))
    };

    const conf = payload.config || {};

    if (conf.saveRemoval) {
      newTicket.deletedLines = ticket.lines
        .filter(l => lineIds.includes(l.id))
        .map(l => {
          // TODO: check the correct properties to reset
          return {
            ...l,
            obposQtyDeleted: l.qty,
            qty: 0,
            grossAmount: 0,
            netAmount: 0
          };
        });
    }

    return newTicket;
  });

  OB.App.StateAPI.Ticket.deleteLine.addActionPreparation(
    async (state, payload) => {
      const ticket = state.Ticket;
      let newPayload = { ...payload };

      newPayload = prepareConfiguration(newPayload);
      newPayload = removeRelatedServices(ticket, newPayload);
      return newPayload;
    }
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
  function removeRelatedServices(ticket, payload) {
    // TODO: consider doing this in action. Note this calculations were done in trigger updateRelations in order.js (component)
    if (!ticket.hasServices) {
      // TODO: is this check necessary?
      return payload;
    }

    let newPayload = { ...payload };

    // TODO: Check if it is necessary to restore the tax category of related products

    const relatedLinesToRemove = ticket.lines
      .filter(
        l =>
          l.relatedLines &&
          !newPayload.lineIds.includes(l.id) &&
          l.relatedLines.some(rl => newPayload.lineIds.includes(rl.orderlineId))
      )
      .map(l => l.id);
    newPayload.lineIds = newPayload.lineIds.concat(relatedLinesToRemove);

    if (relatedLinesToRemove.length > 0) {
      // check again for related lines of the related just added
      newPayload = removeRelatedServices(ticket, newPayload);
    }
    // TODO: handle lines related to serveral

    return newPayload;
  }
})();
