/*global define,Backbone */

define(['utilities', 'arithmetic', 'i18n'], function () {
  
  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};
  
  // Sales.OrderLine Model
  OB.MODEL.OrderLine = Backbone.Model.extend({
    defaults : {
      productid: null,
      productidentifier: null,
      qty: OB.DEC.Zero,
      price: OB.DEC.Zero,
      net: OB.DEC.Zero
    },
    
    printQty: function () {
      return this.get('qty').toString();
    },
    
    printPrice: function () {
      return OB.I18N.formatCurrency(this.get('price'));
    },
    
    calculateNet: function () {
      this.set('net', OB.DEC.mul(this.get('qty'), this.get('price')));
    },
    
    getNet: function () {
      return this.get('net');
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
      'amount': OB.DEC.Zero,
      'paid': OB.DEC.Zero // amount - change...      
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
      this.set('payment', OB.DEC.Zero);
      this.set('change', OB.DEC.Zero);
      this.set('net', OB.DEC.Zero);
    },
    
    getTotal: function () {
      return this.getNet();
    },  
    
    printTotal: function () {
      return OB.I18N.formatCurrency(this.getTotal());      
    },   
    
    calculateNet: function () {
      var net = this.get('lines').reduce(function (memo, e) { 
        return OB.DEC.add(memo, e.getNet()); 
      }, OB.DEC.Zero );
      this.set('net', net);
    },    
    
    getNet: function () {
      return this.get('net');
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
      return OB.DEC.sub(this.getTotal(), this.getPayment());    
    },
    
    getPaymentStatus: function () {
      var total = this.getTotal();
      var pay = this.getPayment();
      return {
        'done': (OB.DEC.compare(total) > 0 && OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0),
        'total': OB.I18N.formatCurrency(total),
        'pending': OB.DEC.compare(OB.DEC.sub(pay, total)) >= 0 ? OB.I18N.formatCurrency(OB.DEC.Zero) : OB.I18N.formatCurrency(OB.DEC.sub(total, pay)),
        'change': OB.DEC.compare(this.getChange()) > 0 ? OB.I18N.formatCurrency(this.getChange()) : null,
        'overpayment': OB.DEC.compare(OB.DEC.sub(pay, total)) > 0 ? OB.I18N.formatCurrency(OB.DEC.sub(pay, total)) : null
      };               
    },
    
    clear: function() {
      this.set('date', new Date());
      this.set('undo', null);
      this.set('bp', null);
      this.get('lines').reset();      
      this.get('payments').reset();  
      this.set('payment', OB.DEC.Zero);
      this.set('change', OB.DEC.Zero);
      this.set('net', OB.DEC.Zero);      
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
      this.set('net', _order.get('net'));
      this.trigger('change');
      this.trigger('clear'); 
    },
    
    removeUnit: function (line, qty) {
      if (!OB.DEC.isNumber(qty)) {
        qty = OB.DEC.One;
      }
      this.setUnit(line, OB.DEC.sub(line.get('qty'), qty), 'rem');
    },
    
    addUnit: function (line, qty) {
      if (!OB.DEC.isNumber(qty)) {
        qty = OB.DEC.One;
      }
      this.setUnit(line, OB.DEC.add(line.get('qty'), qty), 'add');
    },
    
    setUnit: function (line, qty, action) {
      
      if (OB.DEC.isNumber(qty)) {     
        var oldqty = line.get('qty');      
        if (OB.DEC.compare(qty) > 0) {
          var me = this;
          // sets the new quantity
          line.set('qty', qty);
          line.calculateNet();
          this.calculateNet();
          // sets the undo action
          this.set('undo', {
            action: action ? action : 'set',
            oldqty: oldqty,
            line: line,
            undo: function () {
              line.set('qty', oldqty);
              line.calculateNet();
              me.calculateNet();
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
      this.calculateNet();
      // set the undo action
      this.set('undo', {
        action: 'deleteline',
        line: line,
        undo: function() {
          me.get('lines').add(line, {at: index});
          me.calculateNet();
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
          qty: OB.DEC.One,
          price: OB.DEC.number(p.get('price').listPrice)
        });
        newline.calculateNet();
   
        // add the created line
        this.get('lines').add(newline);
        this.calculateNet();
        // set the undo action
        this.set('undo', {
          action: 'addline',
          line: newline,
          undo: function() {
            me.get('lines').remove(newline);
            this.calculateNet();
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
          
      var nocash = OB.DEC.Zero;
      var cash = OB.DEC.Zero;
      var pcash;
      
      for (i = 0, max = payments.length; i < max; i++) {  
        p = payments.at(i);    
        p.set('paid', p.get('amount'));
        if (p.get('kind') === 'payment.cash') {   
          cash = OB.DEC.add(cash, p.get('amount'));
          pcash = p;
        } else {
          nocash = OB.DEC.add(cash, p.get('amount'));
        }
      }
      
      // Calculation of the change....
      if (pcash) {
        if (OB.DEC.compare(nocash - total) > 0) {
          pcash.set('paid', OB.DEC.Zero);
          this.set('payment', nocash);     
          this.set('change', cash);           
        } else if (OB.DEC.compare(OB.DEC.sub(OB.DEC.add(nocash, cash), total)) > 0) {
          pcash.set('paid', OB.DEC.sub(total, nocash));
          this.set('payment', total);     
          this.set('change', OB.DEC.sub(OB.DEC.add(nocash, cash), total));           
        } else {
          pcash.set('paid', cash);
          this.set('payment', OB.DEC.add(nocash, cash));     
          this.set('change', OB.DEC.Zero);             
        }
      } else {
        this.set('payment', nocash);     
        this.set('change', OB.DEC.Zero);          
      }
    },
    
    addPayment: function(payment) {
      var i, max, p;
      
      if (!OB.DEC.isNumber(payment.get('amount')) || OB.DEC.compare(this.getPending()) <= 0) {
        return;
      }
     
      var payments = this.get('payments');
   
      for (i = 0, max = payments.length; i < max; i++) {
        p = payments.at(i);
        if (p.get('kind') === payment.get('kind')) {
          p.set('amount', OB.DEC.add(payment.get('amount'), p.get('amount')));
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
