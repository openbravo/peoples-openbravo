(function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Payment = function (context) {
    var me = this;

    
    this.$ = OB.UTIL.EL(
      {tag: 'div', content: [
        {tag: 'div', attr: {'style': 'background-color: #363636; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
          {tag: 'div', attr: {'style': 'padding: 10px;'}, content: [
            'Remaining',
            {tag: 'button', attr: {'style': 'float:right;'}, content: [
              'OK'                       
            ], init: function () {
                 this.click(function () {
                   me.receipt.trigger('closed');    
                   me.receipt.reset();
                 });
              }
            }            
          ]}                    
        ]}        
      ]}
    );
       
    // Set Model 
    this.receipt = context.get('modelorder');
  };
  
  OB.COMP.Payment.prototype.attr = function (attr, value) {
  };
  OB.COMP.Payment.prototype.append = function append(child) {
  }; 
}());