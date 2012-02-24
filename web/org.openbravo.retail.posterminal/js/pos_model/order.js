
  OBPOS.Model = {};

  // Sales.OrderLine Model
  OBPOS.Model.OrderLine = Backbone.Model.extend({
    defaults : {
      productid: null,
      productidentifier: null,
      qty: 0,
      price: 0
    },
    
    printQty: function () {
      return this.get('qty').toString();
    },
    
    printPrice: function () {
      return OBPOS.Format.formatNumber(this.get('price'), {
        decimals: 2,
        decimal: '.',
        group: ',',
        currency: '$#'
      });
    },
    
    getNet: function () {
      return this.get('price') * this.get('qty');
    },
    
    printNet: function () {
      return OBPOS.Format.formatNumber(this.getNet(), {
        decimals: 2,
        decimal: '.',
        group: ',',
        currency: '$#'
      });
    }
  });
  
  // Sales.OrderLineCol Model.  
  OBPOS.Model.OrderLineCol = Backbone.Collection.extend({
    model: OBPOS.Model.OrderLine
  });

  // Sales.Order Model.
  OBPOS.Model.Order = Backbone.Model.extend({
    initialize : function () {
      this.set('lines', new OBPOS.Model.OrderLineCol());
    },
    
    getNet: function () {
      return this.get('lines').reduce(function (memo, e) { 
        return memo + e.getNet(); 
      }, 0 );
    },
    
    printNet: function () {
      return OBPOS.Format.formatNumber(this.getNet(), {
        decimals: 2,
        decimal: '.',
        group: ',',
        currency: '$#'
      });      
    }
  });
  

