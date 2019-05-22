/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _*/

var OBRDM = {};
OBRDM.UTIL = {};

(function () {

  /**
   * Add a NONE condition to data collection
   *
   * @param Collection
   */

  OBRDM.UTIL.addNoneCondition = function (data) {
    data.add({
      id: '',
      name: ''
    }, {
      at: 0
    });
    return data;
  };

  /**
   * Fill Delivery Modes combo box
   *
   * @param combo Combo box
   * @param args Arguments of function: fetchDataFunction
   */

  OBRDM.UTIL.fillComboCollection = function (combo, args) {
    var deliveryModes = OB.MobileApp.model.get('deliveryModes');
    if (deliveryModes && deliveryModes.length > 0) {
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
  OBRDM.UTIL.deliveryModesForFilter = function (deliveryModes) {
    var excluded = [];
    _.each(deliveryModes, function (excl) {
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

  OBRDM.UTIL.checkPickAndCarryPaidAmount = function (order) {
    var pickAndCarryAmount = OB.DEC.Zero;
    _.each(order.get('lines').models, function (line) {
      if (line.get('product').get('productType') !== 'S') { //Products
        if (!line.get('obrdmDeliveryMode') || line.get('obrdmDeliveryMode') === 'PickAndCarry') {
          if (line.has('obposLinePrepaymentAmount')) {
            pickAndCarryAmount = OB.DEC.add(pickAndCarryAmount, line.get('obposLinePrepaymentAmount'));
          } else if (line.get('obposCanbedelivered')) {
            var discountAmt = _.reduce(line.get('promotions'), function (memo, promo) {
              return OB.DEC.add(memo, OB.UTIL.isNullOrUndefined(promo.amt) ? OB.DEC.Zero : promo.amt);
            }, 0);
            pickAndCarryAmount = OB.DEC.add(pickAndCarryAmount, OB.DEC.sub(line.get('gross'), discountAmt));
          }
        }
      }
    });
    return {
      pickAndCarryAmount: pickAndCarryAmount,
      payment: order.getPaymentWithSign()
    };
  };

}());

OB.UI.SearchServicesFilter.prototype.filterExtensions.push({
  sqlExtension: function (productId, productList, orderline, orderlineList, where, filters, extraParams) {
    var newWhere, result;

    newWhere = where + " and product.obrdmIsdeliveryservice = '" + Boolean(extraParams && extraParams.isDeliveryService) + "'";
    result = {
      where: newWhere,
      filters: filters
    };

    return result;
  },
  hqlExtension: function (productId, productList, orderline, orderlineList, filters, extraParams) {
    filters.push({
      columns: ['obrdmIsdeliveryservice'],
      operator: 'equals',
      value: Boolean(extraParams && extraParams.isDeliveryService),
      boolean: true
    });
    return filters;
  }
});

OBRDM.UTIL.distributePaymentsPickAndCarry = function (multiOrderList, payments, finalCallback) {
  var setPickAndCarryPayments;

  function updateAmountToLayaway(order, amount) {
    var amountToLayaway = order.get('amountToLayaway');
    if (!OB.UTIL.isNullOrUndefined(amountToLayaway)) {
      order.set('amountToLayaway', OB.DEC.sub(amountToLayaway, amount));
    }
  }

  setPickAndCarryPayments = function (orderList, paymentList, orderListIndex, paymentListIndex, callback) {
    if (orderListIndex >= orderList.length || paymentListIndex >= paymentList.length) {
      if (callback instanceof Function) {
        callback();
      }
      return;
    }

    var order = orderList.at(orderListIndex),
        payment = paymentList.at(paymentListIndex),
        paymentLine;

    function addPaymentLine(paymentLine, payment, addPaymentCallback) {
      OB.UTIL.HookManager.executeHooks('OBPOS_MultiOrderAddPaymentLine', {
        paymentLine: paymentLine,
        origPayment: payment
      }, function (args) {
        order.addPayment(args.paymentLine, function () {
          updateAmountToLayaway(order, args.paymentLine.get('origAmount'));
          if (addPaymentCallback instanceof Function) {
            addPaymentCallback();
          }
        });
      });
    }

    if (order.get('amountToLayaway')) {
      setPickAndCarryPayments(orderList, paymentList, orderListIndex + 1, paymentListIndex, callback);
    } else {
      var pickAndCarryAmount = OBRDM.UTIL.checkPickAndCarryPaidAmount(order).pickAndCarryAmount;

      if (pickAndCarryAmount > order.getPayment() && !_.isUndefined(payment)) {
        var amountToPay = OB.DEC.sub(pickAndCarryAmount, order.getPayment());
        if (OB.DEC.compare(amountToPay) > 0) {
          var paymentMethod = OB.MobileApp.model.paymentnames[payment.get('kind')];

          paymentLine = new OB.Model.PaymentLine();
          OB.UTIL.clone(payment, paymentLine);

          if (payment.get('origAmount') <= amountToPay) {
            // Use all the remaining payment amount for this receipt
            payment.set('origAmount', OB.DEC.Zero);
            payment.set('amount', OB.DEC.Zero);
            addPaymentLine(paymentLine, payment, function () {
              setPickAndCarryPayments(orderList, paymentList, orderListIndex, paymentListIndex + 1, callback);
            });
          } else {
            // Get part of the payment and go with the next order
            var amountToPayForeign = OB.DEC.mul(amountToPay, paymentMethod.mulrate, paymentMethod.obposPosprecision);
            payment.set('origAmount', OB.DEC.sub(payment.get('origAmount'), amountToPay));
            payment.set('amount', OB.DEC.sub(payment.get('amount'), amountToPayForeign));

            paymentLine.set('origAmount', amountToPay);
            paymentLine.set('amount', amountToPayForeign);

            addPaymentLine(paymentLine, payment, function () {
              setPickAndCarryPayments(orderList, paymentList, orderListIndex + 1, paymentListIndex, callback);
            });
          }
        } else {
          // This order is already paid, go to the next order
          setPickAndCarryPayments(orderList, paymentList, orderListIndex + 1, paymentListIndex, callback);
        }
      } else {
        setPickAndCarryPayments(orderList, paymentList, orderListIndex + 1, paymentListIndex, callback);
      }

    }
  };

  setPickAndCarryPayments(multiOrderList, payments, OB.DEC.Zero, OB.DEC.Zero, function () {
    if (finalCallback && finalCallback instanceof Function) {
      finalCallback();
    }
  });
};

OB.UTIL.HookManager.registerHook('OBPOS_ServicePriceRules_PreSetPriceToLine', function (args, callback) {
  if (OB.MobileApp.model.get('deliveryPaymentMode') === 'PD' && args.line.get('product').get('obrdmIsdeliveryservice')) {
    var newAmountToPay = (args.priceChanged || !args.line.has('baseAmountToPayInDeliver') ? OB.DEC.mul(args.newprice, args.line.get('qty')) : OB.DEC.mul(args.line.get('product').get('listPrice'), args.line.get('qty'))),
        baseAmountToPay = args.line.get('baseAmountToPayInDeliver');
    if (OB.UTIL.isNullOrUndefined(baseAmountToPay) || OB.DEC.sub(newAmountToPay, baseAmountToPay) !== 0) {
      args.line.set('obrdmAmttopayindelivery', newAmountToPay);
      args.line.set('baseAmountToPayInDeliver', newAmountToPay);
    }
    args.newprice = 0;
  } else {
    args.line.unset('obrdmAmttopayindelivery');
    args.line.unset('baseAmountToPayInDeliver');
  }
  OB.UTIL.HookManager.callbackExecutor(args, callback);
});

OB.UTIL.HookManager.registerHook('OBPOS_LoadRelatedServices_ExtendCriteria', function (args, callback) {
  args.criteria._whereClause += " and product.obrdmIsdeliveryservice =  'false' ";
  OB.UTIL.HookManager.callbackExecutor(args, callback);
});