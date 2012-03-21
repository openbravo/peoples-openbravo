

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
    
    removeUnit: function (line, qty) {
      if (typeof(qty) !== 'number' || isNaN(qty)) {
        qty = 1;
      }
      this.setUnit(line, line.get('qty') - qty, 'rem');
    },
    
    addUnit: function (line, qty) {
      if (typeof(qty) !== 'number' || isNaN(qty)) {
        qty = 1;
      }
      this.setUnit(line, line.get('qty') + qty, 'add');
    },
    
    setUnit: function (line, qty, action) {
      
      if (typeof(qty) === 'number' && !isNaN(qty)) {     
        var oldqty = line.get('qty');      
        if (qty > 0) {
          var me = this;
          // sets the new quantity
          line.set('qty', qty);
          // sets the undo action
          this.set('undo', {
            action: action ? action : 'set',
            oldqty: oldqty,
            line: line,
            undo: function () {
              line.set('qty', oldqty);
              me.set('undo', null);
            }
          });        
        } else {
          this.deleteLine(line);     
        }     
      }
    },
    
    deleteLine:function (line) {
      var me = this;
      // remove the line
      this.get('lines').remove(line);
      // set the undo action
      this.set('undo', {
        action: 'deleteline',
        line: line,
        undo: function() {
          me.get('lines').add(line);
          me.set('undo', null);
        }
      });         
    },
    
    addProduct: function (index, p) {
      var me = this;
      var lines = this.get('lines');
      if (index >= 0 && index < lines.length &&
          lines.at(index).get('productid') === p.get('product').id) {
        this.addUnit(lines.at(index));
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
          action: 'addline',
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
