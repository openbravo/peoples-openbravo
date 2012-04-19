/*global define */

define(['builder', 'utilities', 'i18n', 'model/order'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Payment = function (context) {
    var me = this;
    
    this.dataorder = context.DataOrder;
    this.modelorderlist = context.modelorderlist;
    
    this.receipt = context.modelorder;
    var payments = this.receipt.get('payments');
    var lines = this.receipt.get('lines');
    
    lines.on('reset change add remove', function() {
      this.updatePending();     
    }, this);    
    this.receipt.on('change:payment change:change', function() {
      this.updatePending();     
    }, this);      
    
    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #363636; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [                                                                                                                                                    
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px;height: 30px;'}, content: [
                {kind: B.KindJQuery('span'), id: 'totalpending', attr: {style: 'font-size: 175%; font-weight:bold;'}},
                {kind: B.KindJQuery('span'), id: 'totalpendinglbl', content: [OB.I18N.getLabel('OBPOS_PaymentsRemaining')]},
                {kind: B.KindJQuery('span'), id: 'change', attr: {style: 'font-size: 175%; font-weight:bold;'}},
                {kind: B.KindJQuery('span'), id: 'changelbl', content: [OB.I18N.getLabel('OBPOS_PaymentsChange')]},
                {kind: B.KindJQuery('span'), id: 'overpayment', attr: {style: 'font-size: 175%; font-weight:bold;'}},
                {kind: B.KindJQuery('span'), id: 'overpaymentlbl', content: [OB.I18N.getLabel('OBPOS_PaymentsOverpayment')]},
                {kind: B.KindJQuery('a'), id: 'doneaction', attr: { 'href': '#', 'class': 'btnlink btnlink-small', 'style': 'float:right;'}, content: [
                  'Done'                       
                ], init: function () {
                     this.$.click(function (e) {
                       e.preventDefault();
                       me.dataorder.exec(me.receipt);
                       me.modelorderlist.deleteCurrent();
                     });
                  }
                }    
                 ]}
               ]}
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
              {kind: B.KindJQuery('div'), attr: {style: 'overflow:auto; width: 100%;'}, content: [                                                                                      
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
                        {kind: B.KindJQuery('div'), attr: {'class': 'btnselect', 'style': 'color:white; '}, content: [
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
            ]},
            {kind: B.KindJQuery('div'), id: 'coins', attr: {'class': 'span6'}, content: [                                                                                          
            ]}
          ]}                      
        ]}        
      ]}
    );
    this.$ = this.component.$;
    this.totalpending = this.component.context.totalpending.$;  
    this.totalpendinglbl = this.component.context.totalpendinglbl.$;  
    this.change = this.component.context.change.$;  
    this.changelbl = this.component.context.changelbl.$;  
    this.overpayment = this.component.context.overpayment.$;  
    this.overpaymentlbl = this.component.context.overpaymentlbl.$;     
    this.doneaction = this.component.context.doneaction.$;
  };
  
  OB.COMP.Payment.prototype.attr = function (attrs) {
    var i, max;
    var me = this;
    
    var addCoinButton = function (c, v) {
      c.append(B(
        {kind: B.KindJQuery('a'), attr: { 'href': '#', 'class': 'btnlink btnlink-small btnlink-orange' }, content: [
          OB.I18N.formatCurrency(v)
        ], init: function () {
          this.$.click(function(e) {
            e.preventDefault();
            me.receipt.addPayment(new OB.MODEL.PaymentLine({'kind': 'payment.cash', 'amount': v}));                 
          });
        }}
      ).$);      
    };
       
    if (attrs.cashcoins) {
      var coinscontainer = this.component.context.coins.$;
      for (i = 0, max = attrs.cashcoins.length; i < max; i++) {
        addCoinButton(coinscontainer, attrs.cashcoins[i]);
      }
    }
  };
  
  OB.COMP.Payment.prototype.updatePending = function () {
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
      this.doneaction.show();
    } else {
      this.doneaction.hide();
    }
  };
  
});