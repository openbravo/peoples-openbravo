/*global window, define, Backbone */

define(['builder', 'utilities', 'utilitiesui', 'i18n', 'components/commonbuttons', 'components/total', 'components/paymentcoins'], function (B) {
   
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.ButtonTabPayment = OB.COMP.ButtonTab.extend({

    initialize: function () {
      // I am not calling to super. because I am overwriting the content
      this.$el.attr('href', '#payment');
      this.$el.append(B(
        {kind: B.KindJQuery('div'), attr: {'style': 'text-align: right; width:100px;'}, content: [
          {kind: B.KindJQuery('span'), attr: {'style': 'font-weight: bold'}, content: [
            {kind: OB.COMP.Total}
          ]},                    
          {kind: B.KindJQuery('span'), content: [
            OB.I18N.getLabel('OBPOS_LblPay')
          ]}
        ]}          
      , this.options).$el);      
    },

    shownEvent: function (e) {
      this.options.keyboard.show('toolbarpayment');
    }       
  }); 
  
  OB.COMP.TabPayment = OB.COMP.CustomView.extend({
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'payment', 'class': 'tab-pane'}, content: [
          {kind: OB.COMP.PaymentCoins}                                                                      
        ]}
      );
    }   
  }); 
  
});  