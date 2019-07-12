/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class Discount {
    constructor(ticket, discountImpl, discounts) {
      this.ticket = ticket;
      this.discountImpl = discountImpl;
      this.discounts = discounts;
    }

    /*
     * This method must be ALWAYS override in each subclass
     * Used for log purpouses only. Identifies the discount type being applied
     * Output: String with promotion name
     */
    getDiscountTypeName() {
      throw 'getDiscountTypeName method not implemented';
    }

    /*
     * This method must be ALWAYS override in each subclass
     * Implementation of the discount calculation for extended subclass
     * Input: Array of lines applicable to the promotion
     */
    executeDiscountCalculation() {
      throw 'executeDiscountCalculation method not implemented';
    }

    /*
     * This method must be ALWAYS override in each subclass
     * Decides which applicable lines are valid to be discountable
     * Input: Array of lines candidates to be applied
     * Output: Array of lines applicable to the promotion
     */
    canApplyDiscount() {
      throw 'canApplyDiscount method not implemented';
    }

    getTicket() {
      return this.ticket;
    }

    getDiscountImplementationId() {
      return this.discountImpl.id;
    }

    calculateDiscounts() {
      let applicableLines = this.getApplicableLines();
      let discountableLines = this.canApplyDiscount(applicableLines);
      if (discountableLines && applicableLines.length > 0) {
        OB.debug(
          `Discount type ${this.getDiscountTypeName()} with id ${this.getDiscountImplementationId()} executing discount calculation`
        );
        this.executeDiscountCalculation(discountableLines);
      } else {
        OB.debug(
          `Discount type ${this.getDiscountTypeName()} with id ${this.getDiscountImplementationId()} cannot be applied`
        );
      }
    }

    static isApplicableToLine(line, rule) {
      let elementFound = rule.products.find(
        p => p.product.id === line.product.id
      );
      let onlyIncluded = rule.includedProducts === 'N';
      let applicable =
        (onlyIncluded && elementFound) || (!onlyIncluded && !elementFound);

      if (!applicable) {
        return false;
      }

      elementFound = rule.productCategories.find(
        pc => pc.productCategory.id === line.product.productCategory.id
      );
      onlyIncluded = rule.includedProductCategories === 'N';
      applicable =
        (onlyIncluded && elementFound) || (!onlyIncluded && !elementFound);

      return applicable;
    }

    acceptsDiscount(line) {
      let discountsForLine = this.discounts.filter(
        disc => disc.ticketLine.id === line.id
      );
      return (
        discountsForLine.length === 0 ||
        discountsForLine[discountsForLine.length - 1].applyNext
      );
    }

    getApplicableLines() {
      return this.ticket.lines
        .filter(line => this.acceptsDiscount(line))
        .filter(line => Discount.isApplicableToLine(line, this.discountImpl));
    }

    getPrice(line) {
      let price = line.price;
      this.discounts
        .filter(disc => disc.ticketLine.id === line.id)
        .forEach(disc => (price = OB.DEC.sub(price, disc.discount)));
      return price;
    }

    getUnitPrice(line) {
      return OB.DEC.div(this.getPrice(line), line.qty);
    }

    addDiscount(line, amt, additionalParams) {
      OB.debug(
        'Applying discount',
        this.discountImpl.name,
        line.product._identifier
      );
      let discountToAdd = {
        id: this.discountImpl.id,
        discountType: this.discountImpl.discountType,
        name: this.discountImpl.name,
        ticketLine: { id: line.id, product: line.product._identifier },
        applyNext: this.discountImpl.applyNext,
        discount: amt
      };
      if (additionalParams) {
        Object.assign(discountToAdd, additionalParams);
      }
      this.discounts.push(discountToAdd);
    }
  }

  OB.Discounts.Discount = Discount;
  OB.Discounts.discountRules = {};
})();
