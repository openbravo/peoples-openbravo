/*global define */

define(['builder', 'utilities',  'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.OrderDetails = function (context) {
  
    var me = this;
    this.context = context;
    
    this.renderTitle = function (receipt) {
      return B(
        {kind: B.KindJQuery('strong'), content: [                                                                                        
          OB.I18N.formatHour(receipt.get('orderDate')) + ' - ' + receipt.get('documentNo')
        ]}            
      );
    };    
    
    this.details = B({kind: B.KindJQuery('span')});      
    this.$ = this.details.$;
    
    this.receipt =  context.modelorder;   
    this.receipt.on('clear change:orderDate change:documentNo', function () {
      this.details.$.empty().append(this.renderTitle(this.receipt).$);
    }, this);
  };  
});    