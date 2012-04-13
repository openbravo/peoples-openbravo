/*global define */

define(['builder', 'utilities', 'i18n', 'model/order'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Payment = function (context) {
    var me = this;
    
    this.receipt = context.modelorder;
    var payments = this.receipt.get('payments');
    var lines = this.receipt.get('lines');
    
    lines.on('reset change add remove', function() {
      this.updatePending();     
    }, this);    
    payments.on('reset change add remove', function() {
      this.updatePending();     
    }, this);      
    
    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #363636; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px;'}, content: [
            {kind: B.KindJQuery('span'), id: 'totalpending', attr: {style: 'font-size: 150%; font-weight:bold;'}},
            OB.I18N.getLabel('OBPOS_PaymentsRemaining'),
            {kind: B.KindJQuery('button'), attr: {'style': 'float:right;'}, content: [
              'OK'                       
            ], init: function () {
                 this.$.click(function () {
                   me.receipt.trigger('closed');    
                   me.receipt.clear();
                 });
              }
            }            
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                             
              {kind: B.KindJQuery('a'), attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-orange' }, content: [
                OB.I18N.formatCurrency(50)
              ], init: function () {
                this.$.click(function(e) {
                  e.preventDefault();
                  me.receipt.addPayment(new OB.MODEL.PaymentLine({'kind': 'cash', 'amount': 50}));                 
                });
              }},
              {kind: B.KindJQuery('a'), attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-orange' }, content: [
                OB.I18N.formatCurrency(20)
              ], init: function () {
                this.$.click(function(e) {
                  e.preventDefault();
                  me.receipt.addPayment(new OB.MODEL.PaymentLine({'kind': 'cash', 'amount': 20}));             
                });
              }}               
            ]}
          ]},           
          {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; height: 150px; width: 300px;'}, content: [                                                                                      
            {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px'}, content: [   
              {kind: B.KindJQuery('div'), attr: {'style': 'margin: 5px; border-bottom: 1px solid #cccccc;'}, content: [   
              ]},       
              {kind: OB.COMP.TableView, attr: {
                collection: payments,
                renderEmpty: function () {
                  return B(
                    {kind: B.KindJQuery('div')}
                  );         
                },
                renderLine: function (model) {
                  return B(
                    {kind: B.KindJQuery('div'), attr: {'href': '#', 'class': 'btnselect', 'style': 'color:white; '}, content: [
                      {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 40%'}, content: [ 
                        model.printKind()                                                                
                      ]},                                                                                      
                      {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 40%; text-align:right;'}, content: [ 
                        model.printAmount()                                                                                                                                                     
                      ]},                                                                                      
                      {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
                        {kind: B.KindJQuery('a'), attr: {'href': '#'}, content: [ 
                          {kind: B.KindJQuery('i'), attr: {'class': 'icon-remove icon-white'}}
                        ], init: function () {
                          this.$.click(function(e) {
                            e.preventDefault();
                            me.receipt.removePayment(model);                 
                          });
                        }}                                                         
                      ]},
                      {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
                    ]}
                  );         
                }          
              }}
            ]}        
          ]}            
        ]}        
      ]}
    );
    this.$ = this.component.$;
    this.totalpending = this.component.context.totalpending.$;  
    
  };
  
  OB.COMP.Payment.prototype.updatePending = function () {
    this.totalpending.text(this.receipt.printPending());  
  };
  
});