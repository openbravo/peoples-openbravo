/*global define */

define(['utilities', 'arithmetic', 'i18n'], function () {
  
  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.OrderDiscount = function(context) {
    this._id = 'logicOrderDiscounts';
      
    this.receipt =  context.modelorder;
    
    this.receipt.on('discount', function(line, percentage) { 
    
      if (line && OB.DEC.compare(percentage, OB.DEC.Zero) > 0) {
        this.receipt.setPrice(line, OB.DEC.div(
            OB.DEC.mul(line.get('price'), OB.DEC.sub(OB.DEC.number(100), percentage)), 
            OB.DEC.number(100)));
      }
    }, this);
  };
});