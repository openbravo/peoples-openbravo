(function (OBPOS) {

  OBPOS.Sales.Payment = function (container) {
    var me = this;
    this.container = container;
    this.closeListeners = [];

   container.load('comp_payment.html', function () {
     $('#btnclose').click(function () {
       me.fireCloseEvent();
     });
    });
  }  
  
  OBPOS.Sales.Payment.prototype.calculate = function (receipt) {
    $('#totalpay').text(receipt.printNet());
  }
  
  OBPOS.Sales.Payment.prototype.addCloseListener = function (l) {
    this.closeListeners.push(l);
  }

  OBPOS.Sales.Payment.prototype.fireCloseEvent = function () {
    for (var i = 0, max = this.closeListeners.length; i < max; i++) {
      this.closeListeners[i]();
    }
  } 
}(window.OBPOS));   