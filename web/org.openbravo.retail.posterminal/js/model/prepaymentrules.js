/*
 ************************************************************************************
 * Copyright (C) 2017-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB*/

(function () {

  OB.UTIL.prepaymentRules = {};
  OB.UTIL.prepaymentRules.OBPOS_Default = {
    execute: function (receipt, callback) {
      var me = this,
          prepaymentAmount = receipt.get('lines').reduce(function (memo, line) {
          if (line.get('obposCanbedelivered') || line.get('deliveredQuantity') === line.get('qty')) {
            var linePrepaymentAmount = me.currentLinePrepaymentAmount(line, 100);
            line.set('obposLinePrepaymentAmount', linePrepaymentAmount);
            return OB.DEC.add(memo, linePrepaymentAmount);
          } else {
            line.set('obposLinePrepaymentAmount', OB.DEC.Zero);
            return memo;
          }
        }, 0),
          prepaymentLimitAmount = prepaymentAmount;
      callback(prepaymentAmount, prepaymentLimitAmount);
    },
    currentLinePrepaymentAmount: function (line, percentage, units) {
      var price, discount;
      if (units) {
        price = line.get('grossListPrice') || line.get('priceList');
        if (line.get('promotions') && line.get('promotions').length > 0) {
          discount = line.get('promotions').reduce(function (total, model) {
            return OB.DEC.add(total, model.amt);
          }, 0);
          price = OB.DEC.sub(price, OB.DEC.div(OB.DEC.mul(discount, units), line.get('qty')));
        }
        price = OB.DEC.mul(price, units);
      } else {
        price = line.get('gross');
        if (line.get('promotions') && line.get('promotions').length > 0) {
          discount = line.get('promotions').reduce(function (total, model) {
            return OB.DEC.add(total, model.amt);
          }, 0);
          price = OB.DEC.sub(price, discount);
        }
      }
      return OB.DEC.div(OB.DEC.mul(price, percentage), 100);
    }
  };
}());