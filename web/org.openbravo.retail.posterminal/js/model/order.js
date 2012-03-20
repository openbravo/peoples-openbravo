

define(['utilities', 'model/stack'], function () {
  
  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};
  
  
  OB.MODEL.StackOrder = OB.UTIL.recontext(OB.MODEL.Stack, 'stackorder');

  
  // Sales.OrderLine Model
  OB.MODEL.OrderLine = Backbone.Model.extend({
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
      return OB.UTIL.formatNumber(this.get('price'), {
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
      return OB.UTIL.formatNumber(this.getNet(), {
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
  OB.MODEL.OrderLineCol = Backbone.Collection.extend({
    model: OB.MODEL.OrderLine
  });

  // Sales.Order Model.
  OB.MODEL._Order = Backbone.Model.extend({
    initialize : function () {
      this.set('lines', new OB.MODEL.OrderLineCol());
    },
    
    getNet: function () {
      return this.get('lines').reduce(function (memo, e) { 
        return memo + e.getNet(); 
      }, 0 );
    },
    
    printNet: function () {
      return OB.UTIL.formatNumber(this.getNet(), {
        decimals: 2,
        decimal: '.',
        group: ',',
        currency: '$#'
      });      
    },
    
    reset: function() {
      this.set('undo', null);
      this.get('lines').reset();      
      this.trigger('reset');
    },
    
    addProduct: function (index, p) {
      var me = this;
      var lines = this.get('lines');
      if (index >= 0 && index < lines.length &&
          lines.at(index).get('productid') === p.get('product').id) {
        var modline = lines.at(index);
        // add 1 unit to the current line.
        modline.addUnit();
        // set the undo action
        this.set('undo', {
          action: 'qty',
          line: modline,
          undo: function() {
            modline.removeUnit();
            if (modline.get('qty') <= 0) {
              me.get('lines').remove(modline);
            }
            me.set('undo', null);
          }
        });        
      } else {
        // a new line with 1 unit
        var newline = new OB.MODEL.OrderLine({
          productid: p.get('product').id,
          productidentifier: p.get('product')._identifier,
          qty: 1,
          price: p.get('price').listPrice
        });
        // add the created line
        lines.add(newline);
        // set the undo action
        this.set('undo', {
          action: 'add',
          line: newline,
          undo: function() {
            me.get('lines').remove(newline);
            me.set('undo', null);
          }
        });
      }
    }    
  }); 

  OB.MODEL.Order =  OB.UTIL.recontext(OB.MODEL._Order, 'modelorder');
});
