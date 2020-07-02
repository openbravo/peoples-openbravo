/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global */

(function AddByTotalPromotionDefinition() {
  OB.App.StateAPI.Ticket.registerAction(
    'addByTotalPromotion',
    (ticket, payload) => {
      const newTicket = { ...ticket };
      newTicket.discountsFromUser = ticket.discountsFromUser
        ? { ...ticket.discountsFromUser }
        : {};
      const discount = { ...payload.discount };

      discount.rule = { ...discount.rule };
      const { rule } = discount;

      if (
        newTicket.discountsFromUser.bytotalManualPromotions &&
        (!rule.obdiscAllowmultipleinstan ||
          newTicket.discountsFromUser.bytotalManualPromotions.length > 0)
      ) {
        // Remove other manual promotion with the same ruleId that hasMultiDiscount set as false or undefined
        newTicket.discountsFromUser.bytotalManualPromotions = newTicket.discountsFromUser.bytotalManualPromotions.filter(
          byTotalManualPromotion =>
            byTotalManualPromotion.obdiscAllowmultipleinstan ||
            byTotalManualPromotion.id !== rule.id
        );
      }
      if (rule.obdiscAllowmultipleinstan && !rule.discountinstance) {
        rule.discountinstance = OB.App.UUID.generate();
      }

      const bytotalManualPromotionObj = { ...discount.discountRule };

      // Override some configuration from manualPromotions
      if (bytotalManualPromotionObj.disctTotalamountdisc) {
        bytotalManualPromotionObj.disctTotalamountdisc = rule.userAmt;
        if (discount.currencyIdentifier) {
          bytotalManualPromotionObj.name += ` - ${rule.userAmt} ${discount.currencyIdentifier}`;
        }
      } else if (bytotalManualPromotionObj.disctTotalpercdisc) {
        bytotalManualPromotionObj.disctTotalpercdisc = rule.userAmt;
        // Percentage identifier is slightly different in Variable Percentage amount.
        if (
          discount.discountRule.discountType ===
          '096984DC2B944C85A9162C66C37EE7A3'
        ) {
          bytotalManualPromotionObj.name += ` - ${rule.userAmt} %`;
        } else {
          bytotalManualPromotionObj.name += ` - ${rule.userAmt}%`;
        }
      }
      bytotalManualPromotionObj.noOrder = rule.noOrder;
      bytotalManualPromotionObj.discountinstance = rule.discountinstance;
      if (discount.discountRules) {
        const promotionName = discount.discountRules[
          bytotalManualPromotionObj.discountType
        ].getIdentifier(discount.discountRule, bytotalManualPromotionObj);
        bytotalManualPromotionObj.name = promotionName;
        // eslint-disable-next-line no-underscore-dangle
        bytotalManualPromotionObj._identifier = discount.name;
      }
      bytotalManualPromotionObj.products = [];
      bytotalManualPromotionObj.includedProducts = 'Y';
      bytotalManualPromotionObj.productCategories = [];
      bytotalManualPromotionObj.includedProductCategories = 'Y';
      bytotalManualPromotionObj.productCharacteristics = [];
      bytotalManualPromotionObj.includedCharacteristics = 'Y';
      bytotalManualPromotionObj.allweekdays = true;

      newTicket.discountsFromUser.bytotalManualPromotions = (newTicket
        .discountsFromUser.bytotalManualPromotions
        ? [
            ...newTicket.discountsFromUser.bytotalManualPromotions,
            bytotalManualPromotionObj
          ]
        : [bytotalManualPromotionObj]
      ).sort((a, b) => {
        return a.noOrder - b.noOrder;
      });

      return newTicket;
    }
  );
})();
