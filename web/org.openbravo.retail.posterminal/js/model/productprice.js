/*global define,Backbone */

define([], function () {
  
  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};
  

  OB.MODEL.ProductPrice = Backbone.Collection.extend({
    
    constructor: function (priceListVersion, dataProduct, dataProductPrice) {
      this.priceListVersion = priceListVersion;
      this.dsProduct = dataProduct.ds;
      this.dsProductPrice = dataProductPrice.ds;
      Backbone.Collection.prototype.constructor.call(this);
    },
    inithandler : function (init) { 
       if (init) {
         init.call(this);
       }
    },
    setPriceListVersion: function(priceListVersion) {
      this.priceListVersion = priceListVersion;
    },
    exec : function (filter) {
      var me = this;

      this.dsProduct.exec(filter, function (data) {
        var i, find;
        
        find = function (item) {
          me.dsProductPrice.find({'priceListVersion': me.priceListVersion, 'product': item.product.id}, function(price) {
            if (price) {
              item.price = price;
              me.add(item);
            }
          });
        };
        
        me.reset();
        
        if (data.exception) {
          alert(data.exception.message);
        } else {
          for (i in data) {
            if(data.hasOwnProperty(i)) {
              find(data[i]);
            }
          }
        }
      });
    }
  });    
});