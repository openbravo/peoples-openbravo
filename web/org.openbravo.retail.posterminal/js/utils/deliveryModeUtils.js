/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

var OBRDM = {};
OBRDM.UTIL = {};

(function() {
  /**
   * Add a NONE condition to data collection
   *
   * @param Collection
   */

  OBRDM.UTIL.addNoneCondition = function(data) {
    data.add(
      {
        id: '',
        name: ''
      },
      {
        at: 0
      }
    );
    return data;
  };

  /**
   * Fill Delivery Modes combo box
   *
   * @param combo Combo box
   * @param args Arguments of function: fetchDataFunction
   */

  OBRDM.UTIL.fillComboCollection = function(combo, args) {
    var deliveryModes = OB.MobileApp.model.get('deliveryModes'),
      modes = [];
    if (deliveryModes && deliveryModes.length > 0) {
      if (
        OB.UTIL.isCrossStoreLine(args.model) ||
        (!OB.UTIL.isNullOrUndefined(args.organization) &&
          args.organization.id !==
            OB.MobileApp.model.get('terminal').organization)
      ) {
        _.each(deliveryModes, function(delivery) {
          if (delivery.id !== 'PickAndCarry') {
            modes.push(delivery);
          }
        });
        deliveryModes = modes;
      }
      var data = new Backbone.Collection();
      data.add(deliveryModes);
      combo.dataReadyFunction(data, args);
    } else {
      OB.UTIL.showError(OB.I18N.getLabel('OBRDM_ErrorGettingDeliveryModes'));
      combo.dataReadyFunction(null, args);
    }
  };

  /**
   * Convert a delivery modes to string ready to used in sql IN
   *
   * @param deliveryModes Delivery modes list
   * @return SQL condition
   */
  OBRDM.UTIL.deliveryModesForFilter = function(deliveryModes) {
    var excluded = [];
    _.each(deliveryModes, function(excl) {
      excluded.push("'" + excl + "'");
    });
    return excluded.length > 0 ? excluded.join(',') : '';
  };

  /**
   * Validate if the Pick and Carry lines of the order are completely paid
   *
   * @param order Order that is being checked
   * @return true if the Pick and Carry lines are completely paid
   */

  OBRDM.UTIL.checkPickAndCarryPaidAmount = function(order) {
    var pickAndCarryAmount = OB.DEC.Zero;
    _.each(order.get('lines').models, function(line) {
      if (line.get('product').get('productType') !== 'S') {
        //Products
        if (
          !line.get('obrdmDeliveryMode') ||
          line.get('obrdmDeliveryMode') === 'PickAndCarry'
        ) {
          if (line.has('obposLinePrepaymentAmount')) {
            pickAndCarryAmount = OB.DEC.add(
              pickAndCarryAmount,
              line.get('obposLinePrepaymentAmount')
            );
          } else if (line.get('obposCanbedelivered')) {
            var discountAmt = _.reduce(
              line.get('promotions'),
              function(memo, promo) {
                return OB.DEC.add(
                  memo,
                  OB.UTIL.isNullOrUndefined(promo.amt) ? OB.DEC.Zero : promo.amt
                );
              },
              0
            );
            pickAndCarryAmount = OB.DEC.add(
              pickAndCarryAmount,
              OB.DEC.sub(line.get('gross'), discountAmt)
            );
          }
        }
      }
    });
    return {
      pickAndCarryAmount: pickAndCarryAmount,
      payment: order.getPaymentWithSign()
    };
  };
})();
