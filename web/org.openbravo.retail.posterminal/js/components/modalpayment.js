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
      this.paymentcomponent.show(receipt, key, name, amount);       
      this.$el.modal();
    }
  });

}());