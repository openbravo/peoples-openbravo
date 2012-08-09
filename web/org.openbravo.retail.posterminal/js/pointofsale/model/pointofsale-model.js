OB.OBPOSPointOfSale = OB.OBPOSPointOfSale || {};
OB.OBPOSPointOfSale.Model = OB.OBPOSPointOfSale.Model || {};
OB.OBPOSPointOfSale.UI = OB.OBPOSPointOfSale.UI || {};

//Window model
OB.OBPOSPointOfSale.Model.PointOfSale = OB.Model.WindowModel.extend({
  models: [OB.Model.TaxRate, OB.Model.Product, OB.Model.ProductPrice, OB.Model.ProductCategory, OB.Model.BusinessPartner, OB.Model.Order, OB.Model.DocumentSequence],
  init: function() {
    var modelOrder = new OB.Model.Order(), discounts;
    this.set('order', modelOrder);
    this.set('orderList', new OB.Collection.OrderList());
    
    discounts = new OB.DATA.OrderDiscount(modelOrder);
    
  }
});