/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.MockPayment = Backbone.View.extend({
    tagName: 'div',
    initialize: function () {
      
      var me = this;
      
      this.info = B(                
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ OB.I18N.getLabel('OBPOS_LblModalType') ]},
            {kind: B.KindJQuery('div'), id: 'paymenttype', attr: {'class': 'span6',  style:'font-weight: bold;'}}          
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ OB.I18N.getLabel('OBPOS_LblModalAmount') ]},
            {kind: B.KindJQuery('div'), id: 'paymentamount', attr: {'class': 'span6', style:'font-weight: bold;'}}          
          ]}
        ]}      
      );
      this.$paymenttype = this.info.context.paymenttype.$el;
      this.$paymentamount = this.info.context.paymentamount.$el;
      this.$el.append(this.info.$el);      
      
      var okbutton = OB.COMP.Button.extend({
        render: function() {
          this.$el.addClass('btnlink');
          this.$el.css('float', 'right');
          this.$el.text('OK');
          return this;
        },
        clickEvent: function (e) {
          me.receipt.addPayment(new OB.Model.PaymentLine(
            {
              'kind':me.key,
              'name': me.name,
              'amount': me.amount
            }));
          me.$el.parents('.modal').filter(':first').modal('hide'); // If in a modal dialog, close it
        }        
      });
      this.$el.append(new okbutton().render().$el);
    },
    show: function (receipt, key, name, amount) {
      
      this.$paymenttype.text(name);
      this.$paymentamount.text(OB.I18N.formatCurrency(amount));
      
      this.receipt = receipt;
      this.key = key;
      this.name = name;
      this.amount = amount;
    }
  });  
      
  // register
  OB.POS.paymentProviders.MockPayment = OB.COMP.MockPayment;
}());