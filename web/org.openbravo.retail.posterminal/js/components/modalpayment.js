/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ModalPayment = OB.COMP.Modal.extend({

    header: OB.I18N.getLabel('OBPOS_LblModalPayment'),
    maxheight: '600px',
    getContentView: function () {
      return Backbone.View.extend({tagName: 'div'});
    },
    show: function (receipt, key, name, providerview, amount) {
      
      this.paymentcomponent = new providerview().render();      
      this.contentview.$el.empty().append(this.paymentcomponent.$el);
//        
//      this.contentview.$paymenttype.text(name);
//      this.contentview.$paymentamount.text(OB.I18N.formatCurrency(amount));
      this.paymentcomponent.show(receipt, key, name, amount);       
      this.$el.modal();
    }
  });
  
  OB.COMP.ContentPayment = Backbone.View.extend({
    tagName: 'div',
    initialize: function () {
      this.info = B(                
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ OB.I18N.getLabel('OBPOS_LblModalType') ]},
            {kind: B.KindJQuery('div'), id: 'paymenttype', attr: {'class': 'span6',  style:'font-weight: bold;'}}          
          ]},
          {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
            {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [ OB.I18N.getLabel('OBPOS_LblModalAmount') ]},
            {kind: B.KindJQuery('div'), id: 'paymentamount', attr: {'class': 'span6', style:'font-weight: bold;'}}          
          ]},
          {kind: B.KindJQuery('div'), id: 'paymentcontainer'}
        ]}      
      );
      this.$paymenttype = this.info.context.paymenttype.$el;
      this.$paymentamount = this.info.context.paymentamount.$el;
      this.$paymentcontainer = this.info.context.paymentcontainer.$el;
      this.$el.append(this.info.$el);
    }
  });  

}());