/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  function Discount(ticket, discountImpl, discounts) {
    this.ticket = ticket;
    this.discountImpl = discountImpl;
    this.discounts = discounts;
  }

  Discount.prototype.getTicket = function() {
    return this.ticket;
  };

  Discount.prototype.calculateDiscounts = function() {
    throw 'not implemented!';
  };

  Discount.prototype.getApplicableLines = function() {
    let me = this; // due to partial ES6 support in nashorn
    return this.ticket.lines
      .filter(line => me.acceptsDiscount(line))
      .filter(line => Discount.isApplicableToLine(line, me.discountImpl));
  };

  Discount.prototype.acceptsDiscount = function(line) {
    let discountsForLine = this.discounts.filter(
      disc => disc.ticketLine.id === line.id
    );
    return (
      discountsForLine.length === 0 ||
      discountsForLine[discountsForLine.length - 1].applyNext
    );
  };

  Discount.prototype.getPrice = function(line) {
    let price = line.gross;
    this.discounts
      .filter(disc => disc.ticketLine.id === line.id)
      .forEach(disc => (price = OB.DEC.sub(price, disc.discount)));
    return price;
  };

  Discount.prototype.getUnitPrice = function(line) {
    return OB.DEC.div(this.getPrice(line), line.qty);
  };

  Discount.prototype.addDiscount = function(line, amt) {
    OB.debug(
      'Applying discount',
      this.discountImpl.name,
      line.product._identifier
    );
    this.discounts.push({
      id: this.discountImpl.id,
      discountType: this.discountImpl.discountType,
      name: this.discountImpl.name,
      ticketLine: { id: line.id, product: line.product._identifier },
      applyNext: this.discountImpl.applyNext,
      discount: amt
    });
  };

  // static
  Discount.isApplicableToLine = function(line, rule) {
    let elementFound = rule.products.find(p => p.product === line.product.id);
    let onlyIncluded = rule.includedProducts === 'N';
    let applicable =
      (onlyIncluded && elementFound) || (!onlyIncluded && !elementFound);

    if (!applicable) {
      return false;
    }

    elementFound = rule.productCategories.find(
      pc => pc.productCategory === line.product.productCategory
    );
    onlyIncluded = rule.includedProductCategories === 'N';
    applicable =
      (onlyIncluded && elementFound) || (!onlyIncluded && !elementFound);

    return applicable;
  };
  OB.Discounts.Discount = Discount;
  OB.Discounts.discountRules = {};
})();
