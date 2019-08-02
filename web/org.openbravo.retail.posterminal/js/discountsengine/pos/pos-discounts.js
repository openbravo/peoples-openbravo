/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.Discounts = OB.Discounts || {};
  OB.Discounts.Pos = {
    local: true,

    calculateLocal: function(ticket) {
      if (!OB.Discounts.Pos.ruleImpls) {
        throw 'Local discount cache is not yet initialized, execute: OB.Discounts.Pos.initCache()';
      }
      return OB.Discounts.applyDiscounts(
        ticket,
        this.getApplicableDiscounts(ticket)
      );
    },

    calculateRemote: function(ticket) {
      ticket = JSON.stringify(ticket);
      fetch('../../discount', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: ticket
      })
        .then(response => response.json())
        .then(disc => OB.info(disc));
    },

    applyDiscounts: function(ticket, result) {
      ticket.get('lines').forEach(line => {
        const discountInfoForLine =
          result.lines[line.get('id')] &&
          result.lines[line.get('id')].discounts;
        if (!discountInfoForLine) {
          //No discounts for this line, we clear existing discounts if they exist, and move to the next
          line.set('promotions', []);
          return;
        }

        line.set('promotions', discountInfoForLine.promotions);
        return;
        // let rule = OB.Discounts.Pos.ruleImpls.find(r => r.id === discount.id);
        // if (!rule) {
        //   OB.error('Rule not found!', discount);
        //   return;
        // }
        // let ruleModel = rule.ruleModel;
        // try {
        //   ticket.addPromotion(line, ruleModel, {
        //     amt: discount.discount
        //   });
        // } catch (e) {
        //   OB.error(e);
        // }
      });
    },

    translateTicket: function(receipt) {
      let newTicket = {};
      newTicket.businessPartner = {};
      newTicket.businessPartner.id = receipt.get('bp').id;
      newTicket.businessPartner._identifier = receipt.get('bp')._identifier;
      newTicket.id = receipt.get('id');
      newTicket.date = receipt.get('orderDate');
      newTicket.lines = [];
      receipt.get('lines').forEach(line => {
        let newLine = {};
        newLine.id = line.get('id');
        newLine.product = {};
        newLine.product.id = line.get('product').id;
        newLine.product._identifier = line.get('product')._identifier;
        newLine.qty = line.get('qty');
        newLine.price = line.get('price');
        newTicket.lines.push(newLine);
      });
      return newTicket;
    },

    calculateDiscounts(receipt, callback) {
      const ticketForEngine = OB.Discounts.Pos.translateTicket(receipt);
      let result;
      if (OB.Discounts.Pos.local) {
        result = OB.Discounts.Pos.calculateLocal(ticketForEngine);
        OB.Discounts.Pos.applyDiscounts(receipt, result);
        callback();
      } else {
        result = OB.Discounts.Pos.calculateRemote(receipt, callback);
      }
    },

    initCache: function(callback) {
      OB.info('[Discounts cache] Starting load...');
      const initialTime = new Date().getTime();
      OB.Discounts.Pos.ruleImpls = [];
      OB.Dal.find(OB.Model.Discount, [], rules => {
        const finishCallback = _.after(rules.length, () => {
          OB.info(
            '[Discounts cache] ...load finished. Elapsed time: ' +
              (new Date().getTime() - initialTime) +
              'ms.'
          );
          callback();
        });
        rules.forEach(rule => {
          var r = JSON.parse(JSON.stringify(rule)),
            ruleFilter = { priceAdjustment: rule.get('id') };

          OB.Dal.find(OB.Model.DiscountFilterProduct, ruleFilter, products => {
            r.products = JSON.parse(JSON.stringify(products));
            r.products.forEach(
              offerProduct =>
                (offerProduct.product = { id: offerProduct.product })
            );
            OB.Dal.find(
              OB.Model.DiscountFilterProductCategory,
              ruleFilter,
              productCategories => {
                r.productCategories = JSON.parse(
                  JSON.stringify(productCategories)
                );
                OB.Dal.find(
                  OB.Model.DiscountFilterBusinessPartner,
                  ruleFilter,
                  bps => {
                    r.businessPartners = JSON.parse(JSON.stringify(bps));
                    OB.Dal.find(
                      OB.Model.DiscountFilterBusinessPartnerGroup,
                      ruleFilter,
                      bpCategories => {
                        r.businessPartnerCategories = JSON.parse(
                          JSON.stringify(bpCategories)
                        );
                        r.ruleModel = rule;
                        OB.Discounts.Pos.ruleImpls.push(r);
                        finishCallback();
                      }
                    );
                  }
                );
              }
            );
          });
        });
      });
    },

    getApplicableDiscounts: function(ticket) {
      return OB.Discounts.Pos.ruleImpls
        .filter(rule => this.canApplyRuleToTicket(ticket, rule))
        .filter(rule => this.canApplyRuleToAtLeastOneLine(ticket, rule));
    },

    // This part is calculated only in POS, backend implements it in Java
    canApplyRuleToTicket(ticket, rule) {
      let elementFound = rule.businessPartners.find(
        bp => bp.businessPartner === ticket.bp.id
      );
      let onlyIncluded = rule.includedBusinessPartners === 'N';
      let applicable =
        (onlyIncluded && elementFound) || (!onlyIncluded && !elementFound);

      if (!applicable) {
        return false;
      }

      elementFound = rule.businessPartnerCategories.find(
        bpc => bpc.businessPartnerCategory === ticket.bp.businessPartnerCategory
      );
      onlyIncluded = rule.includedBPCategories === 'N';
      applicable =
        (onlyIncluded && elementFound) || (!onlyIncluded && !elementFound);

      return applicable;
    },

    canApplyRuleToAtLeastOneLine(ticket, rule) {
      return ticket.lines.find(line =>
        OB.Discounts.Discount.isApplicableToLine(line, rule)
      );
    }
  };
})();
