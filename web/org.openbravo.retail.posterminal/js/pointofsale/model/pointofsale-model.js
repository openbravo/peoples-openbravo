/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $ Backbone enyo _ */

OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
OB.OBPOSPointOfSale.Model = OB.OBPOSPointOfSale.Model || {};
OB.OBPOSPointOfSale.UI = OB.OBPOSPointOfSale.UI || {};

//Window model
OB.OBPOSPointOfSale.Model.PointOfSale = OB.Model.WindowModel.extend({
  models: [{
    generatedModel: true,
    modelName: 'TaxRate'
  },
  OB.Model.Product, OB.Model.ProductCategory, OB.Model.BusinessPartner, OB.Model.BPCategory, OB.Model.Order, OB.Model.DocumentSequence, OB.Model.ChangedBusinessPartners,
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
  },
  OB.Model.CurrencyPanel],

  loadUnpaidOrders: function () {
    // Shows a modal window with the orders pending to be paid
    var orderlist = this.get('orderList'),
        criteria = {
        'hasbeenpaid': 'N',
        'session': OB.POS.modelterminal.get('session')
        };
    OB.Dal.find(OB.Model.Order, criteria, function (ordersNotPaid) { //OB.Dal.find success
      var currentOrder = {},
          loadOrderStr;
      if (!ordersNotPaid || ordersNotPaid.length === 0) {
        // If there are no pending orders,
        //  add an initial empty order
        orderlist.addNewOrder();
      } else {
        // The order object is stored in the json property of the row fetched from the database
        orderlist.reset(ordersNotPaid.models);
        // At this point it is sure that there exists at least one order
        currentOrder = ordersNotPaid.models[0];
        orderlist.load(currentOrder);
        loadOrderStr = OB.I18N.getLabel('OBPOS_Order') + currentOrder.get('documentNo') + OB.I18N.getLabel('OBPOS_Loaded');
        OB.UTIL.showAlert.display(loadOrderStr, OB.I18N.getLabel('OBPOS_Info'));
      }
    }, function () { //OB.Dal.find error
      // If there is an error fetching the pending orders,
      // add an initial empty order
      orderlist.addNewOrder();
    });
  },

  processChangedCustomers: function () {
    // Processes the customers who has been changed
    var me = this;

    if (OB.POS.modelterminal.get('connectedToERP')) {
      OB.Dal.find(OB.Model.ChangedBusinessPartners, null, function (customersChangedNotProcessed) { //OB.Dal.find success
        var successCallback, errorCallback;
        if (!customersChangedNotProcessed || customersChangedNotProcessed.length === 0) {
          OB.UTIL.processPaidOrders(me);
          me.loadUnpaidOrders();
          return;
        }
        successCallback = function () {
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_pendigDataOfCustomersProcessed'));

          OB.UTIL.processPaidOrders(me);
          me.loadUnpaidOrders();
        };
        errorCallback = function () {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_errorProcessingCustomersPendingData'));
          // we will not process pending orders in case there was an order while syncing customers
          me.loadUnpaidOrders();
        };
        customersChangedNotProcessed.each(function (cus) {
          cus.set('json', enyo.json.parse(cus.get('json')));
        });
        OB.UTIL.processCustomers(customersChangedNotProcessed, successCallback, errorCallback);
      });
    } else {
      //We are offline. We continue the normal flow
      me.loadUnpaidOrders();
    }
  },

  init: function () {
    var receipt = new OB.Model.Order(),
        discounts, ordersave, customersave, taxes, orderList, hwManager, ViewManager;

    function searchCurrentBP() {
      function errorCallback(tx, error) {
        OB.UTIL.showError("OBDAL error while getting BP info: " + error);
      }

      function successCallbackBPs(dataBps) {
        if (dataBps) {
          OB.POS.modelterminal.set('businessPartner', dataBps);
        }
      }
      OB.Dal.get(OB.Model.BusinessPartner, OB.POS.modelterminal.get('businesspartner'), successCallbackBPs, errorCallback);
    }

    //Because in terminal we've the BP id and we want to have the BP model.
    //In this moment we can ensure data is already loaded in the local database
    searchCurrentBP();

    ViewManager = Backbone.Model.extend({
      defaults: {
        currentWindow: {
          name: 'mainSubWindow',
          params: []
        }
      },
      initialize: function () {}
    });
    this.set('order', receipt);
    orderList = new OB.Collection.OrderList(receipt);
    this.set('orderList', orderList);
    this.set('customer', new OB.Model.BusinessPartner());

    customersave = new OB.DATA.CustomerSave(this);

    this.set('subWindowManager', new ViewManager());
    discounts = new OB.DATA.OrderDiscount(receipt);
    ordersave = new OB.DATA.OrderSave(this);
    taxes = new OB.DATA.OrderTaxes(receipt);

    OB.POS.modelterminal.saveDocumentSequenceInDB();
    this.processChangedCustomers();

    receipt.on('paymentDone', function () {
      receipt.prepareToSend(function () {
        //Create the negative payment for change
        var oldChange = receipt.get('change');
        var clonedCollection = new Backbone.Collection();
        if (!_.isUndefined(receipt.selectedPayment) && !_.isUndefined(receipt.getPaymentStatus()) && receipt.getPaymentStatus().change > 0) {
          var payToDo = receipt.getPaymentStatus();
          var payment = OB.POS.terminal.terminal.paymentnames[receipt.selectedPayment];
          receipt.get('payments').each(function (model) {
            clonedCollection.add(new Backbone.Model(model.toJSON()));
          });
          if (!payment.paymentMethod.iscash) {
            payment = OB.POS.terminal.terminal.paymentnames[OB.POS.modelterminal.get('paymentcash')];
          }
          if (receipt.get('orderType') === 0 || (receipt.get('orderType') === 2 && receipt.get('payment') >= receipt.get('gross'))) {
            receipt.addPayment(new OB.Model.PaymentLine({
              'kind': payment.payment.searchKey,
              'name': payment.payment.commercialName,
              'amount': OB.DEC.sub(0, OB.DEC.mul(payToDo.change, payment.mulrate)),
              'rate': payment.rate,
              'mulrate': payment.mulrate,
              'isocode': payment.isocode,
              'openDrawer': payment.paymentMethod.openDrawer
            }));
          }
          receipt.set('change', oldChange);
          receipt.trigger('closed');
          receipt.get('payments').reset();
          clonedCollection.each(function (model) {
            receipt.get('payments').add(new Backbone.Model(model.toJSON()), {
              silent: true
            });
          });
        } else {
          receipt.trigger('closed');
        }

        receipt.trigger('print'); // to guaranty execution order
        orderList.deleteCurrent();
      });
    }, this);

    receipt.on('openDrawer', function () {
      receipt.trigger('popenDrawer');
    }, this);

    this.printReceipt = new OB.OBPOSPointOfSale.Print.Receipt(receipt);
    this.printLine = new OB.OBPOSPointOfSale.Print.ReceiptLine(receipt);

    // Listening events that cause a discount recalculation
    receipt.get('lines').on('add change:qty change:gross change:net', function (line) {
      if (!receipt.get('isEditable')) {
        return;
      }
      OB.Model.Discounts.applyPromotions(receipt, line);
    }, this);

    receipt.get('lines').on('remove', function () {
      if (!receipt.get('isEditable')) {
        return;
      }
      OB.Model.Discounts.applyPromotions(receipt);
    });

    receipt.on('change:bp', function (line) {
      if (!receipt.get('isEditable')) {
        return;
      }
      OB.Model.Discounts.applyPromotions(receipt);
    }, this);
    receipt.on('voidLayaway', function () {
      var process = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessVoidLayaway');
      process.exec({
        order: receipt
      }, function (data, message) {
        if (data && data.exception) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorVoidLayaway'));
        } else {
          OB.Dal.remove(receipt, null, function (tx, err) {
            OB.UTIL.showError(err);
          });
          receipt.trigger('print');
          if (receipt.get('layawayGross')) {
            receipt.set('layawayGross', null);
          }
          orderList.deleteCurrent();
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgSuccessVoidLayaway'));
        }
      });

    }, this);
  }
});