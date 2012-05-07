/*global define */

define(['builder', 'utilities',  'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.BusinessPartner = function (context) {
  
    var me = this;
    this.context = context;
    
    this.renderTitle = function (receipt) {
      return B(
        {kind: B.KindJQuery('strong'), content: [                                                                                        
          receipt.get('bp') ? receipt.get('bp').get('_identifier') : ''
        ]}            
      );
    };    
    
    this.bp = B({kind: B.KindJQuery('span')});      
    this.$ = this.bp.$;
    
    this.receipt =  context.modelorder;   
    this.receipt.on('clear change:bp change:bploc', function () {
      this.bp.$.empty().append(this.renderTitle(this.receipt).$);
    }, this);
  };  
});    