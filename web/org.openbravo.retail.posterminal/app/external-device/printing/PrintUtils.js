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
  /**
   * Returns the line amount formatted according to the current configuration
   *
   * @param ticketLine {object} - the ticket line whose amount is returned
   * @return {string} - the formatted line amount
   */
  printTicketLineAmount: ticketLine => {
    if (ticketLine.priceIncludesTax) {
      return OB.App.PrintUtils.printAmount(ticketLine.baseGrossUnitAmount);
    }
    return OB.App.PrintUtils.printAmount(ticketLine.baseNetUnitAmount);
  },

  /**
   * Returns the line price formatted according to the current configuration
   *
   * @param ticketLine {object} - the ticket line whose price is returned
   * @return {string} - the formatted line price
   */
  printTicketLinePrice: ticketLine => {
    if (ticketLine.priceIncludesTax) {
      return OB.App.PrintUtils.printAmount(ticketLine.baseGrossUnitPrice);
    }
    return OB.App.PrintUtils.printAmount(ticketLine.baseNetUnitPrice);
  },

  /**
   * Returns a formatted amount according to the current configuration
   *
   * @param amount {number} - the amount to be formatted
   * @return {string} - the formatted amount
   */
  printAmount: amount => {
    return OB.I18N.formatCurrency(amount);
  },

  /**
   * Returns a formatted quantity according to the current configuration
   *
   * @param qty {number} - the quantity to be formatted
   * @return {string} - the formatted amount
   */
  printQty: qty => {
    return OB.DEC.toNumber(
      OB.DEC.toBigDecimal(qty),
      OB.I18N.qtyScale()
    ).toString();
  },

  /**
   * Gets the "change label" according to the payment status of the provided ticket
   *
   * @param ticket {object} - a ticket
   * @return {string} - the "change label"
   */
  getChangeLabelFromTicket: ticket => {
    if (ticket.changePayments) {
      return ticket.changePayments.map(payment => payment.label).join(' + ');
    }
    const changeLoadedTicket = ticket.payments
      .filter(
        payment =>
          payment.paymentData &&
          payment.paymentData.label &&
          payment.paymentData.key === payment.kind
      )
      .map(payment => payment.paymentData.label)
      .join(' + ');
    if (changeLoadedTicket !== '') {
      return changeLoadedTicket;
    }
    if (ticket.change) {
      // exist and different from zero
      // TODO Remove this when implement multicurrency in pos2
      //      Since changePayments will have value
      return OB.I18N.formatCurrency(ticket.change);
    }
    return '';
  }
};
