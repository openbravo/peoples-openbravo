define(['utilities',  'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Order list
  OB.COMP.ReceiptsCounter = function (context) {
  
    var me = this;

    
    this.renderTitle = function (receipt) {
      return OB.UTIL.EL(
        {tag: 'strong', content: [                                                                                        
          OB.I18N.formatHour(receipt.get('date')) + ' - <9332> ', receipt.get('bp') ? receipt.get('bp').get('_identifier') : ''
        ]}  
      );
    };    
    
    this.counter = OB.UTIL.EL({tag: 'span'});      
    this.$ = this.counter;
    
    this.receiptlist = context.get('modelorderlist'); 
    this.receiptlist.on('reset add remove', function () {
      if (this.receiptlist.length > 1) {
        this.$.parent().show();
        this.counter.text('\\ ' + (this.receiptlist.length - 1));
      } else {
        this.$.parent().hide();
        this.counter.html('&nbsp;');
      }
      
    }, this);
  }
  
  OB.COMP.ReceiptsCounter.prototype.attr = function (attr, value) {
  };
  OB.COMP.ReceiptsCounter.prototype.append = function append(child) {
  }; 
  
});    