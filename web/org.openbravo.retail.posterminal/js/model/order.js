/*global define,Backbone */

define(['utilities', 'i18n'], function () {
  
  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};
  
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
      return OB.I18N.formatCurrency(this.get('price'));
    },
    
    getNet: function () {
      return this.get('price') * this.get('qty');
    },
    
    printNet: function () {
      return OB.I18N.formatCurrency(this.getNet());
    }     
  });
  
  // Sales.OrderLineCol Model.  
  OB.MODEL.OrderLineCol = Backbone.Collection.extend({
    model: OB.MODEL.OrderLine
  });

  // Sales.Order Model.
  OB.MODEL._Order = Backbone.Model.extend({
    initialize : function () {
      this.set('date', new Date());
      this.set('undo', null);
      this.set('bp', null);
      this.set('lines', new OB.MODEL.OrderLineCol());
    },
    
    getNet: function () {
      return this.get('lines').reduce(function (memo, e) { 
        return memo + e.getNet(); 
      }, 0 );
    },
    
    printNet: function () {
      return OB.I18N.formatCurrency(this.getNet());      
    },
    
    clear: function() {
      this.set('date', new Date());
      this.set('undo', null);
      this.set('bp', null);
      this.get('lines').reset();      
      this.trigger('change');      
      this.trigger('clear');
    },
    
    clearWith: function(_order) {
      this.set('date', _order.get('date'));
      this.set('undo', null);
      this.set('bp', _order.get('bp'));
      this.get('lines').reset();
      _order.get('lines').forEach(function (elem) {
        this.get('lines').add(elem);
      }, this);
      this.trigger('change');
      this.trigger('clear');
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
      var index = this.get('lines').indexOf(line);
      // remove the line
      this.get('lines').remove(line);
      // set the undo action
      this.set('undo', {
        action: 'deleteline',
        line: line,
        undo: function() {
          me.get('lines').add(line, {at: index});
          me.set('undo', null);
        }
      });         
    },
    
    addProduct: function (line, p) {
      var me = this;
      if (line && line.get('productid') === p.get('product').id) {
        this.addUnit(line);
      } else {
        // a new line with 1 unit
        var newline = new OB.MODEL.OrderLine({
          productid: p.get('product').id,
          productidentifier: p.get('product')._identifier,
          qty: 1,
          price: p.get('price').listPrice
        });
        // add the created line
        this.get('lines').add(newline);
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
    },
    
    setBP: function (bp) {
      var me = this;
      var oldbp = this.get('bp');
      this.set('bp', bp);
      // set the undo action
      this.set('undo', {
        action: bp ? 'setbp' : 'resetbp',
        bp: bp,
        undo: function() {
          me.set('bp', oldbp);
          me.set('undo', null);
        }
      });    
    }    
    
    
  }); 
  
  OB.MODEL.Order =  OB.UTIL.recontext(OB.MODEL._Order, 'modelorder');
  
  OB.MODEL.OrderList = Backbone.Collection.extend({
    model: OB.MODEL._Order,
    
    constructor: function (context, id) {
      this.context = context;
      context.set(id || 'modelorderlist', this);

      
      Backbone.Collection.prototype.constructor.call(this);
    }, 
    initialize : function () {
      this.current = null;
    },    
    attr: function (attr, value) {
    },
    append: function append(child) {
      this.modelorder = child;
    },
    
    createNew: function () {
      this.saveCurrent();
      this.current = new OB.MODEL._Order();
      this.add(this.current);
      this.loadCurrent();     
    },  
    load: function(model) {
      this.saveCurrent();
      this.current = model;
      this.loadCurrent();
    },
    saveCurrent: function () {
      if (this.current) {
        this.current.clearWith(this.modelorder);
      }
    },
    loadCurrent: function () {
      if (this.current) {
        this.modelorder.clearWith(this.current);
      }
    }
    
  }); 

});
