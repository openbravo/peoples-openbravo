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
    this.$ = this.component.$;
    this.totalgross = this.component.context.get('totalgross').$;
    
    // Set Model
    this.receipt =  context.get('modelorder');
    var lines = this.receipt.get('lines');
    
    lines.on('reset change add remove', function() {
      this.totalgross.text(this.receipt.printNet());      
    }, this);
  };
});    