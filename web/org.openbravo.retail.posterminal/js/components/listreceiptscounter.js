/*global define */

define(['builder', 'utilities',  'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.ReceiptsCounter = function (context) {

    this.component = B(
        {kind: B.KindJQuery('span')}
    );      
    this.$el = this.component.$el;
    
    this.receiptlist = context.modelorderlist; 
    this.receiptlist.on('reset add remove', function () {
      if (this.receiptlist.length > 1) {
        this.$el.parent().show();
        this.$el.text('\\ ' + (this.receiptlist.length - 1));
      } else {
        this.$el.parent().hide();
        this.$el.html('&nbsp;');
      }
      
    }, this);
  };
});    