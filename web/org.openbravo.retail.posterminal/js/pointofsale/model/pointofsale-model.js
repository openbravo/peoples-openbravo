/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo, _*/

OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
OB.OBPOSPointOfSale.Model = OB.OBPOSPointOfSale.Model || {};
OB.OBPOSPointOfSale.UI = OB.OBPOSPointOfSale.UI || {};

//Window model
OB.OBPOSPointOfSale.Model.PointOfSale = OB.Model.TerminalWindowModel.extend({
  models: [OB.Model.Order],

  loadUnpaidOrders: function(loadUnpaidOrdersCallback) {
    // Shows a modal window with the orders pending to be paid
    var orderlist = this.get('orderList'),
      model = this,
      reCalculateReceipt = false;

    let ordersNotPaid = OB.App.OpenTicketList.getAllTickets().filter(
      ticket => ticket.hasbeenpaid === 'N'
    );

    let currentOrder = {},
      loadOrderStr;

    // Removing Orders which are created in other users session
    ordersNotPaid = ordersNotPaid.filter(order => {
      return order.session !== OB.MobileApp.model.get('session');
    });

    OB.UTIL.HookManager.executeHooks(
      'OBPOS_PreLoadUnpaidOrdersHook',
      {
        ordersNotPaid: ordersNotPaid,
        model: model
      },
      function(args) {
        OB.MobileApp.model.on(
          'window:ready',
          function() {
            OB.MobileApp.model.off('window:ready', null, model);
            const addNewOrderCallback = () => {
              if (!args.ordersNotPaid || args.ordersNotPaid.length === 0) {
                // If there are no pending orders,
                //  add an initial empty order
                OB.App.State.Ticket.createEmptyTicket();
              } else {
                // The order object is stored in the json property of the row fetched from the database
                orderlist.reset(new Backbone.Collection(args.ordersNotPaid));
                // At this point it is sure that there exists at least one order
                // Function to continue of there is some error
                currentOrder = args.ordersNotPaid[0];
                //removing Orders lines without mandatory fields filled
                OB.UTIL.HookManager.executeHooks(
                  'OBPOS_CheckReceiptMandatoryFields',
                  {
                    orders: orderlist.models // local backbone array, not OrderList instance
                  },
                  function(args) {
                    reCalculateReceipt = args.reCalculateReceipt;
                    OB.UTIL.TicketListUtils.loadTicket(currentOrder).then(
                      () => {
                        if (reCalculateReceipt) {
                          OB.MobileApp.model.receipt.calculateGrossAndSave();
                        }
                        loadOrderStr =
                          OB.I18N.getLabel('OBPOS_Order') +
                          currentOrder.documentNo +
                          OB.I18N.getLabel('OBPOS_Loaded');
                        OB.UTIL.showAlert.display(
                          loadOrderStr,
                          OB.I18N.getLabel('OBPOS_Info')
                        );
                      }
                    );
                  }
                );
              }
            };
            if (
              OB.MobileApp.model.get('terminal').terminalType.safebox &&
              OB.UTIL.isNullOrUndefined(
                OB.UTIL.localStorage.getItem('currentSafeBox')
              )
            ) {
              OB.MobileApp.view.$.containerWindow.getRoot().doShowPopup({
                popup: 'OBPOS_modalSafeBox',
                args: {
                  callback: addNewOrderCallback
                }
              });
            } else {
              addNewOrderCallback();
            }
          },
          model
        );
        loadUnpaidOrdersCallback();
      }
    );
  },

  loadCheckedMultiorders: function() {
    if (!this.get('leftColumnViewManager').isMultiOrder()) {
      return false;
    }
    var checkedMultiOrders,
      multiOrders = this.get('multiOrders'),
      multiOrderList = multiOrders.get('multiOrdersList'),
      me = this,
      criteria = {
        hasbeenpaid: 'N',
        session: OB.MobileApp.model.get('session')
      };
    OB.Dal.find(
      OB.Model.Order,
      criteria,
      function(possibleMultiOrder) {
        //OB.Dal.find success
        if (possibleMultiOrder && possibleMultiOrder.length > 0) {
          checkedMultiOrders = _.compact(
            possibleMultiOrder.map(function(e) {
              if (e.get('checked')) {
                return e;
              }
            })
          );

          multiOrderList.reset(checkedMultiOrders);

          // MultiOrder payments
          var multiOrderAddPayments = function() {
            var payments = JSON.parse(
              OB.UTIL.localStorage.getItem('multiOrdersPayment')
            );
            _.each(payments, function(payment) {
              multiOrders.addPayment(new OB.Model.PaymentLine(payment));
            });
          };
          multiOrderList.trigger('loadedMultiOrder', multiOrderAddPayments);
        } else if (me.isValidMultiOrderState()) {
          multiOrders.resetValues();
          me.get('leftColumnViewManager').setOrderMode();
        }
      },
      function() {
        // If there is an error fetching the checked orders of multiorders,
        //OB.Dal.find error
      }
    );
  },
  isValidMultiOrderState: function() {
    if (this.get('leftColumnViewManager') && this.get('multiOrders')) {
      return (
        this.get('leftColumnViewManager').isMultiOrder() &&
        this.get('multiOrders').hasDataInList()
      );
    }
    return false;
  },
  getPending: function() {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getPending();
    } else {
      return this.get('multiOrders').getPending();
    }
  },
  getChange: function() {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getChange();
    } else {
      return this.get('multiOrders').getChange();
    }
  },
  getTotal: function() {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getTotal();
    } else {
      return this.get('multiOrders').getTotal();
    }
  },
  getPrepaymentAmount: function() {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').get('obposPrepaymentamt');
    } else {
      return this.get('multiOrders').get('obposPrepaymentamt');
    }
  },
  getPayment: function() {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getPayment();
    } else {
      return this.get('multiOrders').getPayment();
    }
  },
  addPayment: function(payment, callback) {
    var modelToIncludePayment;

    if (this.get('leftColumnViewManager').isOrder()) {
      modelToIncludePayment = this.get('order');
    } else {
      modelToIncludePayment = this.get('multiOrders');
    }

    modelToIncludePayment.addPayment(payment, callback);
  },
  deleteMultiOrderList: function() {
    _.each(
      this.get('multiOrders').get('multiOrdersList').models,
      function(order) {
        if (order.get('originalOrderType') !== order.get('orderType')) {
          order.setOrderType(null, order.get('originalOrderType'));
        }
        order.unset('amountToLayaway');
        order.unset('originalOrderType');
        order.unset('belongsToMultiOrder');
        if (order.get('loadedFromServer')) {
          this.get('orderList').current = order;
          this.get('orderList').deleteCurrent();
        }
      },
      this
    );
    return true;
  },
  init: function() {
    OB.error(
      'This init method should never be called for this model. Call initModels and loadModels instead'
    );
    this.initModels(function() {});
    this.loadModels(function() {});
  },

  initModels: function(callback) {
    var me = this;

    // create and expose the receipt
    var receipt = new OB.Model.Order();
    // fire events if the receipt model is the target of the OB.UTIL.clone method
    receipt.triggerEventsIfTargetOfSourceWhenCloning = function() {
      return true;
    };
    OB.MobileApp.model.receipt = receipt;

    // create the multiOrders and expose it
    var multiOrders = new OB.Model.MultiOrders();
    OB.MobileApp.model.multiOrders = multiOrders;
    // create the orderList and expose it
    // TODO: orderList should be removed
    var orderList = new OB.Collection.OrderList(receipt);
    OB.MobileApp.model.orderList = orderList;

    // changing this initialization order may break the loading
    this.set('order', receipt);
    this.set('orderList', orderList);
    this.set('customer', new OB.Model.BusinessPartner());
    this.set('customerAddr', new OB.Model.BPLocation());
    this.set('multiOrders', multiOrders);
    OB.DATA.CustomerSave(this);
    OB.DATA.CustomerAddrSave(this);
    OB.DATA.OrderSave(this);
    OB.DATA.OrderTaxes(receipt);

    this.printLine = new OB.OBPOSPointOfSale.Print.ReceiptLine(receipt);

    var ViewManager = Backbone.Model.extend({
      defaults: {
        currentWindow: {
          name: 'mainSubWindow',
          params: []
        }
      },
      initialize: function() {}
    });

    var LeftColumnViewManager = Backbone.Model.extend({
      defaults: {
        currentView: {}
      },
      initialize: function() {
        this.on(
          'change:currentView',
          function(changedModel) {
            OB.UTIL.localStorage.setItem(
              'leftColumnCurrentView',
              JSON.stringify(changedModel.get('currentView'))
            );
            this.trigger(changedModel.get('currentView').name);
            OB.MobileApp.model.set(
              'isMultiOrderState',
              changedModel.get('currentView').name === 'order' ? false : true
            );
          },
          this
        );
      },
      setOrderMode: function(parameters) {
        this.set('currentView', {
          name: 'order',
          params: parameters
        });
        OB.UTIL.localStorage.setItem(
          'leftColumnCurrentView',
          JSON.stringify(this.get('currentView'))
        );
      },
      isOrder: function() {
        if (this.get('currentView').name === 'order') {
          return true;
        }
        return false;
      },
      setMultiOrderMode: function(parameters) {
        this.set('currentView', {
          name: 'multiorder',
          params: parameters
        });
      },
      isMultiOrder: function() {
        if (this.get('currentView').name === 'multiorder') {
          return true;
        }
        return false;
      }
    });

    this.set('leftColumnViewManager', new LeftColumnViewManager());
    this.set('subWindowManager', new ViewManager());

    OB.MobileApp.model.runSyncProcess(
      function() {
        OB.RR.RequestRouter.sendAllMessages();
        me.loadCheckedMultiorders();
      },
      function() {
        OB.RR.RequestRouter.sendAllMessages();
        me.loadCheckedMultiorders();
      }
    );

    this.checkOpenDrawer = function() {
      if (me.openDrawer) {
        OB.POS.hwserver.openDrawer(
          {
            openFirst: true,
            receipt: me.get('leftColumnViewManager').isMultiOrder()
              ? me.get('multiOrders')
              : receipt
          },
          OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales
        );
      }
    };

    var isSlowDevice =
      OB.UTIL.localStorage.getItem('benchmarkScore') &&
      parseInt(OB.UTIL.localStorage.getItem('benchmarkScore'), 10) < 1000;

    // If the device is too slow and the preference allows it, or the terminal type is configured, a block screen is shown if the calculation of the receipt is taking more than 1 sec
    if (
      (OB.MobileApp.model.get('terminal') &&
        OB.MobileApp.model.get('terminal').terminalType &&
        OB.MobileApp.model.get('terminal').terminalType
          .processingblockscreen) ||
      (isSlowDevice &&
        OB.MobileApp.model.hasPermission(
          'OBPOS_processingBlockScreenOnSlowDevices',
          true
        ))
    ) {
      var execution;
      receipt.on('calculatingReceipt', function() {
        setTimeout(function() {
          if (receipt.calculatingReceipt === true) {
            execution = OB.UTIL.ProcessController.start('slowCalculateReceipt');
          }
        }, 1000);
      });

      receipt.on('calculatedReceipt', function() {
        OB.UTIL.ProcessController.finish('slowCalculateReceipt', execution);
      });
    }

    receipt.on('checkOpenDrawer', function() {
      me.checkOpenDrawer();
    });

    this.get('multiOrders').on('checkOpenDrawer', function() {
      me.checkOpenDrawer();
    });

    receipt.on(
      'paymentAccepted',
      function() {
        OB.UTIL.TicketCloseUtils.paymentAccepted(
          receipt,
          OB.App.OpenTicketList.getAllTickets(),
          null
        );
      },
      this
    );

    receipt.on(
      'paymentDone',
      function(openDrawer) {
        if (OB.UTIL.ProcessController.isProcessActive('paymentDone')) {
          return true;
        }
        var execution = OB.UTIL.ProcessController.start('paymentDone');

        function callbackPaymentAccepted(allowedOpenDrawer) {
          if (allowedOpenDrawer) {
            me.openDrawer = openDrawer;
          }
          receipt.trigger('paymentAccepted');
          OB.UTIL.ProcessController.finish('paymentDone', execution);
        }

        function callbackPaymentCancelled(callbackToExecuteAfter) {
          OB.UTIL.ProcessController.finish('paymentDone', execution);
          receipt.unset('completeTicket');
          // Review this showLoading false
          //OB.UTIL.showLoading(false);
          if (callbackToExecuteAfter instanceof Function) {
            callbackToExecuteAfter();
          }
        }

        function callbackOverpaymentExist(callback) {
          var symbol = OB.MobileApp.model.get('terminal').symbol;
          var symbolAtRight = OB.MobileApp.model.get('terminal')
            .currencySymbolAtTheRight;
          var amount = receipt.getPaymentStatus().overpayment;
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_OverpaymentWarningTitle'),
            OB.I18N.getLabel('OBPOS_OverpaymentWarningBody', [
              OB.I18N.formatCurrencyWithSymbol(amount, symbol, symbolAtRight)
            ]),
            [
              {
                label: OB.I18N.getLabel('OBMOBC_LblOk'),
                isConfirmButton: true,
                action: function() {
                  me.openDrawer = openDrawer;
                  // Need to finish process here??
                  callback(true);
                }
              },
              {
                label: OB.I18N.getLabel('OBMOBC_LblCancel'),
                action: function() {
                  callbackPaymentCancelled(function() {
                    callback(false);
                  });
                }
              }
            ],
            {
              autoDismiss: false,
              hideCloseButton: true
            }
          );
        }

        function callbackPaymentAmountDistinctThanReceipt() {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel(
              'OBPOS_PaymentAmountDistinctThanReceiptAmountTitle'
            ),
            OB.I18N.getLabel(
              'OBPOS_PaymentAmountDistinctThanReceiptAmountBody'
            ),
            [
              {
                label: OB.I18N.getLabel('OBMOBC_LblOk'),
                isConfirmButton: true,
                action: function() {
                  // Need to finish process here??
                  callback(true);
                }
              },
              {
                label: OB.I18N.getLabel('OBMOBC_LblCancel'),
                action: function() {
                  callbackPaymentCancelled(function() {
                    callback(false);
                  });
                }
              }
            ]
          );
        }

        function callbackErrorCancelAndReplace(errorMessage) {
          callbackPaymentCancelled(function() {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              errorMessage
            );
          });
        }

        function callbackErrorCancelAndReplaceOffline() {
          callbackPaymentCancelled(function() {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBMOBC_OfflineWindowRequiresOnline')
            );
          });
        }

        function callbackErrorOrderCancelled() {
          callbackPaymentCancelled(function() {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBMOBC_Error'),
              OB.I18N.getLabel('OBPOS_OrderReplacedError')
            );
          });
        }

        OB.UTIL.TicketCloseUtils.paymentDone(
          receipt,
          callbackPaymentAccepted,
          callbackOverpaymentExist,
          callbackPaymentAmountDistinctThanReceipt,
          callbackErrorCancelAndReplace,
          callbackErrorCancelAndReplaceOffline,
          callbackErrorOrderCancelled,
          callbackPaymentCancelled
        );
      },
      this
    );

    this.get('multiOrders').on(
      'paymentAccepted',
      function() {
        var multiorders = this.get('multiOrders');
        var auxRcpt, auxP;

        OB.UTIL.showLoading(true);

        //clone multiorders
        multiorders.set('frozenMultiOrdersList', new Backbone.Collection());
        multiorders.get('multiOrdersList').forEach(function(rcpt) {
          auxRcpt = new OB.Model.Order();
          OB.UTIL.clone(rcpt, auxRcpt);
          multiorders.get('frozenMultiOrdersList').add(auxRcpt);
        });

        //clone multiorders
        multiorders.set('frozenPayments', new Backbone.Collection());
        multiorders.get('payments').forEach(function(p) {
          auxP = new OB.Model.PaymentLine();
          OB.UTIL.clone(p, auxP);
          multiorders.get('frozenPayments').add(auxP);
        });

        function updateAmountToLayaway(order, amount) {
          var amountToLayaway = order.get('amountToLayaway');
          if (!OB.UTIL.isNullOrUndefined(amountToLayaway)) {
            order.set('amountToLayaway', OB.DEC.sub(amountToLayaway, amount));
          }
        }

        var setPaymentsToReceipts;
        setPaymentsToReceipts = function(
          orderList,
          paymentList,
          changePayments,
          orderListIndex,
          paymentListIndex,
          considerPrepaymentAmount,
          callback
        ) {
          if (
            orderListIndex >= orderList.length ||
            paymentListIndex >= paymentList.length
          ) {
            if (
              paymentListIndex < paymentList.length &&
              considerPrepaymentAmount
            ) {
              setPaymentsToReceipts(
                orderList,
                paymentList,
                changePayments,
                0,
                paymentListIndex,
                false,
                callback
              );
            } else if (callback instanceof Function) {
              // Finished
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
                    updateAmountToLayaway(
                      order,
                      args.paymentLine.get('origAmount')
                    );
                    if (addPaymentCallback instanceof Function) {
                      addPaymentCallback();
                    }
                  });
                }
              );
            };

            if (
              orderListIndex === orderList.length - 1 &&
              !considerPrepaymentAmount
            ) {
              // Transfer everything
              order.set('changePayments', changePayments);
              if (paymentListIndex < paymentList.length) {
                // Pending payments to add
                paymentLine = new OB.Model.PaymentLine();
                OB.UTIL.clone(payment, paymentLine);
                paymentLine.set('forceAddPayment', true);

                payment.set('origAmount', OB.DEC.Zero);
                payment.set('amount', OB.DEC.Zero);
                addPaymentLine(paymentLine, payment, function() {
                  setPaymentsToReceipts(
                    orderList,
                    paymentList,
                    changePayments,
                    orderListIndex,
                    paymentListIndex + 1,
                    considerPrepaymentAmount,
                    callback
                  );
                });
              } else {
                // No more payments to add, finish the process
                if (callback instanceof Function) {
                  // Process finished
                  callback();
                }
              }
            } else {
              var amountToPay;
              if (!OB.UTIL.isNullOrUndefined(order.get('amountToLayaway'))) {
                amountToPay = order.get('amountToLayaway');
              } else if (considerPrepaymentAmount) {
                amountToPay = OB.DEC.sub(
                  order.get('obposPrepaymentamt')
                    ? order.get('obposPrepaymentamt')
                    : order.get('gross'),
                  order.get('payment')
                );
              } else {
                amountToPay = OB.DEC.sub(
                  order.get('gross'),
                  order.get('payment')
                );
              }
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
                    setPaymentsToReceipts(
                      orderList,
                      paymentList,
                      changePayments,
                      orderListIndex,
                      paymentListIndex + 1,
                      considerPrepaymentAmount,
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
                    setPaymentsToReceipts(
                      orderList,
                      paymentList,
                      changePayments,
                      orderListIndex + 1,
                      paymentListIndex,
                      considerPrepaymentAmount,
                      callback
                    );
                  });
                }
              } else {
                // This order is already paid, go to the next order
                setPaymentsToReceipts(
                  orderList,
                  paymentList,
                  changePayments,
                  orderListIndex + 1,
                  paymentListIndex,
                  considerPrepaymentAmount,
                  callback
                );
              }
            }
          } else {
            setPaymentsToReceipts(
              orderList,
              paymentList,
              changePayments,
              orderListIndex,
              paymentListIndex + 1,
              considerPrepaymentAmount,
              callback
            );
          }
        };

        OB.UTIL.HookManager.executeHooks(
          'OBPOS_MultiOrders_PreSetPaymentsToReceipt',
          {
            multiOrderList: multiorders.get('multiOrdersList'),
            payments: multiorders.get('payments')
          },
          function(args) {
            setPaymentsToReceipts(
              args.multiOrderList,
              args.payments,
              multiorders.get('changePayments'),
              OB.DEC.Zero,
              OB.DEC.Zero,
              true,
              function() {
                multiorders.set('change', OB.DEC.Zero);
                multiorders.trigger('closed');
              }
            );
          }
        );
      },
      this
    );

    this.get('multiOrders').on(
      'paymentDone',
      function(openDrawer) {
        this.get('multiOrders').trigger('disableDoneButton');
        var me = this,
          execution,
          paymentstatus = this.get('multiOrders'),
          overpayment = OB.DEC.sub(
            paymentstatus.get('payment'),
            paymentstatus.get('total')
          ),
          orders = paymentstatus.get('multiOrdersList'),
          paymentCancel,
          triggerPaymentAccepted,
          triggerPaymentAcceptedImpl;

        if (OB.UTIL.ProcessController.isProcessActive('paymentDone')) {
          return true;
        }
        execution = OB.UTIL.ProcessController.start('paymentDone');

        paymentCancel = function() {
          OB.UTIL.ProcessController.finish('paymentDone', execution);
          paymentstatus.trigger('paymentCancel');
        };

        triggerPaymentAccepted = function(orders, index) {
          if (index === orders.length) {
            if (
              OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)
            ) {
              OB.MobileApp.model.setSynchronizedCheckpoint(
                triggerPaymentAcceptedImpl
              );
            } else {
              triggerPaymentAcceptedImpl();
            }
          } else {
            OB.UTIL.HookManager.executeHooks(
              'OBPOS_PostPaymentDone',
              {
                receipt: orders.at(index)
              },
              function(args) {
                if (args && args.cancellation && args.cancellation === true) {
                  _.each(
                    me.get('multiOrders').get('multiOrdersList').models,
                    function(currentOrder) {
                      currentOrder.unset('completeTicket');
                    }
                  );
                  paymentCancel();
                  return;
                }
                triggerPaymentAccepted(orders, index + 1);
              }
            );
          }
        };

        triggerPaymentAcceptedImpl = function() {
          OB.UTIL.ProcessController.finish('paymentDone', execution);
          me.get('multiOrders').trigger('paymentAccepted');
        };

        if (overpayment > 0) {
          var symbol = OB.MobileApp.model.get('terminal').symbol,
            symbolAtRight = OB.MobileApp.model.get('terminal')
              .currencySymbolAtTheRight;
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_OverpaymentWarningTitle'),
            OB.I18N.getLabel('OBPOS_OverpaymentWarningBody', [
              OB.I18N.formatCurrencyWithSymbol(
                overpayment,
                symbol,
                symbolAtRight
              )
            ]),
            [
              {
                label: OB.I18N.getLabel('OBMOBC_LblOk'),
                isConfirmButton: true,
                action: function() {
                  me.openDrawer = openDrawer;
                  triggerPaymentAccepted(orders, 0);
                }
              },
              {
                label: OB.I18N.getLabel('OBMOBC_LblCancel'),
                action: function() {
                  paymentCancel();
                }
              }
            ],
            {
              autoDismiss: false,
              hideCloseButton: true
            }
          );
        } else {
          me.openDrawer = openDrawer;
          triggerPaymentAccepted(orders, 0);
        }
      },
      this
    );

    // Listening events that cause a discount recalculation
    receipt.get('lines').on(
      'add change:qty change:price',
      function(line) {
        var terminalOrganization = {
            id: OB.MobileApp.model.get('terminal').organization,
            name: OB.I18N.getLabel('OBPOS_LblThisStore', [
              OB.MobileApp.model.get('terminal').organization$_identifier
            ]),
            country: OB.MobileApp.model.get('terminal').organizationCountryId,
            region: OB.MobileApp.model.get('terminal').organizationRegionId
          },
          terminalWarehouse = {
            id: OB.MobileApp.model.get('warehouses')[0].warehouseid,
            warehousename: OB.MobileApp.model.get('warehouses')[0].warehousename
          };
        // Do not calculate the receipt if the ticket is not editable or is being cloned
        if (
          !receipt.get('isEditable') ||
          receipt.get('skipApplyPromotions') ||
          receipt.get('cloningReceipt')
        ) {
          return;
        }
        if (
          line.get('qty') < 0 &&
          OB.UTIL.isNullOrUndefined(line.get('canceledLine')) &&
          OB.UTIL.isNullOrUndefined(line.get('isVerifiedReturn')) &&
          OB.UTIL.isCrossStoreLine(line)
        ) {
          line.set('organization', terminalOrganization);
          line.set('warehouse', terminalWarehouse);
        }
        // Calculate the receipt
        receipt.calculateReceipt(null, line);
      },
      this
    );

    receipt.get('lines').on('remove', function() {
      if (!receipt.get('isEditable') || receipt.get('deleting')) {
        return;
      }
      // Calculate the receipt
      receipt.calculateReceipt();
    });

    receipt.on(
      'change:bp change:externalBusinessPartner',
      function() {
        if (!receipt.get('isEditable') || receipt.get('lines').length === 0) {
          return;
        }
        receipt.get('lines').forEach(function(l) {
          l.unset('noDiscountCandidates', {
            silent: true
          });
        });
        if (!OB.MobileApp.model.hasPermission('EnableMultiPriceList', true)) {
          // Calculate the receipt only if it's not multipricelist
          receipt.calculateReceipt();
        }
      },
      this
    );

    callback();
  },

  loadModels: function(loadModelsCallback) {
    var me = this;

    this.set('filter', []);
    this.set('brandFilter', []);

    async function searchCurrentBP(callback) {
      var errorCallback = function() {
        OB.error(
          OB.I18N.getLabel('OBPOS_BPInfoErrorTitle') +
            '. Message: ' +
            OB.I18N.getLabel('OBPOS_BPInfoErrorMessage')
        );
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_BPInfoErrorTitle'),
          OB.I18N.getLabel('OBPOS_BPInfoErrorMessage'),
          [
            {
              label: OB.I18N.getLabel('OBPOS_Reload')
            }
          ],
          {
            onShowFunction: function(popup) {
              popup.$.headerCloseButton.hide();
              OB.UTIL.localStorage.removeItem('POSLastTotalRefresh');
              OB.UTIL.localStorage.removeItem('POSLastIncRefresh');
            },
            onHideFunction: function() {
              OB.UTIL.localStorage.removeItem('POSLastTotalRefresh');
              OB.UTIL.localStorage.removeItem('POSLastIncRefresh');
              window.location.reload();
            },
            autoDismiss: false
          }
        );
      };

      function successCallbackBPs(dataBps) {
        if (dataBps) {
          var partnerAddressId = OB.MobileApp.model.get('terminal')
            .partnerAddress;
          dataBps.loadBPLocations(null, null, async function(
            shipping,
            billing,
            locations
          ) {
            var defaultAddress = _.find(locations, function(loc) {
              return loc.id === partnerAddressId;
            });
            if (defaultAddress) {
              if (defaultAddress.get('isShipTo')) {
                shipping = defaultAddress;
              }
              if (defaultAddress.get('isBillTo')) {
                billing = defaultAddress;
              }
            }
            dataBps.setBPLocations(shipping, billing, true);
            dataBps.set('locations', locations);
            OB.MobileApp.model.set('businessPartner', dataBps);
            me.loadUnpaidOrders(function() {
              OB.Taxes.Pos.initCache(function() {
                OB.Discounts.Pos.initCache(function() {
                  me.printReceipt = new OB.OBPOSPointOfSale.Print.Receipt(me);
                  const welcomePrinter = {
                    doPrintWelcome: OB.OBPOSPointOfSale.Print.doPrintWelcome
                  };
                  const hardwareManagerEnpoint = new OB.App.Class.HardwareManagerEndpoint();
                  hardwareManagerEnpoint.setPrinters({
                    printer: me.printReceipt,
                    linePrinter: me.printLine,
                    welcomePrinter
                  });
                  OB.App.SynchronizationBuffer.registerEndpoint(
                    hardwareManagerEnpoint
                  );
                  // Now, get the hardware manager status
                  OB.POS.hwserver.status(function(data) {
                    if (data && data.exception) {
                      OB.UTIL.showError(data.exception.message);
                      callback();
                    } else {
                      // Save hardware manager information
                      if (data && data.version) {
                        // Max database string size: 10
                        var hwmVersion =
                          data.version.length > 10
                            ? data.version.substring(0, 9)
                            : data.version;
                        OB.UTIL.localStorage.setItem(
                          'hardwareManagerVersion',
                          hwmVersion
                        );
                      }
                      if (data && data.revision) {
                        // Max database string size: 15
                        var hwmRevision =
                          data.revision.length > 15
                            ? data.version.substring(0, 14)
                            : data.revision;
                        OB.UTIL.localStorage.setItem(
                          'hardwareManagerRevision',
                          hwmRevision
                        );
                      }
                      if (data && data.javaInfo) {
                        OB.UTIL.localStorage.setItem(
                          'hardwareManagerJavaInfo',
                          data.javaInfo
                        );
                      }
                      // Now that templates has been initialized, print welcome message
                      OB.POS.hwserver.print(
                        me.printReceipt.templatewelcome,
                        {},
                        function(data) {
                          if (data && data.exception) {
                            OB.UTIL.showError(
                              OB.I18N.getLabel(
                                'OBPOS_MsgHardwareServerNotAvailable'
                              )
                            );
                            callback();
                          } else {
                            callback();
                          }
                        },
                        OB.DS.HWServer.DISPLAY
                      );
                    }
                  });
                });
              });
            });
          });
        }
      }

      let checkBPInLocal;
      if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
        checkBPInLocal = function() {
          OB.Dal.get(
            OB.Model.BusinessPartner,
            OB.MobileApp.model.get('businesspartner'),
            successCallbackBPs,
            errorCallback,
            errorCallback,
            null,
            true
          );
        };
        OB.Dal.get(
          OB.Model.BusinessPartner,
          OB.MobileApp.model.get('businesspartner'),
          successCallbackBPs,
          checkBPInLocal,
          errorCallback
        );
      } else {
        checkBPInLocal = async function() {
          try {
            let businessPartner = await OB.App.MasterdataModels.BusinessPartner.withId(
              OB.MobileApp.model.get('businesspartner')
            );
            successCallbackBPs(
              OB.Dal.transform(OB.Model.BusinessPartner, businessPartner)
            );
          } catch (error) {
            errorCallback(error);
          }
        };
        try {
          let businessPartner = await OB.App.MasterdataModels.BusinessPartner.withId(
            OB.MobileApp.model.get('businesspartner')
          );
          if (businessPartner !== undefined) {
            successCallbackBPs(
              OB.Dal.transform(OB.Model.BusinessPartner, businessPartner)
            );
          } else {
            checkBPInLocal();
          }
        } catch (error) {
          errorCallback(error);
        }
      }
    }

    //Because in terminal we've the BP id and we want to have the BP model.
    //In this moment we can ensure data is already loaded in the local database
    searchCurrentBP(loadModelsCallback);
  },

  /**
   * Approval final stage. Where approvalChecked event is triggered, with approved
   * property set to true or false regarding if approval was finally granted. In
   * case of granted approval, the approval is added to the order so it can be saved
   * in backend for audit purposes.
   */
  approvedRequest: function(approved, supervisor, approvalType, callback) {
    var newApprovals,
      approvals,
      approval,
      i,
      callbackFunc,
      hasPermission = false,
      saveApproval,
      executeHook,
      request,
      me = this;

    saveApproval = function(order, silent) {
      newApprovals = [];

      approvals = order.get('approvals') || [];
      if (!Array.isArray(approvalType)) {
        approvalType = [approvalType];
      }

      _.each(approvals, function(appr) {
        var results;
        results = _.find(approvalType, function(apprType) {
          return apprType === appr.approvalType;
        });

        if (_.isUndefined(results)) {
          newApprovals.push(appr);
        }
      });

      for (i = 0; i < approvalType.length; i++) {
        approval = {
          approvalType: approvalType[i],
          userContact: supervisor.get('id'),
          created: new Date().getTime()
        };
        newApprovals.push(approval);
      }
      order.set('approvals', newApprovals, {
        silent: silent
      });
    };

    callbackFunc = function() {
      if (enyo.isFunction(callback)) {
        callback(approved, supervisor, approvalType);
      }
    };

    executeHook = function(approvalType, finalCallback) {
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PostRequestApproval_' + approvalType,
        {
          approved: approved,
          supervisor: supervisor,
          approvalType: approvalType,
          callbackApproval: callback,
          context: me
        },
        function(args) {
          finalCallback(args);
        }
      );
    };

    request = function(args) {
      if (_.isArray(approvalType)) {
        hasPermission = _.every(approvalType, function(a) {
          return OB.MobileApp.model.hasPermission(a, true);
        });
      } else if (!OB.UTIL.isNullOrUndefined(approvalType)) {
        hasPermission = OB.MobileApp.model.hasPermission(approvalType, true);
      } else {
        callbackFunc();
        return;
      }
      if (hasPermission) {
        callbackFunc();
        return;
      }

      if (approved) {
        if (me.get('leftColumnViewManager').isOrder()) {
          saveApproval(me.get('order'));
        } else {
          me.get('multiOrders')
            .get('multiOrdersList')
            .forEach(function(order) {
              saveApproval(order, true);
            });
        }
      }

      me.trigger('approvalChecked', {
        approved: approved
      });
      callbackFunc();
    };

    if (_.isArray(approvalType)) {
      var afterExecuteHook = _.after(approvalType.length, function(args) {
        request(args);
      });
      _.each(approvalType, function(type) {
        executeHook(type.approval, function(args) {
          afterExecuteHook(args);
        });
      });
    } else {
      executeHook(approvalType, function(args) {
        request(args);
      });
    }
  }
});
