/*global define, Backbone */

define(['builder', 'utilities',  'model/order', 'model/terminal'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.BusinessPartner = Backbone.View.extend({
      tagName: 'a',
      className: 'btnlink btnlink-small btnlink-gray',
      attributes: {'href': '#modalcustomer', 'data-toggle': 'modal', 'style': 'font-size: 110%'},
      initialize: function () {

        this.receipt =  this.options.modelorder;
        this.receipt.on('clear change:bp change:bploc', function () {
          this.$el.text(this.receipt.get('bp') ? this.receipt.get('bp').get('_identifier') : '');
        }, this);
      }
  });
});
