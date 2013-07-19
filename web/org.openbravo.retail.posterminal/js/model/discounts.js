/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone,$ */

(function () {
  // Because of problems with module dependencies, it is possible this object to already
  // be defined with some rules.
  var alreadyDefinedRules = (OB && OB.Model && OB.Model.Discounts && OB.Model.Discounts.discountRules) || {};

  OB.Model.Discounts = {
    discountRules: alreadyDefinedRules,
    executor: new OB.Model.DiscountsExecutor(),
    applyPromotions: function (receipt, line) {
      var lines;
      if (receipt && !receipt.get('isEditable')) {
        return;
      }

      if (line) {
        this.executor.addEvent(new Backbone.Model({
          id: line.cid,
          receipt: receipt,
          line: line
        }), true);
      } else {
        lines = receipt.get('lines');
        if (lines.length === 0) {
          // Removing last line, recalculate total
          receipt.calculateGross();
        } else {
          lines.forEach(function (l) {
            this.applyPromotions(receipt, l);
          }, this);
        }
      }
    },

    addManualPromotion: function (receipt, lines, promotion) {
      var rule = OB.Model.Discounts.discountRules[promotion.rule.get ? promotion.rule.get('discountType') : promotion.rule.discountType];
      if (!rule || !rule.addManual) {
        window.console.warn('No manual implemetation for rule ' + promotion.discountType);
        return;
      }

      lines.forEach(function (line) {
        rule.addManual(receipt, line, promotion);
      });

      if (!promotion.alreadyCalculated) {
        // Recalculate all promotions again
        OB.Model.Discounts.applyPromotions(receipt);
      }
    },

    registerRule: function (name, rule) {
      this.discountRules[name] = rule;
    },

    standardFilter: "WHERE date('now') BETWEEN DATEFROM AND COALESCE(date(DATETO), date('9999-12-31'))" //
    + " AND((BPARTNER_SELECTION = 'Y'" //
    + " AND NOT EXISTS" //
    + " (SELECT 1" //
    + " FROM M_OFFER_BPARTNER" //
    + " WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID" //
    + "   AND C_BPARTNER_ID = ?" //
    + " ))" //
    + " OR(BPARTNER_SELECTION = 'N'" //
    + " AND EXISTS" //
    + " (SELECT 1" //
    + " FROM M_OFFER_BPARTNER" //
    + " WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID" //
    + "   AND C_BPARTNER_ID = ?" //
    + " )))" //
    + " AND((BP_GROUP_SELECTION = 'Y'" //
    + " AND NOT EXISTS" //
    + " (SELECT 1" //
    + " FROM C_BPARTNER B," //
    + "   M_OFFER_BP_GROUP OB" //
    + " WHERE OB.M_OFFER_ID = M_OFFER.M_OFFER_ID" //
    + "   AND B.C_BPARTNER_ID = ?" //
    + "   AND OB.C_BP_GROUP_ID = B.C_BP_GROUP_ID" //
    + " ))" //
    + " OR(BP_GROUP_SELECTION = 'N'" //
    + " AND EXISTS" //
    + " (SELECT 1" //
    + " FROM C_BPARTNER B," //
    + "   M_OFFER_BP_GROUP OB" //
    + " WHERE OB.M_OFFER_ID = M_OFFER.M_OFFER_ID" //
    + "   AND B.C_BPARTNER_ID = ?" //
    + "   AND OB.C_BP_GROUP_ID = B.C_BP_GROUP_ID" //
    + " )))" //
    + " AND((PRODUCT_SELECTION = 'Y'" //
    + " AND NOT EXISTS" //
    + " (SELECT 1" //
    + " FROM M_OFFER_PRODUCT" //
    + " WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID" //
    + "   AND M_PRODUCT_ID = ?" //
    + " ))" //
    + " OR(PRODUCT_SELECTION = 'N'" //
    + " AND EXISTS" //
    + " (SELECT 1" //
    + " FROM M_OFFER_PRODUCT" //
    + " WHERE M_OFFER_ID = M_OFFER.M_OFFER_ID" //
    + "   AND M_PRODUCT_ID = ?" //
    + " )))" //
    + " AND((PROD_CAT_SELECTION = 'Y'" //
    + " AND NOT EXISTS" //
    + " (SELECT 1" //
    + " FROM M_PRODUCT P," //
    + "   M_OFFER_PROD_CAT OP" //
    + " WHERE OP.M_OFFER_ID = M_OFFER.M_OFFER_ID" //
    + "   AND P.M_PRODUCT_ID = ?" //
    + "   AND OP.M_PRODUCT_CATEGORY_ID = P.M_PRODUCT_CATEGORY_ID" //
    + " ))" //
    + " OR(PROD_CAT_SELECTION = 'N'" //
    + " AND EXISTS" //
    + " (SELECT 1" //
    + " FROM M_PRODUCT P," //
    + "   M_OFFER_PROD_CAT OP" //
    + " WHERE OP.M_OFFER_ID = M_OFFER.M_OFFER_ID" //
    + "   AND P.M_PRODUCT_ID = ?" //
    + "   AND OP.M_PRODUCT_CATEGORY_ID = P.M_PRODUCT_CATEGORY_ID" //
    + " ))) " //
    // TODO: Discretionary discounts, don't check them by id
    + " AND M_OFFER_TYPE_ID NOT IN ('D1D193305A6443B09B299259493B272A', '20E4EC27397344309A2185097392D964', '7B49D8CC4E084A75B7CB4D85A6A3A578', '8338556C0FBF45249512DB343FEFD280')"
  };

  // Price Adjustment
  OB.Model.Discounts.registerRule('5D4BAF6BB86D4D2C9ED3D5A6FC051579', {
    async: false,
    implementation: function (discountRule, receipt, line) {
      var linePrice, discountedLinePrice, qty = line.get('qty'),
          minQty = discountRule.get('minQuantity'),
          maxQty = discountRule.get('maxQuantity');

      if ((minQty && qty < minQty) || (maxQty && qty > maxQty)) {
        return;
      }

      linePrice = line.get('discountedLinePrice') || line.get('price');
      if (discountRule.get('fixedPrice') || discountRule.get('fixedPrice') === 0) {
        discountedLinePrice = discountRule.get('fixedPrice');
      } else {
        discountedLinePrice = (linePrice - discountRule.get('discountAmount')) * (1 - discountRule.get('discount') / 100);
      }
      receipt.addPromotion(line, discountRule, {
        amt: (linePrice - OB.DEC.toNumber(new BigDecimal(String(discountedLinePrice)))) * qty
      });
      line.set('discountedLinePrice', discountedLinePrice);
    }
  });
}());