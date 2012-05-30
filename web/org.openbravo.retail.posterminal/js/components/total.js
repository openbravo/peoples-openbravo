/*global define */

define(['builder', 'utilities', 'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.Total = function (context) {

    this.component = B(
      {kind: B.KindJQuery('span'), content: [ 
        {kind: B.KindJQuery('strong'), id: 'totalgross'}
      ]}
    );
    this.$el = this.component.$el;
    this.totalgross = this.component.context.totalgross.$el;
    
    // Set Model
    this.receipt =  context.modelorder;
    
    this.receipt.on('change:gross', function() {
      this.totalgross.text(this.receipt.printTotal());      
    }, this);
    
    // Initial total display
    this.totalgross.text(this.receipt.printTotal());      
  };
});    