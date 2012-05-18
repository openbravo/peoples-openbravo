/*global define, Backbone */

define(['builder', 'utilities',  'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.OrderDetails = Backbone.View.extend({
      tagName: 'div',
      attributes: {'style': 'float:left; margin:5px; padding: 5px 15px 5px 0px; font-weight: bold; font-size: 110%; color: green;'},     
      initialize: function () {
        
        this.receipt =  this.options.modelorder;   
        this.receipt.on('clear change:orderDate change:documentNo', function () {
          this.$el.text(OB.I18N.formatHour(this.receipt.get('orderDate')) + ' - ' + this.receipt.get('documentNo'));
        }, this);        
      }
  });
});    