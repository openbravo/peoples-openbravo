/*
 ************************************************************************************
 * Copyright (C) 2017-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB*/

(function() {
  OB.UTIL.prepaymentRules = {};
  OB.UTIL.prepaymentRules.OBPOS_Default = {
    execute: function(receipt, callback) {
      var me = this,
        prepaymentPerc = OB.MobileApp.model.get('terminal').obposPrepaymentPerc,
        prepaymentPercLimit,
        prepaymentPercLayLimit = OB.MobileApp.model.get('terminal')
          .obposPrepayPercLayLimit,
        prepaymentLimitAmount = OB.DEC.Zero,
        prepaymentLayawayLimitAmount = OB.DEC.Zero,
        prepaymentAmount = receipt.get('lines').reduce(function(memo, line) {
          prepaymentPercLimit = OB.MobileApp.model.get('terminal')
            .obposPrepaymentPercLimit;
          if (
            line.get('obposCanbedelivered') ||
            line.get('deliveredQuantity') === line.get('qty')
          ) {
            var linePrepaymentAmount = OB.DEC.Zero;
            if (
              OB.MobileApp.model.get('permissions')[
                'OBRDM_EnableDeliveryModes'
              ] &&
              line.get('obrdmDeliveryMode') === 'PickAndCarry'
            ) {
              prepaymentPercLimit = 100;
              linePrepaymentAmount = line.get('gross');
              if (line.get('promotions') && line.get('promotions').length > 0) {
                linePrepaymentAmount = OB.DEC.sub(
                  linePrepaymentAmount,
                  line.get('promotions').reduce(function(total, model) {
                    return OB.DEC.add(total, model.amt);
                  }, 0)
                );
              }
              line.set('obposLinePrepaymentAmount', linePrepaymentAmount);
              prepaymentLimitAmount = OB.DEC.add(
                prepaymentLimitAmount,
                OB.DEC.div(
                  OB.DEC.mul(linePrepaymentAmount, prepaymentPercLimit),
                  100
                )
              );
              prepaymentLayawayLimitAmount = OB.DEC.add(
                prepaymentLayawayLimitAmount,
                OB.DEC.div(
                  OB.DEC.mul(linePrepaymentAmount, prepaymentPercLayLimit),
                  100
                )
              );
              return OB.DEC.add(memo, linePrepaymentAmount);
            } else {
              linePrepaymentAmount = me.currentLinePrepaymentAmount(
                line,
                prepaymentPerc
              );
              line.set('obposLinePrepaymentAmount', linePrepaymentAmount);
              prepaymentLimitAmount = OB.DEC.add(
                prepaymentLimitAmount,
                OB.DEC.div(
                  OB.DEC.mul(linePrepaymentAmount, prepaymentPercLimit),
                  100
                )
              );
              prepaymentLayawayLimitAmount = OB.DEC.add(
                prepaymentLayawayLimitAmount,
                OB.DEC.div(
                  OB.DEC.mul(linePrepaymentAmount, prepaymentPercLayLimit),
                  100
                )
              );
              return OB.DEC.add(memo, linePrepaymentAmount);
            }
          } else {
            line.set('obposLinePrepaymentAmount', OB.DEC.Zero);
            return memo;
          }
        }, 0);

      callback(
        prepaymentAmount,
        prepaymentLimitAmount,
        prepaymentLayawayLimitAmount
      );
    },
    currentLinePrepaymentAmount: function(line, percentage, units) {
      var price, discount;
      if (units) {
        price = line.get('priceList');
        if (line.get('promotions') && line.get('promotions').length > 0) {
          discount = line.get('promotions').reduce(function(total, model) {
            return OB.DEC.add(total, model.amt);
          }, 0);
          price = OB.DEC.sub(
            price,
            OB.DEC.div(OB.DEC.mul(discount, units), line.get('qty'))
          );
        }
        price = OB.DEC.mul(price, units);
      } else {
        price = line.get('gross');
        if (line.get('promotions') && line.get('promotions').length > 0) {
          discount = line.get('promotions').reduce(function(total, model) {
            return OB.DEC.add(total, model.amt);
          }, 0);
          price = OB.DEC.sub(price, discount);
        }
      }
      return OB.DEC.div(OB.DEC.mul(price, percentage), 100);
    }
  };
})();
