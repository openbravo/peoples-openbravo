/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
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
  OB.Model.CurrencyPanel, OB.Model.SalesRepresentative],

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
        orderlist.addFirstOrder();
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
      orderlist.addFirstOrder();
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
        i, j, k, multiOrders = new OB.Model.MultiOrders(),
        me = this,
        iter, isNew = false,
        discounts, ordersave, customersave, taxes, orderList, hwManager, ViewManager;

    function success() {
      return true;
    }

    function error() {
      OB.UTIL.showError('Error removing');
    }

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
    this.set('multiOrders', multiOrders);

    this.get('multiOrders').on('paymentAccepted', function () {
      OB.UTIL.showLoading(true);
      for (j = 0; j < this.get('multiOrders').get('multiOrdersList').length; j++) {
        //Create the negative payment for change
        iter = this.get('multiOrders').get('multiOrdersList').at(j);
        var clonedCollection = new Backbone.Collection(),
            amountToPay = iter.get('amountToLayaway') ? iter.get('amountToLayaway') : OB.DEC.sub(iter.get('gross'), iter.get('payment'));
        while ((iter.get('amountToLayaway') !== 0 && iter.get('gross') > iter.get('payment')) || (iter.get('amountToLayaway') > 0)) {
          for (i = 0; i < this.get('multiOrders').get('payments').length; i++) {
            //FIXME: MULTICURRENCY
            var payment = this.get('multiOrders').get('payments').at(i),
                paymentMethod = OB.POS.terminal.terminal.paymentnames[payment.get('kind')];

            if (payment.get('origAmount') <= amountToPay) {
              iter.addPayment(new OB.Model.PaymentLine({
                'kind': payment.get('kind'),
                'name': payment.get('name'),
                'amount': OB.DEC.mul(payment.get('origAmount'), paymentMethod.mulrate),
                'rate': paymentMethod.rate,
                'mulrate': paymentMethod.mulrate,
                'isocode': paymentMethod.isocode,
                'openDrawer': payment.get('openDrawer')
              }));
              if (iter.get('amountToLayaway')) {
                iter.set('amountToLayaway', OB.DEC.sub(iter.get('amountToLayaway'), payment.get('origAmount')));
              }
              this.get('multiOrders').get('payments').remove(this.get('multiOrders').get('payments').at(i));
              amountToPay = iter.get('amountToLayaway') ? iter.get('amountToLayaway') : OB.DEC.sub(iter.get('gross'), iter.get('payment'));
            } else {
              this.get('multiOrders').get('payments').at(i).set('origAmount', OB.DEC.sub(this.get('multiOrders').get('payments').at(i).get('origAmount'), amountToPay));
              iter.addPayment(new OB.Model.PaymentLine({
                'kind': payment.get('kind'),
                'name': payment.get('name'),
                'amount': OB.DEC.mul(amountToPay, paymentMethod.mulrate),
                'rate': paymentMethod.rate,
                'mulrate': paymentMethod.mulrate,
                'isocode': paymentMethod.isocode,
                'openDrawer': payment.get('openDrawer')
              }));
              if (iter.get('amountToLayaway')) {
                iter.set('amountToLayaway', OB.DEC.sub(iter.get('amountToLayaway'), amountToPay));
              }
              amountToPay = iter.get('amountToLayaway') ? iter.get('amountToLayaway') : OB.DEC.sub(iter.get('gross'), iter.get('payment'));
              break;
            }
          }
        }
        this.get('multiOrders').trigger('closed', iter);
        iter.trigger('print'); // to guaranty execution order
      }
    }, this);

    customersave = new OB.DATA.CustomerSave(this);

    this.set('subWindowManager', new ViewManager());
    discounts = new OB.DATA.OrderDiscount(receipt);
    ordersave = new OB.DATA.OrderSave(this);
    taxes = new OB.DATA.OrderTaxes(receipt);

    OB.POS.modelterminal.saveDocumentSequenceInDB();
    this.processChangedCustomers();

    receipt.on('paymentAccepted', function () {
      receipt.prepareToSend(function () {
        //Create the negative payment for change
        var oldChange = receipt.get('change');
        var clonedCollection = new Backbone.Collection();
        if (!_.isUndefined(receipt.selectedPayment) && receipt.getChange() > 0) {
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
              'amount': OB.DEC.sub(0, OB.DEC.mul(receipt.getChange(), payment.mulrate)),
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

    receipt.on('paymentDone', function () {

      if (receipt.overpaymentExists()) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_OverpaymentWarningTitle'), OB.I18N.getLabel('OBPOS_OverpaymentWarningBody'), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          action: function () {
            receipt.trigger('paymentAccepted');
          }
        }, {
          label: OB.I18N.getLabel('OBMOBC_LblCancel')
        }]);
      } else {
        receipt.trigger('paymentAccepted');
      }
    }, this);

    this.get('multiOrders').on('paymentDone', function () {
      var me = this,
          paymentstatus = this.get('multiOrders');
      if (OB.DEC.compare(OB.DEC.sub(paymentstatus.get('payment'), paymentstatus.get('total'))) > 0) {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_OverpaymentWarningTitle'), OB.I18N.getLabel('OBPOS_OverpaymentWarningBody'), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          action: function () {
            me.get('multiOrders').trigger('paymentAccepted');
          }
        }, {
          label: OB.I18N.getLabel('OBMOBC_LblCancel')
        }]);
      } else {
        this.get('multiOrders').trigger('paymentAccepted');
      }
    }, this);
    //    FIXME: openDrawer for multiorders
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