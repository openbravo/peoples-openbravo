/*global define */

define(['utilities', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.Total = function (context) {
  
    var me = this;
    
    this.totalgross = OB.UTIL.EL({tag:'strong'});  
    
    this.$ = OB.UTIL.EL(
      {tag: 'span', content: [ 
        this.totalgross
      ]}         
    );
    
    // Set Model
    this.receipt =  context.get('modelorder');
    var lines = this.receipt.get('lines');
    
    lines.on('reset change add remove', function() {
      this.totalgross.text(this.receipt.printNet());      
    }, this);
  };
});    