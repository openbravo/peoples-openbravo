/*
 ************************************************************************************
 * Copyright (C) 2017-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone , _ */

(function () {

  OB.UTIL.TicketCloseUtils = {};

  OB.UTIL.TicketCloseUtils.paymentAccepted = function (receipt, orderList, triggerClosedCallback) {
    receipt.setIsCalculateReceiptLockState(true);
    receipt.setIsCalculateGrossLockState(true);
    receipt.prepareToSend(function () {
      //Create the negative payment for change
      var clonedCollection = new Backbone.Collection(),
          paymentKind, i, totalPrePayment = OB.DEC.Zero,
          totalNotPrePayment = OB.DEC.Zero;
      var triggerReceiptClose = function (receipt) {

          // Adjust leave on credit payments.
          for (i = 0; i < receipt.get('payments').length; i++) {
            paymentKind = OB.MobileApp.model.paymentnames[receipt.get('payments').models[i].get('kind')];
            if (paymentKind && paymentKind.paymentMethod && paymentKind.paymentMethod.leaveascredit) {
              receipt.set('payment', OB.DEC.sub(receipt.get('payment'), receipt.get('payments').models[i].get('amount')));
              receipt.set('paidOnCredit', true);
            }
          }

          // There is only 1 receipt object.
          receipt.set('isBeingClosed', true);
          receipt.trigger('closed', {
            callback: function (args) {
              if (args.skipCallback) {
                triggerClosedCallback();
                return true;
              }
              receipt.set('isBeingClosed', false);

              _.each(orderList.models, function (ol) {
                if (ol.get('id') === receipt.get('id')) {
                  ol.set('isBeingClosed', false);
                  return true;
                }
              }, this);
              receipt.set('json', JSON.stringify(receipt.serializeToJSON()));
              OB.UTIL.setScanningFocus(true);
              OB.Dal.save(receipt, function () {
                OB.UTIL.Debug.execute(function () {
                  if (!args.frozenReceipt) {
                    throw "A clone of the receipt must be provided because it is possible that some rogue process could have changed it";
                  }
                  if (OB.UTIL.isNullOrUndefined(args.isCancelled)) { // allow boolean values
                    throw "The isCancelled flag must be set";
                  }
                });

                // verify that the receipt was not cancelled
                if (args.isCancelled !== true) {
                  var orderToPrint = OB.UTIL.clone(args.frozenReceipt);
                  orderToPrint.get('payments').reset();
                  clonedCollection.each(function (model) {
                    orderToPrint.get('payments').add(new Backbone.Model(model.toJSON()), {
                      silent: true
                    });
                  });
                  orderToPrint.set('hasbeenpaid', 'Y');
                  receipt.trigger('print', orderToPrint, {
                    offline: true
                  });

                  // Verify that the receipt has not been changed while the ticket has being closed
                  var diff = OB.UTIL.diffJson(receipt.serializeToJSON(), args.frozenReceipt.serializeToJSON());
                  // hasBeenPaid and cashUpReportInformation are the only difference allowed in the receipt
                  delete diff.hasbeenpaid;
                  delete diff.cashUpReportInformation;
                  // isBeingClosed is a flag only used to log purposes
                  delete diff.isBeingClosed;
                  // verify if there have been any modification to the receipt
                  var diffStringified = JSON.stringify(diff, undefined, 2);
                  if (diffStringified !== '{}') {
                    OB.error("The receipt has been modified while it was being closed:\n" + diffStringified + "\n");
                  }

                  if (OB.MobileApp.model.hasPermission('OBPOS_alwaysCreateNewReceiptAfterPayReceipt', true)) {
                    orderList.deleteCurrent(true);
                  } else {
                    orderList.deleteCurrent();
                  }
                  receipt.setIsCalculateReceiptLockState(false);
                  receipt.setIsCalculateGrossLockState(false);

                  orderList.synchronizeCurrentOrder();
                }
                triggerClosedCallback();
              }, null, false);
            }
          });
          };

      if (receipt.get('orderType') !== 2 && receipt.get('orderType') !== 3) {
        var negativeLines = _.filter(receipt.get('lines').models, function (line) {
          return line.get('qty') < 0;
        }).length;
        if (negativeLines === receipt.get('lines').models.length) {
          receipt.setOrderType('OBPOS_receipt.return', OB.DEC.One, {
            applyPromotions: false,
            saveOrder: false
          });
        } else {
          receipt.setOrderType('', OB.DEC.Zero, {
            applyPromotions: false,
            saveOrder: false
          });
        }
      }

      receipt.get('payments').each(function (model) {
        clonedCollection.add(new Backbone.Model(model.toJSON()));
        if (model.get('isPrePayment')) {
          totalPrePayment = OB.DEC.add(totalPrePayment, model.get('origAmount'));
        } else {
          totalNotPrePayment = OB.DEC.add(totalNotPrePayment, model.get('origAmount'));
        }
      });

      // Manage change payments (if there is change)
      if (receipt.get('changePayments')) {
        var addPaymentCallback = _.after(receipt.get('changePayments').length, function () {
          triggerReceiptClose(receipt);
        });
        receipt.get('changePayments').forEach(function (changePayment) {
          var paymentToAdd = OB.MobileApp.model.paymentnames[changePayment.key];
          receipt.addPayment(new OB.Model.PaymentLine({
            'kind': paymentToAdd.payment.searchKey,
            'name': paymentToAdd.payment.commercialName,
            'amount': OB.DEC.sub(0, changePayment.amount, paymentToAdd.obposPosprecision),
            'amountRounded': OB.DEC.sub(0, changePayment.amountRounded, paymentToAdd.obposPosprecision),
            'origAmount': OB.DEC.sub(0, changePayment.origAmount),
            'origAmountRounded': OB.DEC.sub(0, OB.DEC.mul(changePayment.amountRounded, paymentToAdd.rate)),
            'rate': paymentToAdd.rate,
            'mulrate': paymentToAdd.mulrate,
            'isocode': paymentToAdd.isocode,
            'allowOpenDrawer': paymentToAdd.paymentMethod.allowopendrawer,
            'isCash': paymentToAdd.paymentMethod.iscash,
            'openDrawer': paymentToAdd.paymentMethod.openDrawer,
            'printtwice': paymentToAdd.paymentMethod.printtwice,
            'paymentData': {
              'label': changePayment.label
            }
          }), addPaymentCallback);
        });
      }
    });
  };

  OB.UTIL.TicketCloseUtils.paymentDone = function (receipt, callbackPaymentAccepted, callbackOverpaymentExists, callbackPaymentAmountDistinctThanReceipt, callbackErrorCancelAndReplace, callbackErrorCancelAndReplaceOffline, callbackErrorOrderCancelled) {

    var isOrderCancelledProcess = new OB.DS.Process('org.openbravo.retail.posterminal.process.IsOrderCancelled'),
        triggerPaymentAccepted, triggerPaymentAcceptedImpl;

    triggerPaymentAccepted = function (openDrawer) {
      OB.UTIL.HookManager.executeHooks('OBPOS_PostPaymentDone', {
        receipt: receipt
      }, function (args) {
        if (args && args.cancellation && args.cancellation === true) {
          receipt.trigger('paymentCancel');
          return;
        }
        if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
          OB.MobileApp.model.setSynchronizedCheckpoint(triggerPaymentAcceptedImpl);
        } else {
          triggerPaymentAcceptedImpl(openDrawer);
        }
      });
    };

    triggerPaymentAcceptedImpl = function (openDrawer) {
      if (receipt.get('doCancelAndReplace') && receipt.get('replacedorder')) {
        isOrderCancelledProcess.exec({
          orderId: receipt.get('replacedorder'),
          documentNo: receipt.get('documentNo')
        }, function (data) {
          if (data && data.exception) {
            if (data.exception.message) {
              callbackErrorCancelAndReplace(data.exception.message);
              return;
            }
            callbackErrorCancelAndReplaceOffline();
            return;
          } else if (data && data.orderCancelled) {
            callbackErrorOrderCancelled();
            return;
          } else {
            if (_.isUndefined(_.find(receipt.get('lines').models, function (line) {
              var qty = line.get('qty') ? line.get('qty') : 0,
                  deliveredQuantity = line.get('deliveredQuantity') ? line.get('deliveredQuantity') : 0;
              return (qty > 0 && qty > deliveredQuantity) || (qty < 0 && qty < deliveredQuantity);
            }))) {
              receipt.set('generateShipment', false);
            }
            callbackPaymentAccepted(openDrawer);
          }
        }, function () {
          callbackErrorCancelAndReplaceOffline();
        });
      } else {
        callbackPaymentAccepted(openDrawer);
      }
    };

    if (receipt.overpaymentExists()) {
      callbackOverpaymentExists(function (result) {
        if (result) {
          triggerPaymentAccepted(false);
        }
      });
    } else if (OB.DEC.abs(receipt.getPayment()) !== OB.DEC.abs(receipt.getGross()) && _.isUndefined(receipt.get('paidInNegativeStatusAmt')) && !receipt.isLayaway() && !receipt.get('paidOnCredit')) {
      callbackPaymentAmountDistinctThanReceipt(function (result) {
        if (result === true) {
          triggerPaymentAccepted(false);
        } else {
          return;
        }
      });
    } else {
      triggerPaymentAccepted(true);
    }
  };

  OB.UTIL.getChangeLabelFromChangePayments = function (changePayments) {
    return changePayments.map(function (item) {
      return item.label;
    }).join(' + ');
  };

  OB.UTIL.getChangeLabelFromPayments = function (payments) {
    return payments.filter(function (payment) {
      return payment.get('amount') < 0;
    }).map(function (payment) {
      return payment.get('paymentData').label;
    }).join(' + ');
  };
}());