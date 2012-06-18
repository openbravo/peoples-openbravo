/*global define, Backbone */

define(['builder', 'utilities', 'model/order', 'model/terminal'], function (B) {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.Total = Backbone.View.extend({
    tagName: 'span',
    initialize: function () {

      this.totalgross = $('<strong/>');
      this.$el.append(totalgross);
  
      // Set Model
      this.receipt = this.options.modelorder;
  
      this.receipt.on('change:gross', function() {
        this.totalgross.text(this.receipt.printTotal());
      }, this);
  
      // Initial total display
      this.totalgross.text(this.receipt.printTotal());
    }
  });
});