/*global define */

define(['utilities', 'arithmetic', 'i18n'], function () {
  
  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};

  OB.MODEL.OrderTaxes = function(context) {
    this._id = 'logicOrderTaxes';
      
    this.receipt =  context.modelorder;
    this.taxrate = context.DataTaxRate;
    
    this.receipt.on('change:bp change:bploc change:net', function() { // any event that triggers recalculation of taxes...
      // Recalculate taxes for this.receipt based on this.taxrate
      
      // Add calculated taxes to this.receipt.
      
    }, this);    
  };
});