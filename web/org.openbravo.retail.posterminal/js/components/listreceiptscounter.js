/*global define */

define(['builder', 'utilities',  'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.ReceiptsCounter = function (context) {

    this.component = B(
        {kind: B.KindJQuery('span')}
    );      
    this.$ = this.component.$;
    
    this.receiptlist = context.modelorderlist; 
    this.receiptlist.on('reset add remove', function () {
      if (this.receiptlist.length > 1) {
        this.$.parent().show();
        this.$.text('\\ ' + (this.receiptlist.length - 1));
      } else {
        this.$.parent().hide();
        this.$.html('&nbsp;');
      }
      
    }, this);
  };
});    