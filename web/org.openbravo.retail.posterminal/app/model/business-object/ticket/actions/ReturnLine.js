/*
 ************************************************************************************
 * Copyright (C) 2021-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function ReturnLineDefinition() {
  OB.App.StateAPI.Ticket.registerAction('returnLine', (ticket, payload) => {
    const { lineIds } = payload;
    let newTicket = { ...ticket };

    newTicket.lines = newTicket.lines
      // Change the sign of every returned line
      .map(line => {
        if (!lineIds.includes(line.id)) {
          return line;
        }

        const newLine = { ...line };
        newLine.qty = -newLine.qty;
        return newLine;
      })
      // Merge lines if possible after changing the sign
      .reduce((accumulator, line) => {
        if (
          line.qty < 0 ||
          !line.product ||
          !line.product.groupProduct ||
          line.splitline ||
          line.product.productType === 'S'
        ) {
          return [...accumulator, line];
        }

        const lineWithSameProduct = accumulator.find(
          otherLine =>
            otherLine.product.id === line.product.id &&
            otherLine.baseGrossUnitPrice === line.baseGrossUnitPrice &&
            otherLine.baseNetUnitPrice === line.baseNetUnitPrice &&
            otherLine.qty > 0 &&
            line.qty > 0
        );
        if (!lineWithSameProduct) {
          return [...accumulator, line];
        }

        return accumulator.map(mergedLine => {
          if (mergedLine.id !== lineWithSameProduct.id) {
            return mergedLine;
          }

          const newMergedLine = { ...mergedLine };
          newMergedLine.qty += line.qty;

          return newMergedLine;
        });
      }, []);

    // Change ticket type if OBPOS_NotAllowSalesWithReturn preference is defined
    if (payload.preferences.notAllowSalesWithReturn) {
      newTicket = OB.App.State.Ticket.Utils.updateTicketType(
        newTicket,
        payload
      );
    }

    return newTicket;
  });

  OB.App.StateAPI.Ticket.returnLine.addActionPreparation(
    async (ticket, payload) => {
      OB.App.State.Ticket.Utils.checkIsEditable(ticket);
      checkSaleWithReturn(ticket, payload);
      checkReturnLayaway(ticket);

      payload.lineIds.forEach(lineId => {
        const line = ticket.lines.find(l => l.id === lineId);
        checkCancelReplace(ticket, line);
        checkReturnable(ticket, line, payload);
      });

      await checkStock(ticket, payload);

      return payload;
    }
  );

  function checkSaleWithReturn(ticket, payload) {
    if (
      OB.App.Security.hasPermission('OBPOS_NotAllowSalesWithReturn') &&
      ticket.lines.some(
        line => line.qty > 0 && !payload.lineIds.includes(line.id)
      )
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorMsg: 'OBPOS_MsgCannotAddNegative'
      });
    }
  }

  function checkReturnLayaway(ticket) {
    if (
      !OB.App.Security.hasPermission('OBPOS_AllowLayawaysNegativeLines') &&
      OB.App.State.Ticket.Utils.isLayaway(ticket)
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorMsg: 'OBPOS_layawaysOrdersWithReturnsNotAllowed'
      });
    }
  }

  function checkCancelReplace(ticket, line) {
    if (ticket.replacedorder && line.remainingQuantity) {
      throw new OB.App.Class.ActionCanceled({
        errorMsg: 'OBPOS_CancelReplaceReturnLines'
      });
    }
  }

  function checkReturnable(ticket, line, payload) {
    if (line.qty > 0 && !line.product.returnable) {
      throw new OB.App.Class.ActionCanceled({
        title: 'OBPOS_UnreturnableProduct',
        errorConfirmation: 'OBPOS_UnreturnableProductMessage',
        // eslint-disable-next-line no-underscore-dangle
        messageParams: [line.product._identifier]
      });
    }

    if (line.product.productType === 'S') {
      const notSelectedRelatedProduct =
        line.relatedLines &&
        line.relatedLines.some(
          relatedLine => !payload.lineIds.includes(relatedLine.orderlineId)
        );
      if (notSelectedRelatedProduct) {
        throw new OB.App.Class.ActionCanceled({
          title: 'OBPOS_UnreturnableRelatedService',
          errorConfirmation: 'OBPOS_NotProductSelectedToReturn',
          // eslint-disable-next-line no-underscore-dangle
          messageParams: [line.product._identifier]
        });
      }
    } else {
      const notReturnableRelatedService = ticket.lines.find(
        l =>
          !payload.lineIds.includes(l.id) &&
          !l.product.returnable &&
          l.relatedLines &&
          l.relatedLines.some(
            relatedLine => relatedLine.orderlineId === line.id
          )
      );
      if (notReturnableRelatedService) {
        throw new OB.App.Class.ActionCanceled({
          title: 'OBPOS_UnreturnableRelatedService',
          errorConfirmation: 'OBPOS_UnreturnableRelatedServiceMessage',
          messageParams: [
            // eslint-disable-next-line no-underscore-dangle
            line.product._identifier,
            // eslint-disable-next-line no-underscore-dangle
            notReturnableRelatedService.product._identifier
          ]
        });
      }
    }
  }

  async function checkStock(ticket, payload) {
    for (let i = 0; i < payload.lineIds.length; i += 1) {
      const lineId = payload.lineIds[i];
      const line = ticket.lines.find(l => l.id === lineId);

      if (line.qty > 0) {
        return;
      }

      // eslint-disable-next-line no-await-in-loop
      const hasStock = await OB.App.StockChecker.hasStock(
        line.product,
        -line.qty,
        { ticket, lineId, options: {}, attrs: {} }
      );

      if (!hasStock) {
        throw new OB.App.Class.ActionSilentlyCanceled(
          `Return line canceled: there is no stock of product ${line.product.id}`
        );
      }
    }
  }
})();
