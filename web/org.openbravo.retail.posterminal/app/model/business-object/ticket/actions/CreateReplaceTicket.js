/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Ticket action that creates a replace ticket in the state
 */

(() => {
  OB.App.StateAPI.Ticket.registerAction(
    'createReplaceTicket',
    (ticket, payload) => {
      const newTicket = {
        ...ticket,
        id: OB.App.UUID.generate(),
        replacedorder: ticket.id,
        orderid: undefined,
        orderDate: new Date(),
        creationDate: null,
        createdBy: payload.orgUserId,
        session: payload.session,
        hasbeenpaid: 'N',
        isPaid: false,
        isEditable: true,
        doCancelAndReplace: true,
        isLayaway: false,
        orderType:
          ticket.isLayaway ||
          (payload.terminal.terminalType.layawayorder &&
            !ticket.lines.some(line => line.deliveredQuantity > 0))
            ? 2
            : ticket.orderType,
        invoiceCreated: undefined,
        documentNo: OB.App.State.DocumentSequence.Utils.calculateReplaceDocumentNumber(
          ticket.documentNo,
          {
            replaceNumberSeparator: payload.terminal.cancelAndReplaceSeparator,
            documentNumberPadding: payload.terminal.documentnoPadding
          }
        ),
        replacedorder_documentNo: ticket.documentNo,
        negativeDocNo: `${ticket.documentNo}*R*`,
        documentType: OB.App.State.Ticket.Utils.isCrossStore(ticket, payload)
          ? ticket.documentType
          : payload.terminal.terminalType.documentType,
        cashVAT: payload.terminal.cashVat,
        posTerminal: payload.terminal.id,
        salesRepresentative:
          ticket.salesRepresentative || payload.context.isSalesRepresentative
            ? payload.context.user.id
            : undefined,
        paidOnCredit: false,
        paidPartiallyOnCredit: false,
        creditAmount: OB.DEC.Zero,
        canceledorder: ticket
      };

      newTicket.lines = ticket.lines
        .map(line => {
          const newLine = {
            ...line,
            id: OB.App.UUID.generate(),
            replacedorderline: line.id,
            invoicedQuantity: undefined,
            grossUnitPrice: undefined,
            lineGrossAmount: undefined,
            obposIspaid: false,
            obposCanbedelivered:
              line.obposCanbedelivered || line.deliveredQuantity === line.qty
          };

          const deferredLine = (payload.deferredLines || []).find(
            l => l.id === line.id
          );
          if (deferredLine) {
            newLine.isEditable = false;
            newLine.isDeletable = false;
          }

          return newLine;
        })
        .map((line, index, lines) => {
          // Update or remove related lines
          if (!ticket.hasServices || !line.relatedLines) {
            return line;
          }

          const newLine = { ...line };
          newLine.relatedLines = line.relatedLines.map(relatedLine => {
            const updatedLine = lines.find(
              l => l.replacedorderline === relatedLine.orderlineId
            );
            return {
              ...relatedLine,
              orderId: newTicket.id,
              orderlineId: updatedLine
                ? updatedLine.id
                : relatedLine.orderlineId
            };
          });
          return newLine;
        });

      return newTicket;
    }
  );

  OB.App.StateAPI.Ticket.createReplaceTicket.addActionPreparation(
    async (ticket, payload) => {
      await OB.App.State.Ticket.Utils.checkDraftPayments(ticket);
      const newPayload = await OB.App.State.Ticket.Utils.checkTicketCanceled(
        ticket,
        { ...payload, actionType: 'R', checkNotEditableLines: true }
      );
      return newPayload;
    }
  );
})();
