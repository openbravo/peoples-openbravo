(function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Payment = function (context) {
    var me = this;
    
    this.totalpending = OB.UTIL.EL({tag:'span', attr: {style: 'font-size: 150%; font-weight:bold;'}});  

    this.paymentsview = new OB.COMP.TableView({
      renderEmpty: function () {
        return function () {
          return OB.UTIL.EL(
            {tag: 'div'}
          );
        };            
      },
  
      renderLine: function (model) {
        return OB.UTIL.EL(
          {tag: 'div', attr: {'href': '#', 'class': 'btnselect', 'style': 'color:white; '}, content: [
            {tag: 'div', attr: {style: 'float: left; width: 40%'}, content: [ 
              model.printKind()                                                                
            ]},                                                                                      
            {tag: 'div', attr: {style: 'float: left; width: 40%; text-align:right;'}, content: [ 
              model.printAmount()                                                                                                                                                     
            ]},                                                                                      
            {tag: 'div', attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
              {tag: 'a', attr: {'href': '#'}, content: [ 
                {tag: 'i', attr: {'class': 'icon-remove icon-white'}}
              ], init: function () {
                this.click(function(e) {
                  e.preventDefault();
                  me.receipt.removePayment(model);                 
                });
              }}                                                         
            ]},
            {tag: 'div', attr: {style: 'clear: both;'}}                                                                                     
          ]}
        );         
      }      
    });    
    
    this.$ = OB.UTIL.EL(
      {tag: 'div', content: [
        {tag: 'div', attr: {'style': 'background-color: #363636; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
          {tag: 'div', attr: {'style': 'padding: 10px;'}, content: [
            this.totalpending, OB.I18N.getLabel('OBPOS_PaymentsRemaining'),
            {tag: 'button', attr: {'style': 'float:right;'}, content: [
              'OK'                       
            ], init: function () {
                 this.click(function () {
                   me.receipt.trigger('closed');    
                   me.receipt.clear();
                 });
              }
            }            
          ]},
          {tag: 'div', attr: {'class': 'row-fluid'}, content: [
            {tag: 'div', attr: {'class': 'span12'}, content: [                                                                             
              {tag: 'a', attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-orange' }, content: [
                OB.I18N.formatCurrency(50)
              ], init: function () {
                this.click(function(e) {
                  e.preventDefault();
                  me.receipt.addPayment(new OB.MODEL.PaymentLine({'kind': 'cash', 'amount': 50}));                 
                });
              }},
              {tag: 'a', attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-orange' }, content: [
                OB.I18N.formatCurrency(20)
              ], init: function () {
                this.click(function(e) {
                  e.preventDefault();
                  me.receipt.addPayment(new OB.MODEL.PaymentLine({'kind': 'cash', 'amount': 20}));             
                });
              }}               
            ]}
          ]},           
          {tag: 'div', attr: {style: 'overflow:auto; height: 150px; width: 300px;'}, content: [                                                                                      
            {tag: 'div', attr: {'style': 'padding: 5px'}, content: [   
              {tag: 'div', attr: {'style': 'margin: 5px; border-bottom: 1px solid #cccccc;'}, content: [   
              ]},       
              this.paymentsview.div
            ]}        
          ]}            
        ]}        
      ]}
    );
       
    // Set Model 
    this.receipt = context.get('modelorder');
    var payments = this.receipt.get('payments');
    var lines = this.receipt.get('lines');
    
    lines.on('reset change add remove', function() {
      this.updatePending();     
    }, this);    
    payments.on('reset change add remove', function() {
      this.updatePending();     
    }, this);      
    
    this.paymentsview.setModel(payments);     
  };
  
  OB.COMP.Payment.prototype.updatePending = function () {
    this.totalpending.text(this.receipt.printPending());  
  };
  
  OB.COMP.Payment.prototype.attr = function (attr, value) {
  };
  OB.COMP.Payment.prototype.append = function append(child) {
  }; 
}());