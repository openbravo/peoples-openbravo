OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
OB.OBPOSPointOfSale.Model = OB.OBPOSPointOfSale.Model || {};
OB.OBPOSPointOfSale.UI = OB.OBPOSPointOfSale.UI || {};

//Window model
OB.OBPOSPointOfSale.Model.PointOfSale = OB.Model.WindowModel.extend({
  models: [OB.Model.TaxRate, OB.Model.Product, OB.Model.ProductPrice, OB.Model.ProductCategory, OB.Model.BusinessPartner, OB.Model.Order, OB.Model.DocumentSequence],

  loadUnpaidOrders: function() {
    // Shows a modal window with the orders pending to be paid
    var orderlist = this.get('orderList');
    criteria = {
      'hasbeenpaid': 'N'
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
        OB.UTIL.showAlert(loadOrderStr, OB.I18N.getLabel('OBUIAPP_Info'));
      }
    }, function() { //OB.Dal.find error
      // If there is an error fetching the pending orders,
      // add an initial empty order
      orderlist.addNewOrder();
    });
  },

  processPaidOrders: function() {
    // Processes the paid, unprocessed orders
    var orderlist = this.get('orderList');
        criteria = {
        hasbeenpaid: 'Y'
        };
    if (OB.POS.modelterminal.get('connectedToERP')) {
      OB.Dal.find(OB.Model.Order, criteria, function(ordersPaidNotProcessed) { //OB.Dal.find success
        var successCallback, errorCallback;
        if (!ordersPaidNotProcessed) {
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
        OB.UTIL.showAlert(OB.I18N.getLabel('OBPOS_ProcessPendingOrders'), OB.I18N.getLabel('OBUIAPP_Info'));
        OB.UTIL.processOrders(this, ordersPaidNotProcessed, successCallback, errorCallback);
      });
    }
  },

  init: function() {
    var modelOrder = new OB.Model.Order(),
        discounts, ordersave;
    this.set('order', modelOrder);
    this.set('orderList', new OB.Collection.OrderList(modelOrder));

    discounts = new OB.DATA.OrderDiscount(modelOrder);
    ordersave = new OB.DATA.OrderSave(this);

    OB.POS.modelterminal.saveDocumentSequenceInDB();
    this.processPaidOrders();
    this.loadUnpaidOrders();
  }
});