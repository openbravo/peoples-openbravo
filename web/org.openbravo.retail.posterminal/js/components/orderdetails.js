/*global B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.OrderDetails = Backbone.View.extend({
      tagName: 'div',
      attributes: {'style': 'float:left; padding: 15px 15px 5px 10px; font-weight: bold; color: #6CB33F;'},
      initialize: function () {

        this.receipt =  this.options.modelorder;
        this.receipt.on('clear change:orderDate change:documentNo', function () {
          this.$el.text(OB.I18N.formatHour(this.receipt.get('orderDate')) + ' - ' + this.receipt.get('documentNo'));
        }, this);
      }
  });
}());