/*
 ************************************************************************************
 * Copyright (C) 2017-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone , _ */

(function() {
  OB.UTIL.TicketCloseUtils = {};

  OB.UTIL.TicketCloseUtils.processChangePayments = function(receipt, callback) {
    var mergeable, addPaymentCallback, prevChange;

    // Manage change payments (if there is change)
    if (receipt.get('changePayments') && receipt.get('changePayments').length) {
      prevChange = receipt.get('change');
      mergeable =
        !OB.MobileApp.model.get('terminal').multiChange &&
        !OB.MobileApp.model.hasPermission('OBPOS_SplitChange', true);
      addPaymentCallback = _.after(
        receipt.get('changePayments').length,
        function() {
          // Set the 'payment' and 'paymentWithSign' attributes
          var paidAmt = OB.DEC.Zero;
          _.each(receipt.get('payments').models, function(payment) {
            if (
              payment.get('isPrePayment') ||
              payment.get('isReversePayment') ||
              !receipt.isNegative()
            ) {
              paidAmt = OB.DEC.add(paidAmt, payment.get('origAmount'));
            } else {
              paidAmt = OB.DEC.sub(paidAmt, payment.get('origAmount'));
            }
          });
          receipt.set('payment', OB.DEC.abs(paidAmt), {
            silent: true
          });
          receipt.set('paymentWithSign', paidAmt, {
            silent: true
          });
          // restore attributes
          receipt.set('change', prevChange, {
            silent: true
          });
          callback();
        }
      );

      receipt.get('changePayments').forEach(function(changePayment) {
        var paymentToAdd = OB.MobileApp.model.paymentnames[changePayment.key];
        receipt.addPayment(
          new OB.Model.PaymentLine({
            kind: paymentToAdd.payment.searchKey,
            name: paymentToAdd.payment.commercialName,
            amount: OB.DEC.sub(
              0,
              changePayment.amount,
              paymentToAdd.obposPosprecision
            ),
            amountRounded: OB.DEC.sub(
              0,
              changePayment.amountRounded,
              paymentToAdd.obposPosprecision
            ),
            origAmount: OB.DEC.sub(0, changePayment.origAmount),
            origAmountRounded: OB.DEC.sub(
              0,
              OB.DEC.mul(changePayment.amountRounded, paymentToAdd.rate)
            ),
            rate: paymentToAdd.rate,
            mulrate: paymentToAdd.mulrate,
            isocode: paymentToAdd.isocode,
            allowOpenDrawer: paymentToAdd.paymentMethod.allowopendrawer,
            isCash: paymentToAdd.paymentMethod.iscash,
            openDrawer: paymentToAdd.paymentMethod.openDrawer,
            printtwice: paymentToAdd.paymentMethod.printtwice,
            changePayment: true,
            paymentData: {
              mergeable: mergeable,
              label: changePayment.label
            }
          }),
          addPaymentCallback
        );
      });
    } else {
      callback();
    }
  };

  OB.UTIL.TicketCloseUtils.paymentAccepted = function(
    receipt,
    orderList,
    triggerClosedCallback
  ) {
    receipt.setIsCalculateReceiptLockState(true);
    receipt.setIsCalculateGrossLockState(true);
    var execution = OB.UTIL.ProcessController.start('completeReceipt');
    receipt.prepareToSend(function() {
      //Create the negative payment for change
      var clonedCollection = new Backbone.Collection(),
        paymentKind,
        i,
        totalPrePayment = OB.DEC.Zero,
        totalNotPrePayment = OB.DEC.Zero;

      if (receipt.get('orderType') !== 2 && receipt.get('orderType') !== 3) {
        var negativeLines = _.filter(receipt.get('lines').models, function(
          line
        ) {
          return line.get('qty') < 0;
        }).length;
        if (
          negativeLines === receipt.get('lines').models.length ||
          (negativeLines > 0 &&
            OB.MobileApp.model.get('permissions')
              .OBPOS_SalesWithOneLineNegativeAsReturns)
        ) {
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

      receipt.get('payments').each(function(model) {
        clonedCollection.add(new Backbone.Model(model.toJSON()));
        if (model.get('isPrePayment')) {
          totalPrePayment = OB.DEC.add(
            totalPrePayment,
            model.get('origAmount')
          );
        } else {
          totalNotPrePayment = OB.DEC.add(
            totalNotPrePayment,
            model.get('origAmount')
          );
        }
      });

      // Adjust leave on credit payments.
      for (i = 0; i < receipt.get('payments').length; i++) {
        paymentKind =
          OB.MobileApp.model.paymentnames[
            receipt.get('payments').models[i].get('kind')
          ];
        if (
          paymentKind &&
          paymentKind.paymentMethod &&
          paymentKind.paymentMethod.leaveascredit
        ) {
          receipt.set(
            'payment',
            OB.DEC.sub(
              receipt.get('payment'),
              receipt.get('payments').models[i].get('amount')
            )
          );
          receipt.set('payOnCredit', true);
        }
      }

      // There is only 1 receipt object.
      receipt.set('isBeingClosed', true);
      receipt.trigger('closed', {
        callback: function(args) {
          if (args.skipCallback) {
            OB.UTIL.ProcessController.finish('completeReceipt', execution);
            if (triggerClosedCallback instanceof Function) {
              triggerClosedCallback();
            }
            return true;
          }
          receipt.set('isBeingClosed', false);

          // Not needed now. Current ticket already closed in the previous line
          // _.each(
          //     orderList.models,
          //     function (ol) {
          //         if (ol.get('id') === receipt.get('id')) {
          //             ol.set('isBeingClosed', false);
          //             return true;
          //         }
          //     },
          //     this
          // );
          OB.UTIL.setScanningFocus(true);
          OB.UTIL.Debug.execute(function() {
            if (!args.frozenReceipt) {
              throw 'A clone of the receipt must be provided because it is possible that some rogue process could have changed it';
            }
            if (OB.UTIL.isNullOrUndefined(args.isCancelled)) {
              // allow boolean values
              throw 'The isCancelled flag must be set';
            }
          });

          // verify that the receipt was not cancelled
          if (args.isCancelled !== true) {
            var orderToPrint = OB.UTIL.clone(args.frozenReceipt),
              invoice = orderToPrint.get('calculatedInvoice');
            orderToPrint.get('payments').reset();
            clonedCollection.each(function(model) {
              orderToPrint
                .get('payments')
                .add(new Backbone.Model(model.toJSON()), {
                  silent: true
                });
            });
            orderToPrint.set('hasbeenpaid', 'Y');
            receipt.trigger('print', orderToPrint, {
              offline: true
            });
            if (invoice && invoice.get('id')) {
              var invoiceToPrint = OB.UTIL.clone(invoice),
                printInvoice = function() {
                  invoiceToPrint.get('payments').reset();
                  clonedCollection.each(function(model) {
                    invoiceToPrint
                      .get('payments')
                      .add(new Backbone.Model(model.toJSON()), {
                        silent: true
                      });
                  });
                  invoiceToPrint.set('hasbeenpaid', 'Y');
                  receipt.trigger('print', invoiceToPrint, {
                    offline: true
                  });
                };
              _.each(invoice.get('lines').models, function(invoiceLine) {
                invoiceLine.unset('product');
              });
              if (
                !OB.MobileApp.model.hasPermission(
                  'OBPOS_print.return_invoice',
                  true
                )
              ) {
                var positiveLine = _.find(receipt.get('lines').models, function(
                  line
                ) {
                  return line.get('qty') >= 0;
                });
                if (!positiveLine) {
                  OB.UTIL.showConfirmation.display(
                    OB.I18N.getLabel('OBPOS_LblPrintInvoices'),
                    OB.I18N.getLabel('OBPOS_LblPrintInvoicesReturn'),
                    [
                      {
                        label: OB.I18N.getLabel('OBMOBC_LblOk'),
                        action: function() {
                          printInvoice();
                        }
                      },
                      {
                        label: OB.I18N.getLabel('OBMOBC_LblCancel')
                      }
                    ],
                    {
                      autoDismiss: false
                    }
                  );
                } else {
                  if (
                    OB.MobileApp.model.hasPermission(
                      'OBPOS_print.invoicesautomatically',
                      true
                    )
                  ) {
                    printInvoice();
                  }
                }
              } else {
                if (
                  OB.MobileApp.model.hasPermission(
                    'OBPOS_print.invoicesautomatically',
                    true
                  )
                ) {
                  printInvoice();
                }
              }
            }

            // Verify that the receipt has not been changed while the ticket has being closed
            var diff = OB.UTIL.diffJson(
              receipt.serializeToJSON(),
              args.diffReceipt.serializeToJSON()
            );
            // hasBeenPaid and cashUpReportInformation are the only difference allowed in the receipt
            delete diff.hasbeenpaid;
            delete diff.cashUpReportInformation;
            // isBeingClosed is a flag only used to log purposes
            delete diff.isBeingClosed;
            // verify if there have been any modification to the receipt
            var diffStringified = JSON.stringify(diff, undefined, 2);
            if (diffStringified !== '{}') {
              OB.warn(
                'The receipt has been modified while it was being closed:\n' +
                  diffStringified +
                  '\n'
              );
            }

            let payload = {};
            if (
              OB.MobileApp.model.hasPermission(
                'OBPOS_alwaysCreateNewReceiptAfterPayReceipt',
                true
              )
            ) {
              payload = {
                forceCreateNew: true
              };
            }

            OB.UTIL.TicketListUtils.removeTicket(payload).then(() => {
              OB.MobileApp.model.receipt.setIsCalculateReceiptLockState(false);
              OB.MobileApp.model.receipt.setIsCalculateGrossLockState(false);

              OB.UTIL.ProcessController.finish('completeReceipt', execution);
              if (triggerClosedCallback instanceof Function) {
                triggerClosedCallback();
              }
            });
            return;
          }
          OB.UTIL.ProcessController.finish('completeReceipt', execution);
          if (triggerClosedCallback instanceof Function) {
            triggerClosedCallback();
          }
        }
      });
    });
  };

  OB.UTIL.TicketCloseUtils.paymentDone = function(
    receipt,
    callbackPaymentAccepted,
    callbackOverpaymentExists,
    callbackPaymentAmountDistinctThanReceipt,
    callbackErrorCancelAndReplace,
    callbackErrorCancelAndReplaceOffline,
    callbackErrorOrderCancelled,
    callbackPaymentCancelled
  ) {
    var triggerPaymentAccepted, triggerPaymentAcceptedImpl;

    triggerPaymentAccepted = function(openDrawer) {
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PostPaymentDone',
        {
          receipt: receipt
        },
        function(args) {
          if (args && args.cancellation && args.cancellation === true) {
            callbackPaymentCancelled();
            return;
          }
          if (
            OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)
          ) {
            OB.MobileApp.model.setSynchronizedCheckpoint(
              triggerPaymentAcceptedImpl
            );
          } else {
            triggerPaymentAcceptedImpl(openDrawer);
          }
        }
      );
    };

    triggerPaymentAcceptedImpl = function(openDrawer) {
      if (receipt.get('doCancelAndReplace') && receipt.get('replacedorder')) {
        receipt.canCancelOrder(
          receipt.get('canceledorder'),
          null,
          function(data) {
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
              callbackPaymentAccepted(openDrawer);
            }
          },
          function() {
            callbackErrorCancelAndReplaceOffline();
          }
        );
      } else {
        callbackPaymentAccepted(openDrawer);
      }
    };

    if (receipt.overpaymentExists()) {
      callbackOverpaymentExists(function(result) {
        if (result) {
          triggerPaymentAccepted(false);
        } else {
          callbackPaymentCancelled();
        }
      });
    } else if (
      receipt.getPayment() !== OB.DEC.abs(receipt.getGross()) &&
      !receipt.isLayaway() &&
      !receipt.get('payOnCredit') &&
      OB.DEC.abs(receipt.get('obposPrepaymentamt')) ===
        OB.DEC.abs(receipt.getGross()) &&
      !OB.MobileApp.model.get('terminal').terminalType.calculateprepayments
    ) {
      callbackPaymentAmountDistinctThanReceipt(function(result) {
        if (result === true) {
          triggerPaymentAccepted(false);
        } else {
          callbackPaymentCancelled();
        }
      });
    } else {
      triggerPaymentAccepted(true);
    }
  };

  OB.UTIL.getChangeLabelFromReceipt = function(receipt) {
    if (receipt.get('changePayments')) {
      return OB.UTIL.getChangeLabelFromChangePayments(
        receipt.get('changePayments')
      );
    } else {
      return OB.UTIL.getChangeLabelFromPayments(receipt.get('payments'));
    }
  };

  OB.UTIL.getChangeLabelFromChangePayments = function(changePayments) {
    return changePayments
      .map(function(item) {
        return item.label;
      })
      .join(' + ');
  };

  OB.UTIL.getChangeLabelFromPayments = function(payments) {
    return payments
      .filter(function(payment) {
        return payment.get('paymentData') && payment.get('paymentData').label;
      })
      .map(function(payment) {
        return payment.get('paymentData').label;
      })
      .join(' + ');
  };

  OB.UTIL.TicketCloseUtils.checkOrdersUpdated = function(
    order,
    successCallback,
    errorCallback
  ) {
    var errorPopup = function(offline, errorType) {
      var title = '',
        message = '',
        buttons = [];
      if (
        offline &&
        !OB.MobileApp.model.hasPermission(
          'OBPOS_AllowToSynchronizeLoadedReceiptsOffline',
          true
        )
      ) {
        title = OB.I18N.getLabel('OBPOS_ErrorCheckingReceipt');
        message = OB.I18N.getLabel('OBPOS_NotPossibleToConfirmReceipt', [
          order.get('documentNo')
        ]);
        buttons.push({
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function() {
            errorCallback();
          }
        });
      } else if (
        offline &&
        OB.MobileApp.model.hasPermission(
          'OBPOS_AllowToSynchronizeLoadedReceiptsOffline',
          true
        )
      ) {
        title = OB.I18N.getLabel('OBPOS_UpdatedReceipt');
        message = OB.I18N.getLabel('OBPOS_NotPossibleToConfirmReceiptWarn', [
          order.get('documentNo')
        ]);
        buttons.push({
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function() {
            successCallback();
          }
        });
        buttons.push({
          label: OB.I18N.getLabel('OBMOBC_LblCancel'),
          action: function() {
            errorCallback();
          }
        });
      } else {
        title = OB.I18N.getLabel('OBPOS_ErrorCheckingReceipt');
        if (errorType === 'P') {
          message = OB.I18N.getLabel('OBPOS_SyncPending', [
            order.get('documentNo')
          ]);
        } else if (errorType === 'E') {
          message = OB.I18N.getLabel('OBPOS_SyncWithErrors', [
            order.get('documentNo')
          ]);
        } else {
          message = OB.I18N.getLabel('OBPOS_RemoveAndLoad', [
            order.get('documentNo')
          ]);
        }

        buttons.push({
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function() {
            errorCallback();
          }
        });
      }
      OB.UTIL.showConfirmation.display(title, message, buttons, {
        onHideFunction: function() {
          errorCallback();
        }
      });
    };

    if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
      successCallback();
      return;
    }

    if (!order.get('isLayaway') && !order.get('isPaid')) {
      successCallback();
      return;
    }

    if (!OB.MobileApp.model.get('connectedToERP') || !navigator.onLine) {
      errorPopup(true);
      return;
    }

    var checkUpdated = new OB.DS.Process(
        'org.openbravo.retail.posterminal.process.CheckUpdated'
      ),
      _order = {
        id: order.id,
        loaded: order.get('loaded'),
        lines: []
      };
    _.each(order.get('lines').models, function(line) {
      var _line = {
        id: line.id,
        loaded: line.get('loaded')
      };
      _order.lines.push(_line);
    });
    checkUpdated.exec(
      {
        order: _order
      },
      function(data) {
        if (data) {
          if (data.exception) {
            errorPopup(false);
          } else if (data.error) {
            errorPopup(false, data.type);
          } else {
            successCallback();
          }
        } else {
          errorCallback();
        }
      },
      function(data) {
        errorPopup(true);
      }
    );
  };
})();
