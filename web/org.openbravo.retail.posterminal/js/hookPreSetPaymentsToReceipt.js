/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global OBRDM */

OB.UTIL.HookManager.registerHook(
  'OBPOS_MultiOrders_PreSetPaymentsToReceipt',
  function(args, callbacks) {
    if (OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true)) {
      var setPickAndCarryPayments;

      setPickAndCarryPayments = function(
        orderList,
        paymentList,
        orderListIndex,
        paymentListIndex,
        callback
      ) {
        if (
          orderListIndex >= orderList.length ||
          paymentListIndex >= paymentList.length
        ) {
          if (callback instanceof Function) {
            callback();
          }
          return;
        }

        var order = orderList.at(orderListIndex),
          payment = paymentList.at(paymentListIndex),
          paymentLine;

        if (payment.get('origAmount')) {
          var addPaymentLine = function(
            paymentLine,
            payment,
            addPaymentCallback
          ) {
            OB.UTIL.HookManager.executeHooks(
              'OBPOS_MultiOrderAddPaymentLine',
              {
                paymentLine: paymentLine,
                origPayment: payment
              },
              function(args) {
                order.addPayment(args.paymentLine, function() {
                  var amountToLayaway = order.get('amountToLayaway');
                  if (!OB.UTIL.isNullOrUndefined(amountToLayaway)) {
                    order.set(
                      'amountToLayaway',
                      OB.DEC.sub(
                        amountToLayaway,
                        args.paymentLine.get('origAmount')
                      )
                    );
                  }
                  if (addPaymentCallback instanceof Function) {
                    addPaymentCallback();
                  }
                });
              }
            );
          };

          if (order.get('amountToLayaway')) {
            setPickAndCarryPayments(
              orderList,
              paymentList,
              orderListIndex + 1,
              paymentListIndex,
              callback
            );
          } else {
            var pickAndCarryAmount = OBRDM.UTIL.checkPickAndCarryPaidAmount(
              order
            ).pickAndCarryAmount;

            if (
              pickAndCarryAmount > order.getPayment() &&
              !_.isUndefined(payment)
            ) {
              var amountToPay = OB.DEC.sub(
                pickAndCarryAmount,
                order.getPayment()
              );
              if (OB.DEC.compare(amountToPay) > 0) {
                var paymentMethod =
                  OB.MobileApp.model.paymentnames[payment.get('kind')];

                paymentLine = new OB.Model.PaymentLine();
                OB.UTIL.clone(payment, paymentLine);

                if (payment.get('origAmount') <= amountToPay) {
                  // Use all the remaining payment amount for this receipt
                  payment.set('origAmount', OB.DEC.Zero);
                  payment.set('amount', OB.DEC.Zero);
                  addPaymentLine(paymentLine, payment, function() {
                    setPickAndCarryPayments(
                      orderList,
                      paymentList,
                      orderListIndex,
                      paymentListIndex + 1,
                      callback
                    );
                  });
                } else {
                  // Get part of the payment and go with the next order
                  var amountToPayForeign = OB.DEC.mul(
                    amountToPay,
                    paymentMethod.mulrate,
                    paymentMethod.obposPosprecision
                  );
                  payment.set(
                    'origAmount',
                    OB.DEC.sub(payment.get('origAmount'), amountToPay)
                  );
                  payment.set(
                    'amount',
                    OB.DEC.sub(payment.get('amount'), amountToPayForeign)
                  );

                  paymentLine.set('origAmount', amountToPay);
                  paymentLine.set('amount', amountToPayForeign);

                  addPaymentLine(paymentLine, payment, function() {
                    setPickAndCarryPayments(
                      orderList,
                      paymentList,
                      orderListIndex + 1,
                      paymentListIndex,
                      callback
                    );
                  });
                }
              } else {
                // This order is already paid, go to the next order
                setPickAndCarryPayments(
                  orderList,
                  paymentList,
                  orderListIndex + 1,
                  paymentListIndex,
                  callback
                );
              }
            } else {
              setPickAndCarryPayments(
                orderList,
                paymentList,
                orderListIndex + 1,
                paymentListIndex,
                callback
              );
            }
          }
        } else {
          setPickAndCarryPayments(
            orderList,
            paymentList,
            orderListIndex,
            paymentListIndex + 1,
            callback
          );
        }
      };

      setPickAndCarryPayments(
        args.multiOrderList,
        args.payments,
        OB.DEC.Zero,
        OB.DEC.Zero,
        function() {
          OB.UTIL.HookManager.callbackExecutor(args, callbacks);
        }
      );
    } else {
      OB.UTIL.HookManager.callbackExecutor(args, callbacks);
    }
  }
);
