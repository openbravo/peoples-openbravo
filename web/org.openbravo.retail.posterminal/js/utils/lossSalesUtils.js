/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global */

(function() {
  OB.UTIL.LossSaleUtils = {};

  OB.UTIL.LossSaleUtils.getLossSaleLines = function(order) {
    var result = [],
      discountsVerifyPriceLimit = [].concat(
        OB.Discounts.Pos.manualRuleImpls.filter(discount => {
          return discount.verifyPriceLimit === true;
        }),
        OB.Discounts.Pos.ruleImpls.filter(discount => {
          return discount.verifyPriceLimit === true;
        })
      );
    _.each(order.get('lines').models, function(line) {
      var verifyPriceLimit =
        OB.MobileApp.model
          .get('priceModificationReasons')
          .some(
            reason =>
              reason.id === line.get('oBPOSPriceModificationReason') &&
              reason.verifyPriceLimit
          ) ||
        line
          .get('promotions')
          .some(promo =>
            discountsVerifyPriceLimit.some(
              discount => promo.ruleId === discount.id
            )
          );

      if (
        verifyPriceLimit &&
        line.get('gross') >= 0 &&
        line.get('isEditable')
      ) {
        var product = line.get('product');
        if (line.get('grossUnitPrice') < product.get('priceLimit')) {
          result.push({
            orderId: order.id,
            id: line.get('id'),
            product: product.get('_identifier'),
            priceLimit: product.get('priceLimit'),
            unitPrice: line.get('grossUnitPrice')
          });
        }
      }
    });
    return result;
  };

  OB.UTIL.LossSaleUtils.adjustPriceOnLossSaleLines = function(lossSaleLines) {
    var order = OB.MobileApp.model.receipt;
    lossSaleLines.forEach(function(lossSaleLine) {
      var line = order
          .get('lines')
          .models.find(l => l.get('id') === lossSaleLine.id),
        discountedPrice = line.get('grossUnitPrice'),
        productPriceLimit = line.get('product').get('priceLimit');
      line.get('promotions').forEach(function(p) {
        if (discountedPrice < productPriceLimit) {
          var promoAmtDiffToLimit = Math.min(
            p.actualAmt,
            OB.DEC.sub(productPriceLimit, discountedPrice)
          );
          p.amt = p.fullAmt = p.actualAmt = p.displayedTotalAmount = OB.DEC.sub(
            p.actualAmt,
            promoAmtDiffToLimit
          );
          p.identifier =
            p.name +
            OB.I18N.getLabel('OBMOBC_Character')[5] +
            OB.I18N.getLabel('OBPOS_LblLimitPrice');
          discountedPrice += promoAmtDiffToLimit;
        }
      });
      line.set('isLossSale', true);
      order.calculateGross();
    });
  };
})();
