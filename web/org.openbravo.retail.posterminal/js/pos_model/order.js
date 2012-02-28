
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
    },
    
    addUnit : function (qty) {   
      qty = isNaN(qty) ? 1 : qty;        
      this.set('qty', this.get('qty') + qty);
    },
    
    removeUnit : function (qty) {   
      qty = isNaN(qty) ? 1 : qty;        
      this.set('qty', this.get('qty') - qty);
    },
    
    setUnit : function (qty) {   
      qty = isNaN(qty) ? this.get('qty') : qty;      
      this.set('qty', qty);
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
    },
    
    reset: function() {
      this.get('lines').reset();      
      this.trigger('reset');
    },
    
    addProduct: function (index, p) {
      var lines = this.get('lines');
      if (index >= 0 && index < lines.length &&
          lines.at(index).get('productid') === p.product.id) {
        // add 1 unit to the current line.
        lines.at(index).addUnit();
      } else {
        // a new line with 1 unit
        lines.add(new OBPOS.Model.OrderLine({
          productid: p.product.id,
          productidentifier: p.product._identifier,
          qty: 1,
          price: p.price.listPrice
        }));
      }
    }    
  });
  
  // Sales.Order Model.
  OBPOS.Model.Stack = Backbone.Model.extend({
    initialize : function () {
      this.set('selected', -1);
    },
    
    setModel: function (order) {
      order.get('lines').on('reset', function () {
        this.set('selected', -1);
      }, this);      
    }
  });

