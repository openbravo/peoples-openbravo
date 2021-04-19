/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the Ticket action that creates a cancel ticket in the state
 */

(() => {
  OB.App.StateAPI.Ticket.registerAction(
    'createCancelTicket',
    (ticket, payload, options) => {
      const { globalState } = options;
      const newTicket = {
        ...ticket,
        id: OB.App.UUID.generate(),
        documentNo: `${ticket.documentNo}*R*`,
        isEditable: false,
        cancelLayaway: true,
        fromLayaway: ticket.isLayaway,
        isLayaway: false,
        isPaid: false,
        orderType: 3,
        posTerminal: payload.terminal.id,
        documentType: OB.App.State.Ticket.Utils.isCrossStore(ticket, payload)
          ? ticket.documentType
          : payload.terminal.terminalType.documentType,
        obposAppCashup: globalState.Cashup.id,
        orderDate: new Date(),
        timezoneOffset: new Date().getTimezoneOffset(),
        payments: [],
        nettingPayment: OB.DEC.sub(ticket.payment, ticket.grossAmount),
        paidOnCredit: false,
        paidPartiallyOnCredit: false,
        creditAmount: OB.DEC.Zero,
        canceledorder: ticket
      };

      newTicket.lines = ticket.lines
        .flatMap(line => {
          // Remove negative and fully delivered lines
          if (line.qty < 0 || line.qty === line.deliveredQuantity) {
            return [];
          }

          const newLine = { ...line };
          const newQty = (line.deliveredQuantity || 0) - line.qty;
          newLine.id = OB.App.UUID.generate();
          newLine.canceledLine = line.id;
          newLine.qty = newQty;
          newLine.deliveredQuantity = undefined;
          newLine.invoicedQuantity = undefined;
          newLine.obposCanbedelivered = true;
          newLine.obposIspaid = false;

          newLine.promotions = line.promotions.map(promotion => {
            const getPromotionAmount = amount =>
              OB.DEC.mul(amount, OB.BIGDEC.div(newQty, line.qty));
            const newPromotion = { ...promotion };
            newPromotion.amt = getPromotionAmount(promotion.amt);
            newPromotion.actualAmt = getPromotionAmount(promotion.actualAmt);
            newPromotion.displayedTotalAmount = getPromotionAmount(
              promotion.displayedTotalAmount
            );
            return newPromotion;
          });

          return newLine;
        })
        .map((line, index, lines) => {
          // Update or remove related lines
          if (
            line.product.productType !== 'S' ||
            !line.product.isLinkedToProduct
          ) {
            return line;
          }

          const newLine = { ...line };
          newLine.relatedLines = line.relatedLines.flatMap(relatedLine => {
            const updatedLine = lines.find(
              l => l.canceledLine === relatedLine.orderlineId
            );
            if (updatedLine) {
              return { ...relatedLine, orderlineId: updatedLine.id };
            }
            if (!relatedLine.deferred) {
              return [];
            }
            return relatedLine;
          });

          return newLine;
        });

      return newTicket;
    }
  );

  OB.App.StateAPI.Ticket.createCancelTicket.addActionPreparation(
    async (ticket, payload) => {
      await OB.App.State.Ticket.Utils.checkTicketCanceled(ticket, {
        checkNotDeliveredDeferredServices: true
      });
      await checkDeliveredQuantity(ticket);
      return payload;
    }
  );

  async function checkDeliveredQuantity(ticket) {
    if (ticket.payment >= (ticket.deliveredQuantityAmount || 0)) {
      return;
    }
    await OB.App.View.DialogUIHandler.askConfirmation({
      title: 'OBPOS_Attention',
      message: 'OBPOS_DeliveredMoreThanPaid'
    });
  }
})();
