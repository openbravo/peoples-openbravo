/*global define */

define(['utilities',  'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.BusinessPartner = function (context) {
  
    var me = this;
    this.context = context;
    
    this.renderTitle = function (receipt) {
      return OB.UTIL.EL(
        {tag: 'strong', content: [                                                                                        
          OB.I18N.formatHour(receipt.get('date')) + ' - <9332> ', receipt.get('bp') ? receipt.get('bp').get('_identifier') : ''
        ]}  
      );
    };    
    
    this.bp = OB.UTIL.EL({tag: 'span'});      
    this.$ = this.bp;
    
    this.receipt =  context.get('modelorder');   
    this.receipt.on('clear change:bp', function () {
      this.bp.empty().append(this.renderTitle(this.receipt));
    }, this);
  };
  
  OB.COMP.BusinessPartner.prototype.attr = function (attr, value) {
  };
  OB.COMP.BusinessPartner.prototype.append = function append(child) {
  }; 
  
});    