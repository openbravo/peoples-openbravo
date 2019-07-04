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
    calculateLocal: function() {
      var ticket = JSON.parse(JSON.stringify(OB.MobileApp.model.receipt));
      if (!OB.Discounts.Pos.ruleImpls) {
        throw 'Local discount cache is not yet initialized, execute: OB.Discounts.Pos.initCache()';
      }
      return OB.Discounts.applyDiscounts(
        ticket,
        this.getApplicableDiscounts(ticket)
      );
    },

    calculateRemote: function(jsMode) {
      var ticket = JSON.parse(JSON.stringify(OB.MobileApp.model.receipt));
      if (jsMode) {
        ticket.jsEngine = jsMode;
      }

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

    applyDiscounts: function(ticket, discounts) {
      discounts.forEach(discount => {
        let line = ticket.get('lines').get(discount.ticketLine.id);
        let rule = OB.Discounts.Pos.ruleImpls.find(r => r.id === discount.id);
        if (!rule) {
          OB.error('Rule not found!', discount);
          return;
        }
        let ruleModel = rule.ruleModel;
        try {
          ticket.addPromotion(line, ruleModel, {
            amt: discount.discount
          });
        } catch (e) {
          OB.error(e);
        }
      });
    },

    initCache: function() {
      OB.Discounts.Pos.ruleImpls = [];
      OB.Dal.find(OB.Model.Discount, [], rules => {
        rules.forEach(rule => {
          var r = JSON.parse(JSON.stringify(rule)),
            ruleFilter = { priceAdjustment: rule.get('id') };

          OB.Dal.find(
            OB.Model.DiscountFilterProduct,
            ruleFilter,
            products => (r.products = JSON.parse(JSON.stringify(products)))
          );

          OB.Dal.find(
            OB.Model.DiscountFilterProductCategory,
            ruleFilter,
            productCategories =>
              (r.productCategories = JSON.parse(
                JSON.stringify(productCategories)
              ))
          );

          OB.Dal.find(
            OB.Model.DiscountFilterBusinessPartner,
            ruleFilter,
            bps => (r.businessPartners = JSON.parse(JSON.stringify(bps)))
          );

          OB.Dal.find(
            OB.Model.DiscountFilterBusinessPartnerGroup,
            ruleFilter,
            bpCategories =>
              (r.businessPartnerCategories = JSON.parse(
                JSON.stringify(bpCategories)
              ))
          );
          r.ruleModel = rule;
          OB.Discounts.Pos.ruleImpls.push(r);
        });
      });

      let stdPromotions = OB.Model.Discounts.applyPromotions;

      OB.Model.Discounts.applyPromotions = function(receipt, line) {
        if (OB.Discounts.Pos.oldImpl) {
          OB.info(
            'Using old discount implementation - to use new one: OB.Discounts.Pos.oldImpl=false'
          );
          stdPromotions(receipt, line);
        } else {
          OB.info(
            'Using new discount engine - to use old one: OB.Discounts.Pos.oldImpl=true'
          );
          let discounts = OB.Discounts.Pos.calculateLocal();
          OB.Discounts.Pos.applyDiscounts(receipt, discounts);
          OB.Model.Discounts.finishPromotions(receipt, line);
        }
      };
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
