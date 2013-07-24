/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone,$ */

(function () {
  // Because of problems with module dependencies, it is possible this object to already
  // be defined with some rules.
  var alreadyDefinedRules = (OB && OB.Model && OB.Model.Discounts && OB.Model.Discounts.discountRules) || {},
      onLoadActions = (OB && OB.Model && OB.Model.Discounts && OB.Model.Discounts.onLoadActions) || [],
      i;

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

    /**
     * Gets the list of manual promotions. If asArray param is true, it is returned
     * as an array, other case, as a comma separated string to be used in sql statements
     */
    getManualPromotions: function (asList) {
      var p, promos = [],
          promosSql = '';
      for (p in this.discountRules) {
        if (this.discountRules.hasOwnProperty(p)) {
          if (this.discountRules[p].addManual) {
            promos.push(p);
          }
        }
      }

      if (asList) {
        return promos;
      } else {
        // generate sql
        for (p = 0; p < promos.length; p++) {
          if (promosSql !== '') {
            promosSql += ', ';
          }
          promosSql += "'" + promos[p] + "'";
        }
        return promosSql;
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


  // Because of dependency models cannot be directly registered in promotions module 
  if (OB && OB.Model && OB.Model.Discounts && OB.Model.Discounts.extraModels) {
    for (i = 0; i < OB.Model.Discounts.extraModels.length; i++) {
      OB.Data.Registry.registerModel(OB.Model.Discounts.extraModels[i]);
    }
  }

  for (i = 0; i < onLoadActions.length; i++) {
    if (onLoadActions[i].execute) {
      onLoadActions[i].execute();
    }
  }
}());