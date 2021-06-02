/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global */

(function ReactivateQuotation() {
  OB.App.StateAPI.Ticket.registerAction(
    'reactivateQuotation',
    (ticket, payload) => {
      const newTicket = { ...ticket };
      const { user, session, date } = payload;
      const oldIdMap = {};
      newTicket.lines = ticket.lines.map(line => {
        const newLine = {
          ...line,
          id: OB.App.UUID.generate(),
          promotions: undefined
        };
        if (line.hasRelatedServices) {
          oldIdMap[line.id] = newLine.id;
        }
        return newLine;
      });
      newTicket.oldId = newTicket.id;
      newTicket.id = OB.App.UUID.generate();
      newTicket.hasbeenpaid = 'N';
      newTicket.documentNo = '';
      newTicket.isPaid = false;
      newTicket.isEditable = true;
      newTicket.createdBy = user;
      newTicket.session = session;
      newTicket.orderDate = date;
      newTicket.skipApplyPromotions = false;
      delete newTicket.creationDate;
      delete newTicket.deletedLines;
      if (newTicket.hasServices) {
        newTicket.lines.forEach(line => {
          if (line.relatedLines) {
            // eslint-disable-next-line no-param-reassign
            line.relatedLines = line.relatedLines.map(relatedLine => {
              return {
                ...relatedLine,
                orderId: newTicket.id,
                orderlineId: oldIdMap[relatedLine.orderlineId]
                  ? oldIdMap[relatedLine.orderlineId]
                  : relatedLine.orderLineId
              };
            });
          }
        });
      }
      return newTicket;
    }
  );
})();
