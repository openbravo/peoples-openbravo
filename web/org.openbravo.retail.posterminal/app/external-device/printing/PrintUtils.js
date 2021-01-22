/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview contains utilities that are intended to be used mostly in print templates
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

OB.App.PrintUtils = {
  printTicketLineAmount: ticketLine => {
    if (ticketLine.priceIncludesTax) {
      return OB.App.PrintUtils.printAmount(ticketLine.baseGrossUnitAmount);
    }
    return OB.App.PrintUtils.printAmount(ticketLine.baseNetUnitAmount);
  },

  printTicketLinePrice: ticketLine => {
    if (ticketLine.priceIncludesTax) {
      return OB.App.PrintUtils.printAmount(ticketLine.baseGrossUnitPrice);
    }
    return OB.App.PrintUtils.printAmount(ticketLine.baseNetUnitPrice);
  },

  printAmount: amount => {
    return OB.I18N.formatCurrency(amount);
  },

  printQty: qty => {
    return OB.DEC.toNumber(
      OB.DEC.toBigDecimal(qty),
      OB.I18N.qtyScale()
    ).toString();
  },

  getChangeLabelFromTicket: ticket => {
    if (ticket.changePayments) {
      return ticket.changePayments.map(payment => payment.label).join(' + ');
    }
    return ticket.payments
      .filter(
        payment =>
          payment.paymentData &&
          payment.paymentData.label &&
          payment.paymentData.key === payment.kind
      )
      .map(payment => payment.paymentData.label)
      .join(' + ');
  }
};
