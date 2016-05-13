/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo, _, BigDecimal, localStorage */

OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
OB.OBPOSPointOfSale.Model = OB.OBPOSPointOfSale.Model || {};
OB.OBPOSPointOfSale.UI = OB.OBPOSPointOfSale.UI || {};

//Window model
OB.OBPOSPointOfSale.Model.PointOfSale = OB.Model.TerminalWindowModel.extend({
  models: [{
    generatedModel: true,
    modelName: 'TaxRate'
  }, {
    generatedModel: true,
    modelName: 'TaxZone'
  },
  OB.Model.Product, OB.Model.ProductCategory, OB.Model.ProductCategoryTree, OB.Model.PriceList, OB.Model.ProductPrice, OB.Model.OfferPriceList, OB.Model.ServiceProduct, OB.Model.ServiceProductCategory, OB.Model.ServicePriceRule, OB.Model.ServicePriceRuleRange, OB.Model.ServicePriceRuleRangePrices, OB.Model.ServicePriceRuleVersion, OB.Model.BusinessPartner, OB.Model.BPCategory, OB.Model.BPLocation, OB.Model.Order, OB.Model.DocumentSequence, OB.Model.ChangedBusinessPartners, OB.Model.ChangedBPlocation, OB.Model.ProductBOM, OB.Model.TaxCategoryBOM, OB.Model.CancelLayaway,
  {
    generatedModel: true,
    modelName: 'Discount'
  }, {
    generatedModel: true,
    modelName: 'DiscountFilterBusinessPartner'
  }, {
    generatedModel: true,
    modelName: 'DiscountFilterBusinessPartnerGroup'
  }, {
    generatedModel: true,
    modelName: 'DiscountFilterProduct'
  }, {
    generatedModel: true,
    modelName: 'DiscountFilterProductCategory'
  }, {
    generatedModel: true,
    modelName: 'DiscountFilterRole'
  }, {
    generatedModel: true,
    modelName: 'DiscountFilterCharacteristic'
  },
  OB.Model.CurrencyPanel, OB.Model.SalesRepresentative, OB.Model.Brand, OB.Model.ProductCharacteristicValue, OB.Model.CharacteristicValue, OB.Model.Characteristic, OB.Model.ReturnReason, OB.Model.CashUp, OB.Model.OfflinePrinter, OB.Model.PaymentMethodCashUp, OB.Model.TaxCashUp],

  loadUnpaidOrders: function (loadUnpaidOrdersCallback) {
    // Shows a modal window with the orders pending to be paid
    var orderlist = this.get('orderList'),
        model = this,
        criteria = {
        'hasbeenpaid': 'N'
        };
    OB.Dal.find(OB.Model.Order, criteria, function (ordersNotPaid) { //OB.Dal.find success
      var currentOrder = {},
          loadOrderStr;

      // Getting Max Document No, Quotation No from Unpaid orders
      var maxDocumentNo = 0,
          maxQuotationNo = 0,
          maxReturnNo = 0;
      _.each(ordersNotPaid.models, function (order) {
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
      if (maxDocumentNo > 0 && OB.MobileApp.model.documentnoThreshold < maxDocumentNo) {
        OB.MobileApp.model.documentnoThreshold = maxDocumentNo;
      }
      if (maxQuotationNo > 0 && OB.MobileApp.model.quotationnoThreshold < maxQuotationNo) {
        OB.MobileApp.model.quotationnoThreshold = maxQuotationNo;
      }
      if (maxReturnNo > 0 && OB.MobileApp.model.returnnoThreshold < maxReturnNo) {
        OB.MobileApp.model.returnnoThreshold = maxReturnNo;
      }

      // Removing Orders which are created in other users session 
      var outOfSessionOrder = _.filter(ordersNotPaid.models, function (order) {
        if (order && order.get('session') !== OB.MobileApp.model.get('session')) {
          return true;
        }
      });
      _.each(outOfSessionOrder, function (orderToRemove) {
        ordersNotPaid.remove(orderToRemove);
      });

      OB.UTIL.HookManager.executeHooks('OBPOS_PreLoadUnpaidOrdersHook', {
        ordersNotPaid: ordersNotPaid,
        model: model
      }, function (args) {
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
          orderlist.load(currentOrder);
          loadOrderStr = OB.I18N.getLabel('OBPOS_Order') + currentOrder.get('documentNo') + OB.I18N.getLabel('OBPOS_Loaded');
          OB.UTIL.showAlert.display(loadOrderStr, OB.I18N.getLabel('OBPOS_Info'));
        }
        loadUnpaidOrdersCallback();
      });
    }, function () { //OB.Dal.find error
      // If there is an error fetching the pending orders,
      // add an initial empty order
      orderlist.addFirstOrder();
    });
  },

  loadCheckedMultiorders: function () {
    // Shows a modal window with the orders pending to be paid
    var checkedMultiOrders, multiOrderList = this.get('multiOrders').get('multiOrdersList'),
        criteria = {
        'hasbeenpaid': 'N',
        'session': OB.MobileApp.model.get('session')
        };
    OB.Dal.find(OB.Model.Order, criteria, function (possibleMultiOrder) { //OB.Dal.find success
      if (possibleMultiOrder && possibleMultiOrder.length > 0) {
        checkedMultiOrders = _.compact(possibleMultiOrder.map(function (e) {
          if (e.get('checked')) {
            return e;
          }
        }));
        //The order object is stored in the json property of the row fetched from the database
        multiOrderList.reset(checkedMultiOrders);
      }
    }, function () {
      // If there is an error fetching the checked orders of multiorders,
      //OB.Dal.find error
    });
  },
  isValidMultiOrderState: function () {
    if (this.get('leftColumnViewManager') && this.get('multiOrders')) {
      return this.get('leftColumnViewManager').isMultiOrder() && this.get('multiOrders').hasDataInList();
    }
    return false;
  },
  getPending: function () {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getPending();
    } else {
      return this.get('multiOrders').getPending();
    }
  },
  getChange: function () {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getChange();
    } else {
      return this.get('multiOrders').getChange();
    }
  },
  getTotal: function () {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getTotal();
    } else {
      return this.get('multiOrders').getTotal();
    }
  },
  getPayment: function () {
    if (this.get('leftColumnViewManager').isOrder()) {
      return this.get('order').getPayment();
    } else {
      return this.get('multiOrders').getPayment();
    }
  },
  addPayment: function (payment) {
    var modelToIncludePayment;

    if (this.get('leftColumnViewManager').isOrder()) {
      modelToIncludePayment = this.get('order');
    } else {
      modelToIncludePayment = this.get('multiOrders');
    }

    modelToIncludePayment.addPayment(payment);
  },
  deleteMultiOrderList: function () {
    var i;
    for (i = 0; this.get('multiOrders').get('multiOrdersList').length > i; i++) {
      if (!this.get('multiOrders').get('multiOrdersList').at(i).get('isLayaway')) { //if it is not true, means that iti is a new order (not a loaded layaway)
        this.get('multiOrders').get('multiOrdersList').at(i).unset('amountToLayaway');
        this.get('multiOrders').get('multiOrdersList').at(i).set('orderType', 0);
        continue;
      }
      this.get('orderList').current = this.get('multiOrders').get('multiOrdersList').at(i);
      this.get('orderList').deleteCurrent();
      if (!_.isNull(this.get('multiOrders').get('multiOrdersList').at(i).id)) {
        this.get('orderList').deleteCurrentFromDatabase(this.get('multiOrders').get('multiOrdersList').at(i));
      }
    }
  },
  init: function () {
    OB.error("This init method should never be called for this model. Call initModels and loadModels instead");
    this.initModels(function () {});
    this.loadModels(function () {});
  },
  initModels: function (callback) {
    var me = this;

    // create and expose the receipt
    var receipt = new OB.Model.Order();
    // fire events if the receipt model is the target of the OB.UTIL.clone method
    receipt.triggerEventsIfTargetOfSourceWhenCloning = function () {
      return true;
    };
    OB.MobileApp.model.receipt = receipt;

    // create the multiOrders
    var multiOrders = new OB.Model.MultiOrders();
    // create the orderList and expose it
    var orderList = new OB.Collection.OrderList(receipt);
    OB.MobileApp.model.orderList = orderList;
    var auxReceiptList = [];

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

    this.printReceipt = new OB.OBPOSPointOfSale.Print.Receipt(this);
    this.printLine = new OB.OBPOSPointOfSale.Print.ReceiptLine(receipt);

    var ViewManager = Backbone.Model.extend({
      defaults: {
        currentWindow: {
          name: 'mainSubWindow',
          params: []
        }
      },
      initialize: function () {}
    });

    var LeftColumnViewManager = Backbone.Model.extend({
      defaults: {
        currentView: {}
      },
      initialize: function () {
        this.on('change:currentView', function (changedModel) {
          OB.UTIL.localStorage.setItem('leftColumnCurrentView', JSON.stringify(changedModel.get('currentView')));
          this.trigger(changedModel.get('currentView').name);
        }, this);
      },
      setOrderMode: function (parameters) {
        this.set('currentView', {
          name: 'order',
          params: parameters
        });
        OB.UTIL.localStorage.setItem('leftColumnCurrentView', JSON.stringify(this.get('currentView')));
      },
      isOrder: function () {
        if (this.get('currentView').name === 'order') {
          return true;
        }
        return false;
      },
      setMultiOrderMode: function (parameters) {
        this.set('currentView', {
          name: 'multiorder',
          params: parameters
        });
      },
      isMultiOrder: function () {
        if (this.get('currentView').name === 'multiorder') {
          return true;
        }
        return false;
      }
    });

    this.set('leftColumnViewManager', new LeftColumnViewManager());
    this.set('subWindowManager', new ViewManager());

    OB.MobileApp.model.runSyncProcess(function () {
      OB.RR.RequestRouter.sendAllMessages();
      me.loadCheckedMultiorders();
    }, function () {
      OB.RR.RequestRouter.sendAllMessages();
      me.loadCheckedMultiorders();
    });

    receipt.on('paymentAccepted', function () {
      var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes("receipt.paymentAccepted");
      receipt.setIsCalculateReceiptLockState(true);
      receipt.setIsCalculateGrossLockState(true);
      receipt.prepareToSend(function () {
        //Create the negative payment for change
        var oldChange = receipt.get('change'),
            clonedCollection = new Backbone.Collection(),
            paymentKind, i, totalPrePayment = OB.DEC.Zero,
            totalNotPrePayment = OB.DEC.Zero;
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
        if (!_.isUndefined(receipt.get('selectedPayment')) && receipt.getChange() > 0) {
          var payment = OB.MobileApp.model.paymentnames[receipt.get('selectedPayment')];
          if (!payment.paymentMethod.iscash) {
            payment = OB.MobileApp.model.paymentnames[OB.MobileApp.model.get('paymentcash')];
          }
          if (receipt.get('payment') >= receipt.get('gross') || (!_.isUndefined(receipt.get('paidInNegativeStatusAmt')) && OB.DEC.compare(OB.DEC.sub(receipt.get('gross'), OB.DEC.sub(totalPrePayment, totalNotPrePayment))) >= 0)) {
            receipt.addPayment(new OB.Model.PaymentLine({
              'kind': payment.payment.searchKey,
              'name': payment.payment.commercialName,
              'amount': OB.DEC.sub(0, OB.DEC.mul(receipt.getChange(), payment.mulrate)),
              'rate': payment.rate,
              'mulrate': payment.mulrate,
              'isocode': payment.isocode,
              'allowOpenDrawer': payment.paymentMethod.allowopendrawer,
              'isCash': payment.paymentMethod.iscash,
              'openDrawer': payment.paymentMethod.openDrawer,
              'printtwice': payment.paymentMethod.printtwice
            }));
          }
          receipt.set('change', oldChange);
          for (i = 0; i < receipt.get('payments').length; i++) {
            paymentKind = OB.MobileApp.model.paymentnames[receipt.get('payments').models[i].get('kind')];
            if (paymentKind && paymentKind.paymentMethod && paymentKind.paymentMethod.leaveascredit) {
              receipt.set('payment', OB.DEC.sub(receipt.get('payment'), receipt.get('payments').models[i].get('amount')));
              receipt.set('paidOnCredit', true);
            }
          }
        } else {
          for (i = 0; i < receipt.get('payments').length; i++) {
            paymentKind = OB.MobileApp.model.paymentnames[receipt.get('payments').models[i].get('kind')];
            if (paymentKind && paymentKind.paymentMethod && paymentKind.paymentMethod.leaveascredit) {
              receipt.set('payment', OB.DEC.sub(receipt.get('payment'), receipt.get('payments').models[i].get('amount')));
              receipt.set('paidOnCredit', true);
            }
          }
        }

        // There is only 1 receipt object.
        receipt.trigger('closed', {
          callback: function (args) {
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
              // hasBeenPaid is the only difference allowed in the receipt
              delete diff.hasbeenpaid;
              // verify if there have been any modification to the receipt
              var diffStringified = JSON.stringify(diff, undefined, 2);
              if (diffStringified !== '{}') {
                OB.error("The receipt has been modified while it was being closed:\n" + diffStringified + "\n");
              }
              receipt.setIsCalculateReceiptLockState(false);
              receipt.setIsCalculateGrossLockState(false);

              orderList.deleteCurrent();
              orderList.synchronizeCurrentOrder();
            }
            enyo.$.scrim.hide();
            OB.UTIL.SynchronizationHelper.finished(synchId, "receipt.paymentAccepted");
          }
        });

      });
    }, this);

    receipt.on('paymentDone', function (openDrawer) {
      var process = new OB.DS.Process('org.openbravo.retail.posterminal.process.IsOrderCancelled'),
          triggerPaymentAccepted;

      triggerPaymentAccepted = function () {
        if (receipt.get('doCancelAndReplace') && receipt.get('replacedorder')) {
          process.exec({
            orderId: receipt.get('replacedorder'),
            setCancelled: true
          }, function (data) {
            if (data && data.exception) {
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBMOBC_OfflineWindowRequiresOnline'));
              return;
            } else if (data && data.orderCancelled) {
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_OrderReplacedError'));
              return;
            } else {
              receipt.trigger('paymentAccepted');
            }
          });
        } else {
          receipt.trigger('paymentAccepted');
        }
      };

      if (receipt.overpaymentExists()) {
        var symbol = OB.MobileApp.model.get('terminal').symbol;
        var symbolAtRight = OB.MobileApp.model.get('terminal').currencySymbolAtTheRight;
        var amount = receipt.getPaymentStatus().overpayment;
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_OverpaymentWarningTitle'), OB.I18N.getLabel('OBPOS_OverpaymentWarningBody', [OB.I18N.formatCurrencyWithSymbol(amount, symbol, symbolAtRight)]), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function () {
            if (openDrawer) {
              OB.POS.hwserver.openDrawer({
                openFirst: false,
                receipt: receipt
              }, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales);
            }
            triggerPaymentAccepted();
          }
        }, {
          label: OB.I18N.getLabel('OBMOBC_LblCancel')
        }]);
      } else if ((OB.DEC.abs(receipt.getPayment()) !== OB.DEC.abs(receipt.getGross())) && (!receipt.isLayaway() && !receipt.get('paidOnCredit'))) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_PaymentAmountDistinctThanReceiptAmountTitle'), OB.I18N.getLabel('OBPOS_PaymentAmountDistinctThanReceiptAmountBody'), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function () {
            triggerPaymentAccepted();
          }
        }, {
          label: OB.I18N.getLabel('OBMOBC_LblCancel')
        }]);
      } else {
        if (openDrawer) {
          OB.POS.hwserver.openDrawer({
            openFirst: true,
            receipt: receipt
          }, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales);
        }
        triggerPaymentAccepted();
      }
    }, this);

    this.get('multiOrders').on('paymentAccepted', function () {
      var synchIdPaymentAccepted = OB.UTIL.SynchronizationHelper.busyUntilFinishes("multiOrders.paymentAccepted");
      OB.UTIL.showLoading(true);
      var ordersLength = this.get('multiOrders').get('multiOrdersList').length;

      function readyToSendFunction() {
        //this function is executed when all orders are ready to be sent
        me.get('leftColumnViewManager').setOrderMode();
        if (me.get('orderList').length > _.filter(me.get('multiOrders').get('multiOrdersList').models, function (order) {
          return !order.get('isLayaway');
        }).length) {
          me.get('orderList').addNewOrder();
        }
      }

      // this var is a function (copy of the above one) which is called by every items, but it is just executed once (when ALL items has called to it)
      var SyncReadyToSendFunction = _.after(this.get('multiOrders').get('multiOrdersList').length, readyToSendFunction);

      function prepareToSendCallback(order) {
        var auxReceipt = new OB.Model.Order();
        OB.UTIL.clone(order, auxReceipt);

        if (order.get('orderType') !== 2 && order.get('orderType') !== 3) {
          var negativeLines = _.filter(order.get('lines').models, function (line) {
            return line.get('qty') < 0;
          }).length;
          if (negativeLines === order.get('lines').models.length) {
            order.setOrderType('OBPOS_receipt.return', OB.DEC.One, {
              applyPromotions: false,
              saveOrder: false
            });
          } else {
            order.setOrderType('', OB.DEC.Zero, {
              applyPromotions: false,
              saveOrder: false
            });
          }
        }
        me.get('multiOrders').trigger('closed', order);
        if (!OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
          enyo.$.scrim.hide();
          me.get('multiOrders').trigger('print', order, {
            offline: true
          }); // to guaranty execution order
          SyncReadyToSendFunction();

          auxReceiptList.push(auxReceipt);
          if (auxReceiptList.length === me.get('multiOrders').get('multiOrdersList').length) {
            OB.UTIL.cashUpReport(auxReceiptList);
            auxReceiptList = [];
          }
        }
      }
      var i, j;
      for (j = 0; j < ordersLength; j++) {
        //Create the negative payment for change
        var iter = this.get('multiOrders').get('multiOrdersList').at(j);
        var amountToPay = !_.isUndefined(iter.get('amountToLayaway')) && !_.isNull(iter.get('amountToLayaway')) ? iter.get('amountToLayaway') : OB.DEC.sub(iter.get('gross'), iter.get('payment'));
        while (((_.isUndefined(iter.get('amountToLayaway')) || iter.get('amountToLayaway') > 0) && iter.get('gross') > iter.get('payment')) || (iter.get('amountToLayaway') > 0)) {
          for (i = 0; i < this.get('multiOrders').get('payments').length; i++) {
            var payment = this.get('multiOrders').get('payments').at(i),
                paymentMethod = OB.MobileApp.model.paymentnames[payment.get('kind')];
            //FIXME:Change is always given back in store currency
            if (this.get('multiOrders').get('change') > 0 && paymentMethod.paymentMethod.iscash) {
              payment.set('origAmount', OB.DEC.sub(payment.get('origAmount'), this.get('multiOrders').get('change')));
              this.get('multiOrders').set('change', OB.DEC.Zero);
            }
            if (payment.get('origAmount') <= amountToPay) {
              var bigDecAmount = new BigDecimal(String(OB.DEC.mul(payment.get('origAmount'), paymentMethod.mulrate)));
              iter.addPayment(new OB.Model.PaymentLine({
                'kind': payment.get('kind'),
                'name': payment.get('name'),
                'amount': OB.DEC.toNumber(bigDecAmount),
                'rate': paymentMethod.rate,
                'mulrate': paymentMethod.mulrate,
                'isocode': paymentMethod.isocode,
                'allowOpenDrawer': payment.get('allowopendrawer'),
                'isCash': payment.get('iscash'),
                'openDrawer': payment.get('openDrawer'),
                'printtwice': payment.get('printtwice')
              }));
              if (!_.isUndefined(iter.get('amountToLayaway')) && !_.isNull(iter.get('amountToLayaway'))) {
                iter.set('amountToLayaway', OB.DEC.sub(iter.get('amountToLayaway'), payment.get('origAmount')));
              }
              this.get('multiOrders').get('payments').remove(this.get('multiOrders').get('payments').at(i));
              amountToPay = !_.isUndefined(iter.get('amountToLayaway')) && !_.isNull(iter.get('amountToLayaway')) ? iter.get('amountToLayaway') : OB.DEC.sub(iter.get('gross'), iter.get('payment'));
            } else {
              var bigDecAmountAux, amtAux;
              if (j === this.get('multiOrders').get('multiOrdersList').length - 1 && !paymentMethod.paymentMethod.iscash) {
                bigDecAmountAux = new BigDecimal(String(payment.get('origAmount')));
                amtAux = OB.DEC.toNumber(bigDecAmountAux);
                this.get('multiOrders').get('payments').at(i).set('origAmount', OB.DEC.sub(this.get('multiOrders').get('payments').at(i).get('origAmount'), payment.get('origAmount')));
              } else {
                bigDecAmountAux = new BigDecimal(String(OB.DEC.mul(amountToPay, paymentMethod.mulrate)));
                amtAux = OB.DEC.toNumber(bigDecAmountAux);
                this.get('multiOrders').get('payments').at(i).set('origAmount', OB.DEC.sub(this.get('multiOrders').get('payments').at(i).get('origAmount'), amountToPay));
              }

              iter.addPayment(new OB.Model.PaymentLine({
                'kind': payment.get('kind'),
                'name': payment.get('name'),
                'amount': amtAux,
                'rate': paymentMethod.rate,
                'mulrate': paymentMethod.mulrate,
                'isocode': paymentMethod.isocode,
                'allowOpenDrawer': payment.get('allowopendrawer'),
                'isCash': payment.get('iscash'),
                'openDrawer': payment.get('openDrawer'),
                'printtwice': payment.get('printtwice')
              }));
              if (!_.isUndefined(iter.get('amountToLayaway')) && !_.isNull(iter.get('amountToLayaway'))) {
                iter.set('amountToLayaway', OB.DEC.sub(iter.get('amountToLayaway'), amtAux));
              }
              amountToPay = !_.isUndefined(iter.get('amountToLayaway')) && !_.isNull(iter.get('amountToLayaway')) ? iter.get('amountToLayaway') : OB.DEC.sub(iter.get('gross'), iter.get('payment'));
              break;
            }
          }
        }
        iter.prepareToSend(prepareToSendCallback);
      }
      OB.UTIL.SynchronizationHelper.finished(synchIdPaymentAccepted, "multiOrders.paymentAccepted");
    }, this);

    this.get('multiOrders').on('paymentDone', function (openDrawer) {
      var me = this,
          paymentstatus = this.get('multiOrders');
      if (OB.DEC.compare(OB.DEC.sub(paymentstatus.get('payment'), paymentstatus.get('total'))) > 0) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_OverpaymentWarningTitle'), OB.I18N.getLabel('OBPOS_OverpaymentWarningBody'), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function () {
            if (openDrawer) {
              OB.POS.hwserver.openDrawer({
                openFirst: false,
                receipt: me.get('multiOrders')
              }, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales);
            }
            me.get('multiOrders').trigger('paymentAccepted');
          }
        }, {
          label: OB.I18N.getLabel('OBMOBC_LblCancel')
        }]);
      } else {
        if (openDrawer) {
          OB.POS.hwserver.openDrawer({
            openFirst: true,
            receipt: me.get('multiOrders')
          }, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerSales);
        }
        this.get('multiOrders').trigger('paymentAccepted');
      }
    }, this);

    // Listening events that cause a discount recalculation
    receipt.get('lines').on('add change:qty change:price', function (line) {
      if (!receipt.get('isEditable')) {
        return;
      }
      //When we do not want to launch promotions process (Not apply or remove discounts)
      if (receipt.get('cloningReceipt') || receipt.get('skipApplyPromotions') || line.get('skipApplyPromotions')) {
        return;
      }
      // Calculate the receipt
      receipt.calculateReceipt(null, line);
    }, this);

    receipt.get('lines').on('remove', function () {
      if (!receipt.get('isEditable')) {
        return;
      }
      // Calculate the receipt
      receipt.calculateReceipt();
    });

    receipt.on('change:bp', function () {
      if (!receipt.get('isEditable') || receipt.get('lines').length === 0) {
        return;
      }
      receipt.get('lines').forEach(function (l) {
        l.unset('noDiscountCandidates', {
          silent: true
        });
      });
      if (!OB.MobileApp.model.hasPermission('EnableMultiPriceList', true)) {
        // Calculate the receipt only if it's not multipricelist
        receipt.calculateReceipt();
      }
    }, this);

    receipt.on('voidLayaway', function () {
      var finishVoidLayaway = function () {
          var synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes('finishVoidLayaway');
          var process = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessVoidLayaway');
          var auxReceipt = new OB.Model.Order();
          OB.UTIL.clone(receipt, auxReceipt);
          auxReceipt.prepareToSend(function () {
            OB.UTIL.cashUpReport(auxReceipt, function () {
              OB.UTIL.calculateCurrentCash();
            });
          });
          receipt.set('obposAppCashup', OB.MobileApp.model.get('terminal').cashUpId);
          receipt.set('timezoneOffset', new Date().getTimezoneOffset());
          receipt.set('gross', OB.DEC.mul(receipt.get('gross'), -1));
          receipt.get('payments').forEach(function (payment) {
            payment.set('origAmount', OB.DEC.mul(payment.get('origAmount'), -1));
            payment.set('paid', OB.DEC.mul(payment.get('paid'), -1));
          });
          process.exec({
            messageId: OB.UTIL.get_UUID(),
            data: [{
              id: receipt.get('id'),
              order: receipt
            }]
          }, function (data) {
            if (data && data.exception) {
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorVoidLayaway'));
              OB.UTIL.SynchronizationHelper.finished(synchId, "finishVoidLayaway");
            } else {
              OB.Dal.remove(receipt, null, function (tx, err) {
                OB.UTIL.showError(err);
              });
              receipt.trigger('print');
              if (receipt.get('layawayGross')) {
                receipt.set('layawayGross', null);
              }
              orderList.deleteCurrent();
              receipt.trigger('change:gross', receipt);

              OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgSuccessVoidLayaway'));
              OB.UTIL.SynchronizationHelper.finished(synchId, "finishVoidLayaway");
            }
          }, function () {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline'));
            OB.UTIL.SynchronizationHelper.finished(synchId, "finishVoidLayaway");
          });
          };

      if (receipt.overpaymentExists()) {
        var symbol = OB.MobileApp.model.get('terminal').symbol;
        var symbolAtRight = OB.MobileApp.model.get('terminal').currencySymbolAtTheRight;
        var amount = receipt.getPaymentStatus().overpayment;
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_OverpaymentWarningTitle'), OB.I18N.getLabel('OBPOS_OverpaymentWarningBody', [OB.I18N.formatCurrencyWithSymbol(amount, symbol, symbolAtRight)]), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function () {
            finishVoidLayaway();
          }
        }, {
          label: OB.I18N.getLabel('OBMOBC_LblCancel')
        }]);
      } else {
        finishVoidLayaway();
      }
    }, this);

    receipt.on('cancelLayaway', function () {
      var finishCancelLayaway = function () {
          var cancelLayawayObj = {},
              cancelLayawayModel = new OB.Model.CancelLayaway(),
              docNo = OB.MobileApp.model.getNextDocumentno(),
              documentNo = receipt.get('documentNo'),
              process = new OB.DS.Process('org.openbravo.retail.posterminal.process.IsOrderCancelled');

          process.exec({
            orderId: receipt.get('id'),
            setCancelled: true
          }, function (data) {
            if (data && data.exception) {
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBMOBC_OfflineWindowRequiresOnline'));
              return;
            } else if (data && data.orderCancelled) {
              OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_Error'), OB.I18N.getLabel('OBPOS_LayawayCancelledError'));
              return;
            } else {
              cancelLayawayObj.negativeDocNo = docNo;
              cancelLayawayObj.orderId = receipt.get('id');
              cancelLayawayObj.payments = JSON.parse(JSON.stringify(receipt.get('payments')));
              cancelLayawayObj.gross = receipt.getPaymentStatus().isNegative ? OB.DEC.mul(receipt.get('gross'), -1) : receipt.get('gross');
              cancelLayawayObj.paidOnCredit = receipt.get("paidOnCredit");
              cancelLayawayObj.posTerminal = receipt.get("posTerminal");
              cancelLayawayObj.payment = receipt.get("payment");
              cancelLayawayObj.isQuotation = receipt.get("isQuotation");
              cancelLayawayObj.orderType = 2;
              cancelLayawayObj.isPaid = receipt.get("isPaid");
              cancelLayawayObj.isLayaway = receipt.get("isLayaway");
              cancelLayawayObj.paidOnCredit = receipt.get("paidOnCredit");
              cancelLayawayObj.defaultPaymentType = receipt.get("defaultPaymentType");
              cancelLayawayObj.obposAppCashup = OB.MobileApp.model.get('terminal').cashUpId;
              cancelLayawayObj.timezoneOffset = new Date().getTimezoneOffset();
              if (receipt.get('deliveredQuantityAmount')) {
                cancelLayawayObj.deliveredQuantityAmount = OB.I18N.formatCurrency(receipt.getDeliveredQuantityAmount());
              }

              cancelLayawayObj.payments.forEach(function (payment) {
                payment.origAmount = receipt.getPaymentStatus().isNegative ? OB.DEC.mul(payment.origAmount, -1) : payment.origAmount;
                payment.paid = receipt.getPaymentStatus().isNegative ? OB.DEC.mul(payment.paid, -1) : payment.paid;
              });

              cancelLayawayModel.set('json', JSON.stringify(cancelLayawayObj));

              OB.Dal.save(cancelLayawayModel, function () {
                OB.MobileApp.model.updateDocumentSequenceWhenOrderSaved(docNo.documentnoSuffix, OB.MobileApp.model.set('quotationDocumentSequence'), function () {
                  var orderId = receipt.id;

                  OB.MobileApp.model.runSyncProcess();
                  orderList.deleteCurrent();
                  OB.Dal.get(OB.Model.Order, orderId, function (model) {
                    function cancelAndNew() {
                      OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgSuccessCancelLayaway', [documentNo]));
                    }
                    if (model) {
                      OB.Dal.remove(model, function (tx) {
                        cancelAndNew();
                      }, function (tx, err) {
                        OB.UTIL.showError(err);
                      });
                    } else {
                      cancelAndNew();
                    }
                  }, function (tx, err) {
                    OB.UTIL.showError(err);
                  });
                });
              }, function () {
                OB.error(arguments);
              });
              receipt.set('negativeDocNo', docNo.documentNo);
              receipt.trigger('print');
            }
          }, function () {
            OB.error(arguments);
          });
          };
      if (receipt.overpaymentExists()) {
        var symbol = OB.MobileApp.model.get('terminal').symbol;
        var symbolAtRight = OB.MobileApp.model.get('terminal').currencySymbolAtTheRight;
        var amount = receipt.getPaymentStatus().overpayment;
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_OverpaymentWarningTitle'), OB.I18N.getLabel('OBPOS_OverpaymentWarningBody', [OB.I18N.formatCurrencyWithSymbol(amount, symbol, symbolAtRight)]), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function () {
            finishCancelLayaway();
          }
        }, {
          label: OB.I18N.getLabel('OBMOBC_LblCancel')
        }]);
      } else {
        finishCancelLayaway();
      }
    }, this);

    callback();
  },
  loadModels: function (loadModelsCallback) {
    var me = this;

    this.set('filter', []);
    this.set('brandFilter', []);

    function searchCurrentBP(callback) {
      var errorCallback = function () {
          OB.error(OB.I18N.getLabel('OBPOS_BPInfoErrorTitle') + '. Message: ' + OB.I18N.getLabel('OBPOS_BPInfoErrorMessage'));
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_BPInfoErrorTitle'), OB.I18N.getLabel('OBPOS_BPInfoErrorMessage'), [{
            label: OB.I18N.getLabel('OBPOS_Reload')
          }], {
            onShowFunction: function (popup) {
              popup.$.headerCloseButton.hide();
              OB.UTIL.localStorage.removeItem('POSLastTotalRefresh');
              OB.UTIL.localStorage.removeItem('POSLastIncRefresh');
            },
            onHideFunction: function () {
              OB.UTIL.localStorage.removeItem('POSLastTotalRefresh');
              OB.UTIL.localStorage.removeItem('POSLastIncRefresh');
              window.location.reload();
            },
            autoDismiss: false
          });
          };

      function successCallbackBPs(dataBps) {
        var partnerAddressId = OB.MobileApp.model.get('terminal').partnerAddress,
            successCallbackBPLoc;

        if (dataBps) {
          if (partnerAddressId && dataBps.get('locId') !== partnerAddressId) {
            // Set default location
            successCallbackBPLoc = function (bpLoc) {
              dataBps.set('locId', bpLoc.get('id'));
              dataBps.set('locName', bpLoc.get('name'));
              dataBps.set('cityName', bpLoc.get('cityName'));
              dataBps.set('countryName', bpLoc.get('countryName'));
              dataBps.set('postalCode', bpLoc.get('postalCode'));
              dataBps.set('locationModel', bpLoc);
              OB.MobileApp.model.set('businessPartner', dataBps);
              me.loadUnpaidOrders(callback);
            };
            OB.Dal.get(OB.Model.BPLocation, partnerAddressId, successCallbackBPLoc, errorCallback, errorCallback);
          } else {
            // set locationModel
            if (dataBps.get('locId')) {
              successCallbackBPLoc = function (bpLoc) {
                dataBps.set('locId', bpLoc.get('id'));
                dataBps.set('locName', bpLoc.get('name'));
                dataBps.set('cityName', bpLoc.get('cityName'));
                dataBps.set('countryName', bpLoc.get('countryName'));
                dataBps.set('postalCode', bpLoc.get('postalCode'));
                dataBps.set('locationModel', bpLoc);
                OB.MobileApp.model.set('businessPartner', dataBps);
                me.loadUnpaidOrders(callback);
              };
              OB.Dal.get(OB.Model.BPLocation, dataBps.get('locId'), successCallbackBPLoc, errorCallback, errorCallback);
            }
          }
        }
      }
      OB.Dal.get(OB.Model.BusinessPartner, OB.MobileApp.model.get('businesspartner'), successCallbackBPs, errorCallback, errorCallback);
    }

    //Because in terminal we've the BP id and we want to have the BP model.
    //In this moment we can ensure data is already loaded in the local database
    searchCurrentBP(loadModelsCallback);

  },

  /**
   * This method is invoked before paying a ticket, it is intended to do global
   * modifications in the ticket with OBPOS_PrePaymentHook hook, after this hook
   * execution checkPaymentApproval is invoked
   * OBPOS_PrePaymentApproval can be used to ensure certain order within the
   * same hook
   */
  completePayment: function (caller) {
    var me = this;
    OB.UTIL.HookManager.executeHooks('OBPOS_PrePaymentHook', {
      context: this,
      caller: caller
    }, function () {
      OB.UTIL.HookManager.executeHooks('OBPOS_PrePaymentApproval', {
        context: me,
        caller: caller
      }, function () {
        me.checkPaymentApproval(caller);
      });
    });
  },

  /**
   * Hooks for OBPOS_CheckPaymentApproval can modify args.approved to check if
   * payment is approved. In case value is true the process will continue, if not
   * it is aborted
   */
  checkPaymentApproval: function (caller) {
    var me = this;
    OB.UTIL.HookManager.executeHooks('OBPOS_CheckPaymentApproval', {
      approvals: [],
      context: this,
      caller: caller
    }, function (args) {
      var negativeLines = _.filter(me.get('order').get('lines').models, function (line) {
        return line.get('qty') < 0;
      }).length;
      if (negativeLines > 0 && !OB.MobileApp.model.get('permissions')['OBPOS_approval.returns']) {
        args.approvals.push('OBPOS_approval.returns');
      }
      if (args.approvals.length > 0) {
        OB.UTIL.Approval.requestApproval(
        me, args.approvals, function (approved) {
          if (approved) {
            me.trigger('approvalChecked', {
              approved: (args.approved !== undefined) ? args.approved : true
            });
          }
        });
      } else {
        me.trigger('approvalChecked', {
          approved: (args.approved !== undefined) ? args.approved : true
        });
      }
    });
  },

  /**
   * Approval final stage. Where approvalChecked event is triggered, with approved
   * property set to true or false regarding if approval was finally granted. In
   * case of granted approval, the approval is added to the order so it can be saved
   * in backend for audit purposes.
   */
  approvedRequest: function (approved, supervisor, approvalType, callback) {
    var order = this.get('order'),
        newApprovals = [],
        approvals, approval, i, date;


    approvals = order.get('approvals') || [];
    if (!Array.isArray(approvalType)) {
      approvalType = [approvalType];
    }

    _.each(approvals, function (appr) {
      var results;
      results = _.find(approvalType, function (apprType) {
        return apprType === appr.approvalType;
      });

      if (_.isUndefined(results)) {
        newApprovals.push(appr);
      }

    });

    if (approved) {
      date = new Date();
      date = date.getTime();
      for (i = 0; i < approvalType.length; i++) {
        approval = {
          approvalType: approvalType[i],
          userContact: supervisor.get('id'),
          created: (new Date()).getTime()
        };
        newApprovals.push(approval);
      }
      order.set('approvals', newApprovals);
    }


    this.trigger('approvalChecked', {
      approved: approved
    });
    if (enyo.isFunction(callback)) {
      callback(approved, supervisor, approvalType);
    }
  }
});