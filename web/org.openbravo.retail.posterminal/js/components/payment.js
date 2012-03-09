(function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Payment = function (context) {
    var me = this;

    this.$ = $("<div/>").load('comp_payment.html', function () {
     $('#btnclose').click(function () {
       me.receipt.trigger('closed');
    
       me.receipt.reset();
     });
    });
    
    // Set Model 
    this.receipt = context.get('modelorder');
    
    this.receipt.get('lines').on('reset change add remove', function() {
      $('#totalpay').text(this.receipt.printNet());
    }, this);    
  };
  
  OB.COMP.Payment.prototype.attr = function (attr, value) {
  };
  OB.COMP.Payment.prototype.append = function append(child) {
  }; 
}());