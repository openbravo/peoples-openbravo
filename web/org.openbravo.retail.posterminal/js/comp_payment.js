(function (OBPOS) {

  OBPOS.Sales.Payment = function (container) {
    var me = this;
    this.container = container;
    this.closeListeners = [];

   container.load('comp_payment.html', function () {
     $('#btnclose').click(function () {
       me.receipt.trigger('closed');
    
       me.receipt.reset();
     });
    });
  }  
  
  OBPOS.Sales.Payment.prototype.setModel = function (receipt) {
    this.receipt = receipt;
    
    this.receipt.get('lines').on('reset change add remove', function() {
      $('#totalpay').text(this.receipt.printNet());
    }, this);    
  };

}(window.OBPOS));   