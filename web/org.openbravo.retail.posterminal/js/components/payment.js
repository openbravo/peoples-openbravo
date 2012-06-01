/*global define */

define(['builder', 'utilities', 'arithmetic', 'i18n', 'components/commonbuttons', 'model/order'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Payment = OB.COMP.CustomView.extend({
    paymentButtons : [],
    initialize : function () {
      var i, max;
      var me = this;
  
      this.modelorderlist = this.options.modelorderlist;   
      this.receipt = this.options.modelorder;
      var payments = this.receipt.get('payments');
      var lines = this.receipt.get('lines');
      
      this.receipt.on('change:payment change:change change:gross', function() {
        this.updatePending();     
      }, this);      
      
      this.component = B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #363636; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                                                                    
  
              ]}
            ]},
            {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
                {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px;height: 30px;'}, content: [
                  {kind: B.KindJQuery('span'), id: 'totalpending', attr: {style: 'font-size: 175%; font-weight:bold;'}},
                  {kind: B.KindJQuery('span'), id: 'totalpendinglbl', content: [OB.I18N.getLabel('OBPOS_PaymentsRemaining')]},
                  {kind: B.KindJQuery('span'), id: 'change', attr: {style: 'font-size: 175%; font-weight:bold;'}},
                  {kind: B.KindJQuery('span'), id: 'changelbl', content: [OB.I18N.getLabel('OBPOS_PaymentsChange')]},
                  {kind: B.KindJQuery('span'), id: 'overpayment', attr: {style: 'font-size: 175%; font-weight:bold;'}},
                  {kind: B.KindJQuery('span'), id: 'overpaymentlbl', content: [OB.I18N.getLabel('OBPOS_PaymentsOverpayment')]} 
                ]},                
                {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; width: 100%;'}, content: [                                                                                      
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px'}, content: [   
                    {kind: B.KindJQuery('div'), attr: {'style': 'margin: 5px; border-bottom: 1px solid #cccccc;'}, content: [   
                    ]},       
                    {kind: OB.COMP.TableView, attr: {
                      collection: payments,
                      renderEmpty: function () {
                        return (
                          {kind: B.KindJQuery('div')}
                        );         
                      },
                      renderLine: OB.COMP.SelectPanel.extend({
                        render: function () {
                          var model = this.model;
                          this.$el.append(B(
                            {kind: B.KindJQuery('div'), attr: {'style': 'color:white; '}, content: [
                              {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 40%'}, content: [ 
                                 OB.POS.modelterminal.getPaymentName(this.model.get('kind'))                                          
                              ]},                                                                                      
                              {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 40%; text-align:right;'}, content: [ 
                                this.model.printAmount()                                                                                                                                                     
                              ]},                                                                                      
                              {kind: B.KindJQuery('div'), attr: {style: 'float: left; width: 20%; text-align:right;'}, content: [                                                                                        
                                {kind: B.KindJQuery('a'), attr: {'href': '#'}, content: [ 
                                  {kind: B.KindJQuery('i'), attr: {'class': 'icon-remove icon-white'}}
                                ], init: function () {
                                  this.$el.click(function(e) {
                                    e.preventDefault();
                                    me.receipt.removePayment(model);                 
                                  });
                                }}                                                         
                              ]},
                              {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}}                                                                                     
                            ]}                            
                          ).$el);
                          return this;
                        }
                      })         
                    }}
                  ]}        
                ]}                                                                                          
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [    
                {kind: B.KindJQuery('div'), id: 'coinscontainer', content: [
                ]},
                {kind: B.KindJQuery('div'), id: 'doneaction', content: [
                      {kind: B.KindJQuery('a'), attr: { 'href': '#', 'class': 'btnlink', 'style': 'font-size: 150%; font-weight: bold; float: right;'}, content: [
                        OB.I18N.getLabel('OBPOS_LblDone')                 
                      ], init: function () {
                           this.$el.click(function (e) {
                             e.preventDefault();
                             me.receipt.trigger('taxes', me.modelorderlist);
                           });
                        }
                      }                   
                ]}
              ]}
            ]}                      
          ]}        
        ]}
      );
      this.setElement(this.component.$el);
      this.totalpending = this.component.context.totalpending.$el;  
      this.totalpendinglbl = this.component.context.totalpendinglbl.$el;  
      this.change = this.component.context.change.$el;  
      this.changelbl = this.component.context.changelbl.$el;  
      this.overpayment = this.component.context.overpayment.$el;  
      this.overpaymentlbl = this.component.context.overpaymentlbl.$el;     
      this.doneaction = this.component.context.doneaction.$el;
      this.coinscontainer = this.component.context.coinscontainer.$el;    
      this.updatePending();
      
      for (i = 0, max = this.paymentButtons.length; i < max; i++) {
        this.addButton(this.paymentButtons[i]);
      }      
    },
    addButton : function (btn) {
      var btninst = new btn(this.options);
      if (btninst.render) {
        btninst = btninst.render();
      }
      this.coinscontainer.append(btninst.$el);
    },
    updatePending : function () {
      var paymentstatus = this.receipt.getPaymentStatus();
      this.totalpending.text(paymentstatus.pending);  
      if (paymentstatus.change) {
        this.change.text(paymentstatus.change);  
        this.change.show();
        this.changelbl.show();
      } else {
        this.change.hide();
        this.changelbl.hide();      
      }
      if (paymentstatus.overpayment) {
        this.overpayment.text(paymentstatus.overpayment);  
        this.overpayment.show();
        this.overpaymentlbl.show();
      } else {
        this.overpayment.hide();
        this.overpaymentlbl.hide();      
      } 
      if (paymentstatus.done) {
        this.coinscontainer.hide();
        this.doneaction.show();
      } else {
        this.doneaction.hide();
        this.coinscontainer.show();
      }
    }    
    
  });
  

  
//  OB.COMP.Payment.prototype.attr = function (attrs) {
//    var i, max;
//    var me = this;
//    
//    var addCoinButton = function (v) { 
//      
//      var btn = new (OB.COMP.PaymentButton.extend({paymenttype: v.paymenttype, amount: v.amount, classcolor: v.classcolor, label: v.label }))(me.options);
//      me.coinscontainer.append(btn.render().$el);;  
//    };       
//    if (attrs.cashcoins) {
//      for (i = 0, max = attrs.cashcoins.length; i < max; i++) {
//        addCoinButton(attrs.cashcoins[i]);
//      }
//    }
//  };
  

  
});