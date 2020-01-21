/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
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
  models: [
    {
      generatedModel: true,
      modelName: 'TaxRate'
    },
    {
      generatedModel: true,
      modelName: 'TaxZone'
    },
    OB.Model.Product,
    OB.Model.ProductCategoryTree,
    OB.Model.PriceList,
    OB.Model.ProductPrice,
    OB.Model.ServiceProduct,
    OB.Model.ServiceProductCategory,
    OB.Model.ServicePriceRule,
    OB.Model.ServicePriceRuleRange,
    OB.Model.ServicePriceRuleRangePrices,
    OB.Model.ServicePriceRuleVersion,
    OB.Model.BusinessPartner,
    OB.Model.BPLocation,
    OB.Model.Order,
    OB.Model.DocumentSequence,
    OB.Model.ChangedBusinessPartners,
    OB.Model.ChangedBPlocation,
    OB.Model.ProductBOM,
    OB.Model.TaxCategoryBOM,
    OB.Model.CancelLayaway,
    OB.Model.ProductServiceLinked, //
    OB.Model.CurrencyPanel,
    OB.Model.ProductCharacteristicValue,
    OB.Model.CharacteristicValue,
    OB.Model.Characteristic,
    OB.Model.CashUp,
    OB.Model.OfflinePrinter,
    OB.Model.PaymentMethodCashUp,
    OB.Model.TaxCashUp
  ],

  loadUnpaidOrders: function(loadUnpaidOrdersCallback) {
    // Shows a modal window with the orders pending to be paid
    var orderlist = this.get('orderList'),
      model = this,
      reCalculateReceipt = false,
      criteria = {
        hasbeenpaid: 'N'
      };
    OB.Dal.find(
      OB.Model.Order,
      criteria,
      function(ordersNotPaid) {
        //OB.Dal.find success
        var currentOrder = {},
          loadOrderStr;

        // Getting Max Document No, Quotation No from Unpaid orders
        var maxDocumentNo = 0,
          maxQuotationNo = 0,
          maxReturnNo = 0;
        _.each(ordersNotPaid.models, function(order) {
          if (order) {
            if (order.get('documentnoSuffix') > maxDocumentNo) {
              maxDocumentNo = order.get('documentnoSuffix');
            }
            if (order.get('quotationnoSuffix') > maxQuotationNo) {
              maxQuotationNo = order.get('quotationnoSuffix');
            }
            if (order.get('returnnoSuffix') > maxReturnNo) {
              maxReturnNo = order.get('returnnoSuffix');
            }
          }
        });

        // Setting the Max Document No, Quotation No to their respective Threshold
        if (
          maxDocumentNo > 0 &&
          OB.MobileApp.model.documentnoThreshold < maxDocumentNo
        ) {
          OB.MobileApp.model.documentnoThreshold = maxDocumentNo;
        }
        if (
          maxQuotationNo > 0 &&
          OB.MobileApp.model.quotationnoThreshold < maxQuotationNo
        ) {
          OB.MobileApp.model.quotationnoThreshold = maxQuotationNo;
        }
        if (
          maxReturnNo > 0 &&
          OB.MobileApp.model.returnnoThreshold < maxReturnNo
        ) {
          OB.MobileApp.model.returnnoThreshold = maxReturnNo;
        }

        // Removing Orders which are created in other users session
        var outOfSessionOrder = _.filter(ordersNotPaid.models, function(order) {
          if (
            order &&
            order.get('session') !== OB.MobileApp.model.get('session')
          ) {
            return true;
          }
        });
        _.each(outOfSessionOrder, function(orderToRemove) {
          ordersNotPaid.remove(orderToRemove);
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
                if (!args.ordersNotPaid || args.ordersNotPaid.length === 0) {
                  // If there are no pending orders,
                  //  add an initial empty order
                  orderlist.addFirstOrder();
                } else {
                  // The order object is stored in the json property of the row fetched from the database
                  orderlist.reset(args.ordersNotPaid.models);
                  // At this point it is sure that there exists at least one order
                  // Function to continue of there is some error
                  currentOrder = args.ordersNotPaid.models[0];
                  //removing Orders lines without mandatory fields filled
                  OB.UTIL.HookManager.executeHooks(
                    'OBPOS_CheckReceiptMandatoryFields',
                    {
                      orders: orderlist.models
                    },
                    function(args) {
                      reCalculateReceipt = args.reCalculateReceipt;
                      orderlist.load(currentOrder);
                      if (reCalculateReceipt) {
                        OB.MobileApp.model.receipt.calculateGrossAndSave();
                      }
                      loadOrderStr =
                        OB.I18N.getLabel('OBPOS_Order') +
                        currentOrder.get('documentNo') +
                        OB.I18N.getLabel('OBPOS_Loaded');
                      OB.UTIL.showAlert.display(
                        loadOrderStr,
                        OB.I18N.getLabel('OBPOS_Info')
                      );
                    }
                  );
                }
              },
              model
            );
            loadUnpaidOrdersCallback();
          }
        );
      },
      function() {
        //OB.Dal.find error
        OB.MobileApp.model.on(
          'window:ready',
          function() {
            OB.MobileApp.model.off('window:ready', null, model);
            // If there is an error fetching the pending orders,
            // add an initial empty order
            orderlist.addFirstOrder();
          },
          model
        );
        loadUnpaidOrdersCallback();
      }
    );
  },

  loadCheckedMultiorders: function() {
    // Shows a modal window with the orders pending to be paid
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
          var payments = JSON.parse(
            OB.UTIL.localStorage.getItem('multiOrdersPayment')
          );
          _.each(payments, function(payment) {
            multiOrders.addPayment(new OB.Model.PaymentLine(payment));
          });
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
          if (order.get('id')) {
            this.get('orderList').deleteCurrentFromDatabase(order);
          }
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
    OB.DATA.OrderDiscount(receipt);
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
        OB.UTIL.TicketCloseUtils.paymentAccepted(receipt, orderList, null);
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
          paymentstatus = this.get('multiOrders'),
          overpayment = OB.DEC.sub(
            paymentstatus.get('payment'),
            paymentstatus.get('total')
          ),
          orders = paymentstatus.get('multiOrdersList'),
          triggerPaymentAccepted,
          triggerPaymentAcceptedImpl;

        if (paymentstatus.get('paymentDone')) {
          return true;
        }
        paymentstatus.set('paymentDone', true);

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
                  me.get('multiOrders').trigger('paymentCancel');
                  return;
                }
                triggerPaymentAccepted(orders, index + 1);
              }
            );
          }
        };

        triggerPaymentAcceptedImpl = function() {
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
                  paymentstatus.trigger('paymentCancel');
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
            ])
          },
          terminalWarehouse = {
            id: OB.MobileApp.model.get('warehouses')[0].warehouseid,
            warehousename: OB.MobileApp.model.get('warehouses')[0].warehousename
          };
        // Do not calculate the receipt if the ticket is not editable or is being cloned
        if (!receipt.get('isEditable') || receipt.get('cloningReceipt')) {
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
      'change:bp',
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

    receipt.on(
      'voidLayaway',
      function() {
        var execution = OB.UTIL.ProcessController.start('voidLayaway'),
          process = new OB.DS.Process(
            'org.openbravo.retail.posterminal.ProcessVoidLayaway'
          ),
          auxReceipt = new OB.Model.Order(),
          receiptForPrinting = new OB.Model.Order();

        function finishVoidLayaway() {
          OB.UTIL.ProcessController.finish('voidLayaway', execution);
        }

        function revertCashupReport(callback) {
          OB.UTIL.clone(receipt, auxReceipt);
          auxReceipt.set('isLayaway', false);
          auxReceipt.set('orderType', 2);
          OB.UTIL.cashUpReport(auxReceipt, function() {
            if (callback instanceof Function) {
              callback();
            }
          });
        }

        function updateCashup() {
          OB.UTIL.TicketCloseUtils.checkOrdersUpdated(
            auxReceipt,
            function() {
              auxReceipt.set('timezoneOffset', new Date().getTimezoneOffset());
              auxReceipt.set('gross', OB.DEC.Zero);
              auxReceipt.set('isVoided', true);
              auxReceipt.set('orderType', 3);
              auxReceipt.prepareToSend(function() {
                OB.Dal.transaction(function(tx) {
                  OB.UTIL.cashUpReport(
                    auxReceipt,
                    function(cashUp) {
                      auxReceipt.set(
                        'cashUpReportInformation',
                        JSON.parse(cashUp.models[0].get('objToSend'))
                      );
                      OB.UTIL.HookManager.executeHooks(
                        'OBPOS_PreSyncReceipt',
                        {
                          receipt: receipt,
                          model: me,
                          tx: tx
                        },
                        function(args) {
                          auxReceipt.set(
                            'json',
                            JSON.stringify(receipt.serializeToSaveJSON())
                          );
                          process.exec(
                            {
                              messageId: OB.UTIL.get_UUID(),
                              data: [
                                {
                                  id: auxReceipt.get('id'),
                                  posTerminal: OB.MobileApp.model.get(
                                    'terminal'
                                  ).id,
                                  order: auxReceipt
                                }
                              ]
                            },
                            function(data) {
                              if (data && data.exception) {
                                revertCashupReport(function() {
                                  if (data.exception.message) {
                                    OB.UTIL.showConfirmation.display(
                                      OB.I18N.getLabel('OBMOBC_Error'),
                                      data.exception.message
                                    );
                                  } else {
                                    OB.UTIL.showError(
                                      OB.I18N.getLabel(
                                        'OBPOS_MsgErrorVoidLayaway'
                                      )
                                    );
                                  }
                                  finishVoidLayaway();
                                });
                              } else {
                                auxReceipt.trigger(
                                  OB.MobileApp.model.get('terminal')
                                    .defaultwebpostab
                                );
                                OB.Dal.remove(receipt, null, function(tx, err) {
                                  OB.UTIL.showError(err);
                                });
                                OB.UTIL.clone(auxReceipt, receiptForPrinting);
                                auxReceipt.trigger('print', receiptForPrinting);
                                orderList.deleteCurrent();
                                receipt.trigger('change:gross', receipt);
                                OB.UTIL.showSuccess(
                                  OB.I18N.getLabel(
                                    'OBPOS_MsgSuccessVoidLayaway'
                                  )
                                );
                                finishVoidLayaway();
                              }
                            },
                            function() {
                              revertCashupReport(function() {
                                OB.UTIL.showError(
                                  OB.I18N.getLabel(
                                    'OBPOS_OfflineWindowRequiresOnline'
                                  )
                                );
                                finishVoidLayaway();
                              });
                            }
                          );
                        },
                        tx
                      );
                    },
                    tx
                  );
                });
              });
            },
            finishVoidLayaway
          );
        }

        OB.UTIL.clone(receipt, auxReceipt);
        auxReceipt.set('voidLayaway', true);
        if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
          OB.UTIL.rebuildCashupFromServer(function() {
            auxReceipt.set(
              'obposAppCashup',
              OB.MobileApp.model.get('terminal').cashUpId
            );
            updateCashup();
          });
        } else {
          updateCashup();
        }
      },
      this
    );

    receipt.on(
      'cancelLayaway',
      function() {
        var finishCancelLayaway = function() {
          var processCancelLayaway,
            execution = OB.UTIL.ProcessController.start('cancelLayaway');

          processCancelLayaway = function() {
            var cloneOrderForNew = new OB.Model.Order(),
              cloneOrderForPrinting = new OB.Model.Order(),
              creationDate;

            OB.UTIL.TicketCloseUtils.checkOrdersUpdated(
              receipt.get('canceledorder'),
              function() {
                receipt.prepareToSend(function() {
                  creationDate = new Date();
                  receipt.set(
                    'creationDate',
                    OB.I18N.normalizeDate(creationDate)
                  );
                  receipt.set(
                    'obposCreatedabsolute',
                    OB.I18N.formatDateISO(creationDate)
                  ); // Absolute date in ISO format
                  receipt.set(
                    'obposAppCashup',
                    OB.MobileApp.model.get('terminal').cashUpId
                  );
                  OB.UTIL.HookManager.executeHooks(
                    'OBPOS_FinishCancelLayaway',
                    {
                      context: me,
                      model: me,
                      receipt: receipt
                    },
                    function(args) {
                      OB.UTIL.HookManager.executeHooks(
                        'OBPOS_PreSyncReceipt',
                        {
                          receipt: receipt,
                          model: me
                        },
                        function(args) {
                          OB.Dal.transaction(
                            function(tx) {
                              if (receipt.isNegative()) {
                                receipt
                                  .get('payments')
                                  .forEach(function(payment) {
                                    payment.set(
                                      'amount',
                                      OB.DEC.mul(payment.get('amount'), -1)
                                    );
                                    payment.set(
                                      'origAmount',
                                      OB.DEC.mul(payment.get('origAmount'), -1)
                                    );
                                    payment.set(
                                      'paid',
                                      OB.DEC.mul(payment.get('paid'), -1)
                                    );
                                  });
                              }
                              OB.UTIL.cashUpReport(
                                receipt,
                                function(cashUp) {
                                  var cancelLayawayModel = new OB.Model.CancelLayaway(),
                                    cancelLayawayObj;

                                  receipt.set(
                                    'cashUpReportInformation',
                                    JSON.parse(
                                      cashUp.models[0].get('objToSend')
                                    )
                                  );
                                  receipt.set('created', new Date().getTime());
                                  receipt.set(
                                    'json',
                                    JSON.stringify(
                                      receipt.serializeToSaveJSON()
                                    )
                                  );

                                  OB.UTIL.clone(receipt, cloneOrderForNew);
                                  OB.UTIL.clone(receipt, cloneOrderForPrinting);

                                  cancelLayawayObj = receipt.serializeToJSON();
                                  cancelLayawayModel.set(
                                    'json',
                                    JSON.stringify(cancelLayawayObj)
                                  );
                                  OB.Dal.getInTransaction(
                                    tx,
                                    OB.Model.Order,
                                    receipt.id,
                                    function(model) {
                                      OB.Dal.removeInTransaction(tx, model);
                                    }
                                  );
                                  OB.Dal.saveInTransaction(
                                    tx,
                                    cancelLayawayModel
                                  );
                                },
                                tx
                              );
                            },
                            function() {
                              //transaction error callback
                              OB.error(
                                '[cancellayaway] The transaction failed to be commited. LayawayId: ' +
                                  receipt.get('id')
                              );
                              OB.UTIL.ProcessController.finish(
                                'cancelLayaway',
                                execution
                              );
                            },
                            function() {
                              //transaction success callback
                              OB.info(
                                '[cancellayaway] Transaction success. LayawayId: ' +
                                  receipt.get('id')
                              );

                              function cancelAndNew() {
                                if (
                                  OB.MobileApp.model.hasPermission(
                                    'OBPOS_cancelLayawayAndNew',
                                    true
                                  )
                                ) {
                                  OB.UTIL.showConfirmation.display(
                                    OB.I18N.getLabel(
                                      'OBPOS_cancelLayawayAndNewHeader'
                                    ),
                                    OB.I18N.getLabel(
                                      'OBPOS_cancelLayawayAndNewBody'
                                    ),
                                    [
                                      {
                                        label: OB.I18N.getLabel('OBPOS_LblOk'),
                                        action: function() {
                                          orderList.addNewOrder();
                                          var linesMap = {},
                                            order = orderList.modelorder,
                                            addRelatedLines,
                                            addLineToTicket;

                                          var finalCallback = function() {
                                            order.unset(
                                              'preventServicesUpdate'
                                            );
                                            order
                                              .get('lines')
                                              .trigger('updateRelations');
                                          };

                                          addRelatedLines = function(index) {
                                            if (
                                              index ===
                                              order.get('lines').length
                                            ) {
                                              finalCallback();
                                              return;
                                            }
                                            var line = order
                                                .get('lines')
                                                .at(index),
                                              oldLine = linesMap[line.id];
                                            if (oldLine.get('relatedLines')) {
                                              line.set('relatedLines', []);
                                              _.each(
                                                oldLine.get('relatedLines'),
                                                function(relatedLine) {
                                                  var newRelatedLine = _.clone(
                                                    relatedLine
                                                  );
                                                  // If the service is not a deferred service, the related line, documentNo
                                                  // and orderId must be updated. If it is, is must be marked as deferred
                                                  if (
                                                    !newRelatedLine.otherTicket
                                                  ) {
                                                    var i,
                                                      keys = _.keys(linesMap);
                                                    newRelatedLine.orderDocumentNo = order.get(
                                                      'documentNo'
                                                    );
                                                    newRelatedLine.orderId =
                                                      order.id;
                                                    for (
                                                      i = 0;
                                                      i < keys.length;
                                                      i++
                                                    ) {
                                                      var key = keys[i];
                                                      if (
                                                        newRelatedLine.orderlineId ===
                                                        linesMap[key].id
                                                      ) {
                                                        newRelatedLine.orderlineId = key;
                                                        break;
                                                      }
                                                    }
                                                  }
                                                  line
                                                    .get('relatedLines')
                                                    .push(newRelatedLine);
                                                  if (
                                                    !order.get('hasServices')
                                                  ) {
                                                    order.set(
                                                      'hasServices',
                                                      true
                                                    );
                                                  }
                                                }
                                              );
                                            }
                                            // Hook to allow any needed relation from an external module
                                            OB.UTIL.HookManager.executeHooks(
                                              'OBPOS_CancelAndNewAddLineRelation',
                                              {
                                                order: order,
                                                cloneOrderForNew: cloneOrderForNew,
                                                line: line,
                                                oldLine: oldLine,
                                                linesMap: linesMap
                                              },
                                              function(args) {
                                                addRelatedLines(index + 1);
                                              }
                                            );
                                          };

                                          addLineToTicket = function(idx) {
                                            if (
                                              idx ===
                                              cloneOrderForNew.get('lines')
                                                .length
                                            ) {
                                              addRelatedLines(0);
                                            } else {
                                              var line = cloneOrderForNew
                                                .get('lines')
                                                .at(idx);
                                              order.addProduct(
                                                line.get('product'),
                                                -line.get('qty'),
                                                {
                                                  isSilentAddProduct: true
                                                },
                                                undefined,
                                                function(success, orderline) {
                                                  if (success) {
                                                    linesMap[
                                                      order
                                                        .get('lines')
                                                        .at(
                                                          order.get('lines')
                                                            .length - 1
                                                        ).id
                                                    ] = line;
                                                  }
                                                  addLineToTicket(idx + 1);
                                                }
                                              );
                                            }
                                          };

                                          if (
                                            cloneOrderForNew.get('isLayaway')
                                          ) {
                                            OB.MobileApp.view.$.containerWindow
                                              .getRoot()
                                              .showDivText(null, {
                                                permission: null,
                                                orderType: 2
                                              });
                                          }
                                          order.set(
                                            'bp',
                                            cloneOrderForNew.get('bp')
                                          );
                                          addLineToTicket(0);
                                        }
                                      },
                                      {
                                        label: OB.I18N.getLabel('OBPOS_Cancel')
                                      }
                                    ]
                                  );
                                }
                              }

                              function syncProcessCallback() {
                                OB.UTIL.showSuccess(
                                  OB.I18N.getLabel(
                                    'OBPOS_MsgSuccessCancelLayaway',
                                    [
                                      receipt
                                        .get('canceledorder')
                                        .get('documentNo')
                                    ]
                                  )
                                );
                                orderList.deleteCurrent();
                                OB.UTIL.calculateCurrentCash();
                                OB.UTIL.ProcessController.finish(
                                  'cancelLayaway',
                                  execution
                                );
                                receipt.trigger(
                                  'print',
                                  cloneOrderForPrinting,
                                  {
                                    callback: cancelAndNew,
                                    forceCallback: true
                                  }
                                );
                              }

                              OB.MobileApp.model.runSyncProcess(
                                function() {
                                  syncProcessCallback();
                                },
                                function() {
                                  if (
                                    OB.MobileApp.model.hasPermission(
                                      'OBMOBC_SynchronizedMode',
                                      true
                                    )
                                  ) {
                                    OB.Dal.get(
                                      OB.Model.Order,
                                      receipt.get('id'),
                                      function(loadedReceipt) {
                                        receipt.clearWith(loadedReceipt);
                                        //We need to restore the payment tab, as that's what the user should see if synchronization fails
                                        OB.MobileApp.view.waterfall(
                                          'onTabChange',
                                          {
                                            tabPanel: 'payment',
                                            keyboard: 'toolbarpayment',
                                            edit: false
                                          }
                                        );
                                        receipt.set('hasbeenpaid', 'N');
                                        receipt.unset('completeTicket');
                                        receipt.trigger('updatePending');
                                        OB.Dal.save(
                                          receipt,
                                          function() {
                                            OB.UTIL.calculateCurrentCash(
                                              function() {
                                                OB.UTIL.ProcessController.finish(
                                                  'cancelLayaway',
                                                  execution
                                                );
                                              }
                                            );
                                          },
                                          null,
                                          false
                                        );
                                      }
                                    );
                                  } else {
                                    syncProcessCallback();
                                  }
                                }
                              );
                            }
                          );
                        }
                      );
                    }
                  );
                });
              },
              function() {
                OB.UTIL.ProcessController.finish('cancelLayaway', execution);
              }
            );
          };

          receipt.canCancelOrder(
            receipt.get('canceledorder'),
            null,
            function(data) {
              if (data && data.exception) {
                if (data.exception.message) {
                  OB.UTIL.showConfirmation.display(
                    OB.I18N.getLabel('OBMOBC_Error'),
                    data.exception.message
                  );
                  OB.UTIL.ProcessController.finish('cancelLayaway', execution);
                  return;
                }
                OB.UTIL.showConfirmation.display(
                  OB.I18N.getLabel('OBMOBC_Error'),
                  OB.I18N.getLabel('OBMOBC_OfflineWindowRequiresOnline')
                );
                OB.UTIL.ProcessController.finish('cancelLayaway', execution);
                return;
              } else if (data && data.orderCancelled) {
                OB.UTIL.showConfirmation.display(
                  OB.I18N.getLabel('OBMOBC_Error'),
                  OB.I18N.getLabel('OBPOS_LayawayCancelledError')
                );
                OB.UTIL.ProcessController.finish('cancelLayaway', execution);
                return;
              } else {
                if (
                  OB.MobileApp.model.hasPermission(
                    'OBMOBC_SynchronizedMode',
                    true
                  )
                ) {
                  OB.UTIL.rebuildCashupFromServer(function() {
                    OB.MobileApp.model.setSynchronizedCheckpoint(function() {
                      processCancelLayaway();
                    });
                  });
                } else {
                  processCancelLayaway();
                }
              }
            },
            function() {
              OB.UTIL.showConfirmation.display(
                OB.I18N.getLabel('OBMOBC_Error'),
                OB.I18N.getLabel('OBMOBC_OfflineWindowRequiresOnline')
              );
              OB.UTIL.ProcessController.finish('cancelLayaway', execution);
              OB.error(arguments);
            }
          );
        };

        if (receipt.overpaymentExists()) {
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
                  finishCancelLayaway();
                }
              },
              {
                label: OB.I18N.getLabel('OBMOBC_LblCancel')
              }
            ]
          );
        } else {
          finishCancelLayaway();
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

    function searchCurrentBP(callback) {
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
          dataBps.loadBPLocations(null, null, function(
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
            OB.Dal.save(
              dataBps,
              function() {},
              function() {
                OB.error(arguments);
              }
            );
            me.loadUnpaidOrders(function() {
              OB.Discounts.Pos.initCache(function() {
                me.printReceipt = new OB.OBPOSPointOfSale.Print.Receipt(me);
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
        }
      }
      var checkBPInLocal = function() {
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
