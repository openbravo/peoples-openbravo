/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $ */

OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
OB.OBPOSPointOfSale.Model = OB.OBPOSPointOfSale.Model || {};
OB.OBPOSPointOfSale.UI = OB.OBPOSPointOfSale.UI || {};

//Window model
OB.OBPOSPointOfSale.Model.PointOfSale = OB.Model.WindowModel.extend({
  models: [OB.Model.TaxRate, OB.Model.Product, OB.Model.ProductPrice, OB.Model.ProductCategory, OB.Model.BusinessPartner, OB.Model.Order, OB.Model.DocumentSequence, OB.Model.Discount, OB.Model.DiscountFilterProduct],

  loadUnpaidOrders: function() {
    // Shows a modal window with the orders pending to be paid
    var orderlist = this.get('orderList'),
        criteria = {
        'hasbeenpaid': 'N',
        'session': OB.POS.modelterminal.get('session')
        };
    OB.Dal.find(OB.Model.Order, criteria, function(ordersNotPaid) { //OB.Dal.find success
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
        OB.UTIL.showAlert.display(loadOrderStr, OB.I18N.getLabel('OBUIAPP_Info'));
      }
    }, function() { //OB.Dal.find error
      // If there is an error fetching the pending orders,
      // add an initial empty order
      orderlist.addNewOrder();
    });
  },

  processPaidOrders: function() {
    // Processes the paid, unprocessed orders
    var orderlist = this.get('orderList'),
        me = this,
        criteria = {
        hasbeenpaid: 'Y'
        };
    if (OB.POS.modelterminal.get('connectedToERP')) {
      OB.Dal.find(OB.Model.Order, criteria, function(ordersPaidNotProcessed) { //OB.Dal.find success
        var successCallback, errorCallback;
        if (!ordersPaidNotProcessed || ordersPaidNotProcessed.length === 0) {
          return;
        }
        successCallback = function() {
          $('.alert:contains("' + OB.I18N.getLabel('OBPOS_ProcessPendingOrders') + '")').alert('close');
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgSuccessProcessOrder'));
        };
        errorCallback = function() {
          $('.alert:contains("' + OB.I18N.getLabel('OBPOS_ProcessPendingOrders') + '")').alert('close');
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorProcessOrder'));
        };
        OB.UTIL.showAlert.display(OB.I18N.getLabel('OBPOS_ProcessPendingOrders'), OB.I18N.getLabel('OBUIAPP_Info'));
        OB.UTIL.processOrders(me, ordersPaidNotProcessed, successCallback, errorCallback);
      });
    }
  },

  applyDiscounts: function(line) {
    var discounts = [],
        receipt = this.get('order');
    if (line) {
      // check which are the discounts to be applied
      // 2x1 example
      var
      criteria = {
        '_sql': 'where exists (select 1 from m_offer_product p where m_offer.m_offer_id = p.m_offer_id and m_product_id = \'' + line.get('product').id + '\')'

      };
      OB.Dal.find(OB.Model.Discount, criteria, function(d) { //OB.Dal.find success
        console.log('ds', d);
        d.forEach(function(disc) {
          discounts.push({
            name: disc.get('name'),
            gross: line.get('priceList')
          });
        });
        receipt.addDiscount(line, discounts);
      }, function() {
      });


      //--
    } else {
      // TODO: apply discounts for the whole ticket
    }
  },

  init: function() {
    var receipt = new OB.Model.Order(),
        discounts, ordersave, taxes, orderList, hwManager;
    this.set('order', receipt);
    orderList = new OB.Collection.OrderList(receipt);
    this.set('orderList', orderList);

    discounts = new OB.DATA.OrderDiscount(receipt);
    ordersave = new OB.DATA.OrderSave(this);
    taxes = new OB.DATA.OrderTaxes(receipt);

    OB.POS.modelterminal.saveDocumentSequenceInDB();
    this.processPaidOrders();
    this.loadUnpaidOrders();

    receipt.on('paymentDone', function() {
      receipt.calculateTaxes(function() {
        receipt.trigger('closed');
        receipt.trigger('print'); // to guaranty execution order
        orderList.deleteCurrent();
      });
    }, this);

    this.printReceipt = new OB.OBPOSPointOfSale.Print.Receipt(receipt);
    this.printLine = new OB.OBPOSPointOfSale.Print.ReceiptLine(receipt);

    // Listening events that cause a discount recalculation
    receipt.get('lines').on('add change:qty', function(line) {
      this.applyDiscounts(line);
    }, this);

    receipt.on('change:bp', function(line) {
      this.applyDiscounts();
    }, this);
  }
});