/*global Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.BusinessPartner = OB.COMP.SmallButton.extend({
      className: 'btnlink btnlink-small btnlink-gray',
      attributes: {'href': '#modalcustomer', 'data-toggle': 'modal'},
      initialize: function () {
        OB.COMP.SmallButton.prototype.initialize.call(this); // super.initialize();
        this.receipt = this.options.modelorder;
        this.receipt.on('clear change:bp change:bploc', function () {
          this.$el.text(this.receipt.get('bp') ? this.receipt.get('bp').get('_identifier') : '');
        }, this);
      }
  });
}());
