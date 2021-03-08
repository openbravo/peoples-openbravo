/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
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
      .map(line => {
        if (!lineIds.includes(line.id)) {
          return line;
        }

        const newLine = { ...line };
        newLine.qty = -newLine.qty;
        return newLine;
      })
      .reduce((accumulator, line) => {
        if (
          line.qty < 0 ||
          !line.product ||
          !line.product.groupProduct ||
          line.splitline
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

    if (payload.preferences.notAllowSalesWithReturn) {
      newTicket = OB.App.State.Ticket.Utils.updateTicketType(
        newTicket,
        payload
      );
    }

    // FIXME: !line.get('relatedLines'? -> returnline.js
    // FIXME: OBPOS_CheckStockReturnLine hook -> pointofsale.returnLine()
    // FIXME: add service approval -> order.setOrderType()
    // FIXME: check line.promotions.obdiscAllowinnegativelines -> order.returnLine()

    return newTicket;
  });

  OB.App.StateAPI.Ticket.returnLine.addActionPreparation(
    async (ticket, payload) => {
      // FIXME: validations needed in ReturnTicket?
      checkTicketRestrictions(ticket, payload);
      payload.lineIds.forEach(lineId => {
        const line = ticket.lines.find(l => l.id === lineId);
        checkTicketLineRestrictions(ticket, line, payload);
      });
      return payload;
    }
  );

  function checkTicketRestrictions(ticket, payload) {
    OB.App.State.Ticket.Utils.checkIsEditable(ticket);
    checkSaleWithReturn(ticket, payload);
    checkReturnLayaway(ticket);
  }

  function checkTicketLineRestrictions(ticket, line, payload) {
    checkCancelReplace(ticket, line);
    // FIXME: implement checkReturnableServices from order.js (see checkNotReturnableService from AddProduct)
    checkReturnable(ticket, line, payload);
  }

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
    if (!line.product.returnable) {
      throw new OB.App.Class.ActionCanceled({
        title: 'OBPOS_UnreturnableProduct',
        errorConfirmation: 'OBPOS_UnreturnableProductMessage',
        // eslint-disable-next-line no-underscore-dangle
        messageParams: [line.product._identifier]
      });
    }

    if (line.product.productType === 'S') {
      const notSelectedRelatedProduct = line.relatedLines.find(
        l => !payload.lineIds.includes(l.id)
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
            notReturnableRelatedService.productName
          ]
        });
      }
    }
  }
})();
