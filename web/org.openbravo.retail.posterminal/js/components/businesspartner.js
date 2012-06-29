/*global B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.BusinessPartner = Backbone.View.extend({
      tagName: 'button',
      className: 'btnlink btnlink-small btnlink-gray',
      attributes: {'href': '#modalcustomer', 'data-toggle': 'modal'},
      initialize: function () {

        this.receipt =  this.options.modelorder;
        this.receipt.on('clear change:bp change:bploc', function () {
          this.$el.text(this.receipt.get('bp') ? this.receipt.get('bp').get('_identifier') : '');
        }, this);
      }
  });
}());
