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
  
  // Sales.Payment Model
  OB.MODEL.PaymentLine = Backbone.Model.extend({
    defaults : {
      'amount': 0,
      'paid': 0 // amount - change...      
    },
    printKind: function () {
      return OB.I18N.getLabel('OBPOS_PayKind:' + this.get('kind'));
    },     
    printAmount: function () {
      return OB.I18N.formatCurrency(this.get('amount'));
    }
  });       
  
  // Sales.OrderLineCol Model.  
  OB.MODEL.PaymentLineCol = Backbone.Collection.extend({
    model: OB.MODEL.PaymentLine
  });
  
  // Sales.Order Model.
  OB.MODEL.Order = Backbone.Model.extend({
    constructor: function (context) {
      this._id = 'modelorder';    
      Backbone.Model.prototype.constructor.call(this);
    },    
    initialize : function () {
      this.set('date', new Date());
      this.set('undo', null);
      this.set('bp', null);
      this.set('lines', new OB.MODEL.OrderLineCol());
      this.set('payments', new OB.MODEL.PaymentLineCol());       
      this.set('payment', 0);
      this.set('change', 0);
    },
    
    getTotal: function () {
      return this.getNet();
    },  
    
    printTotal: function () {
      return OB.I18N.formatCurrency(this.getTotal());      
    },    
    
    getNet: function () {
      return this.get('lines').reduce(function (memo, e) { 
        return memo + e.getNet(); 
      }, 0 );
    },
    
    printNet: function () {
      return OB.I18N.formatCurrency(this.getNet());      
    },
    
    getPayment: function () {
      return this.get('payment');  
    }, 
    
    getChange: function () {
      return this.get('change');  
    },     
    
    getPending: function () {
      return this.getTotal() - this.getPayment();    
    },
    
    getPaymentStatus: function () {
      var total = this.getTotal();
      var pay = this.getPayment();
      return {
        'done': (total > 0 && pay >= total),
        'total': OB.I18N.formatCurrency(total),
        'pending': pay >= total ? OB.I18N.formatCurrency(0) : OB.I18N.formatCurrency(total - pay),
        'change': this.getChange() > 0 ? OB.I18N.formatCurrency(this.getChange()) : null,
        'overpayment': pay > total ? OB.I18N.formatCurrency(pay - total) : null
      };               
    },
    
    clear: function() {
      this.set('date', new Date());
      this.set('undo', null);
      this.set('bp', null);
      this.get('lines').reset();      
      this.get('payments').reset();  
      this.set('payment', 0);
      this.set('change', 0);
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
      this.get('payments').reset();
      _order.get('payments').forEach(function (elem) {
        this.get('payments').add(elem);
      }, this);      
      this.set('payment', _order.get('payment'));
      this.set('change', _order.get('change'));
      this.trigger('change');
      this.trigger('clear'); 
    },
    
    removeUnit: function (line, qty) {
      if (typeof(qty) !== 'number' || isNaN(qty)) {
        qty = 1;
      }
      this.setUnit(line, line.get('qty') - qty, 'rem');
      this.adjustPayment();
    },
    
    addUnit: function (line, qty) {
      if (typeof(qty) !== 'number' || isNaN(qty)) {
        qty = 1;
      }
      this.setUnit(line, line.get('qty') + qty, 'add');
      this.adjustPayment();
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
        this.adjustPayment();
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
      this.adjustPayment();
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
      this.adjustPayment();
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
    },
    
    adjustPayment: function () {   
      var i, max, p;
      var payments = this.get('payments');   
      var total = this.getTotal();
          
      var nocash = 0;
      var cash = 0;
      var pcash;
      
      for (i = 0, max = payments.length; i < max; i++) {  
        p = payments.at(i);    
        p.set('paid', p.get('amount'));
        if (p.get('kind') === 'payment.cash') {   
          cash = cash + p.get('amount');
          pcash = p;
        } else {
          nocash = nocash + p.get('amount');
        }
      }
      
      // Calculation of the change....
      if (pcash) {
        if (nocash > total) {
          pcash.set('paid', 0);
          this.set('payment', nocash);     
          this.set('change', cash);           
        } else if (nocash + cash > total) {
          pcash.set('paid', total - nocash);
          this.set('payment', total);     
          this.set('change', nocash + cash - total);           
        } else {
          pcash.set('paid', cash);
          this.set('payment', nocash + cash);     
          this.set('change', 0);             
        }
      } else {
        this.set('payment', nocash);     
        this.set('change', 0);          
      }
    },
    
    addPayment: function(payment) {
      var i, max, p;
      
      if (typeof(payment.get('amount')) !== 'number' || !isFinite(payment.get('amount')) || this.getPending() <= 0) {
        return;
      }
     
      var payments = this.get('payments');
   
      for (i = 0, max = payments.length; i < max; i++) {
        p = payments.at(i);
        if (p.get('kind') === payment.get('kind')) {
          p.set('amount', payment.get('amount') + p.get('amount'));
          this.adjustPayment();
          return;
        }
      }      
      payments.add(payment);      
      this.adjustPayment();
    },
    
    removePayment: function(payment) {
      var payments = this.get('payments');
      payments.remove(payment);
      this.adjustPayment();     
    }    
  }); 
  
  OB.MODEL.OrderList = Backbone.Collection.extend({
    model: OB.MODEL.Order,
    
    constructor: function (context) {
      this._id = 'modelorderlist';
      this.modelorder = context.modelorder;
     
      Backbone.Collection.prototype.constructor.call(this);
    }, 
    initialize : function () {
      this.current = null;
    },    
    
    createNew: function () {
      this.saveCurrent();
      this.current = new OB.MODEL.Order();
      this.add(this.current);
      this.loadCurrent();     
    },  
    
    deleteCurrent: function () {
      if (this.current) {
        this.remove(this.current);
        if (this.length > 0) {
          this.current = this.at(this.length - 1);
        } else {
          this.current = new OB.MODEL.Order();
          this.add(this.current);
        }
        this.loadCurrent();
      }
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
