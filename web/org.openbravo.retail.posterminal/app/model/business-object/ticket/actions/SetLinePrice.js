/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global lodash */

(function SetLinePriceDefinition() {
  OB.App.StateAPI.Ticket.registerAction('setLinePrice', (ticket, payload) => {
    const newTicket = { ...ticket };
    const { lineIds, price, reason } = payload;

    newTicket.lines = newTicket.lines.map(l => {
      if (!lineIds.includes(l.id)) {
        return l;
      }

      const listPrice = newTicket.priceIncludesTax
        ? l.grossListPrice
        : l.netListPrice;
      const newLine = {
        ...l,
        baseGrossUnitPrice: newTicket.priceIncludesTax ? price : undefined,
        baseNetUnitPrice: newTicket.priceIncludesTax ? undefined : price,
        discountPercentage: listPrice
          ? OB.DEC.toNumber(
              OB.DEC.toBigDecimal(listPrice)
                .subtract(new BigDecimal(price.toString()))
                .multiply(new BigDecimal('100'))
                .divide(
                  OB.DEC.toBigDecimal(listPrice),
                  2,
                  BigDecimal.prototype.ROUND_HALF_UP
                )
            )
          : OB.DEC.Zero
      };

      if (
        newTicket.deliveryPaymentMode === 'PD' &&
        newLine.product.obrdmIsdeliveryservice
      ) {
        newLine.obrdmAmttopayindelivery = OB.DEC.mul(price, newLine.qty);
        newLine.baseGrossUnitPrice = newTicket.priceIncludesTax ? 0 : undefined;
        newLine.baseNetUnitPrice = newTicket.priceIncludesTax ? undefined : 0;
      }

      if (reason) {
        newLine.oBPOSPriceModificationReason = reason;
      } else {
        delete newLine.oBPOSPriceModificationReason;
      }

      return newLine;
    });

    return newTicket;
  });

  function checkParameters(ticket, lineIds, price) {
    if (lineIds === undefined) {
      throw new Error('lineIds parameter is mandatory');
    }

    if (!(lineIds instanceof Array)) {
      throw new Error('lineIds parameter must be an array of Ids');
    }

    const ticketLineIds = ticket.lines.map(l => l.id);
    const notPresentLineIds = lineIds.filter(
      lid => !ticketLineIds.includes(lid)
    );
    if (notPresentLineIds.length !== 0) {
      throw new Error(`not found lineIds: [${notPresentLineIds.join(',')}]`);
    }

    if (price === undefined) {
      throw new Error('price parameter is mandatory');
    }

    if (!lodash.isNumber(price)) {
      throw new Error(`price is not numeric: ${price}`);
    }

    if (price < 0) {
      throw new Error('Cannot set price less than 0');
    }

    const lines = ticket.lines.filter(l => lineIds.includes(l.id));
    if (
      ticket.deliveryPaymentMode === 'PD' &&
      price === 0 &&
      lines.some(
        l => l.product.obrdmIsdeliveryservice && l.obrdmAmttopayindelivery !== 0
      )
    ) {
      // Covers the case price being reset by order backbone listener, this check should be removed
      // once backbone ticket disappers (see test DeliveryRates_PayInDelivery_SingleDeliveryService)
      throw new OB.App.Class.ActionSilentlyCanceled();
    }
  }

  function checkRestrictions(ticket, lines, price) {
    OB.App.State.Ticket.Utils.checkIsEditable(ticket);

    if (
      lines.some(
        l =>
          !l.product.obposEditablePrice || l.product.isEditablePrice === false
      )
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_modalNoEditableLineBody'
      });
    }

    if (lines.some(l => l.replacedorderline && l.qty < 0)) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_CancelReplaceReturnPriceChange'
      });
    }

    // in a verified return it's only allowed to change price with permissions
    // and even with permissions only to decrease the price
    const canModifyVerifiedReturn =
      !ticket.isPaid &&
      OB.App.Security.hasPermission('OBPOS_ModifyPriceVerifiedReturns');

    const priceProperty = ticket.priceIncludesTax
      ? 'baseGrossUnitPrice'
      : 'baseNetUnitPrice';
    if (
      lines.some(
        l =>
          !l.isEditable &&
          !(
            canModifyVerifiedReturn &&
            l.originalDocumentNo &&
            price < l[priceProperty]
          )
      )
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorMsg: 'OBPOS_CannotChangePrice'
      });
    }
  }

  OB.App.StateAPI.Ticket.setLinePrice.addActionPreparation(
    async (ticket, payload) => {
      const { price, lineIds, ignoreValidations } = payload;

      checkParameters(ticket, lineIds, price);

      const lines = ticket.lines.filter(l => lineIds.includes(l.id));

      if (!ignoreValidations) {
        checkRestrictions(ticket, lines, price);
      }

      return payload;
    }
  );
})();