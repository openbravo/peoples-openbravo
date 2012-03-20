define(['utilities', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Scan = function (context) {
    var me = this;
    
    var msgwelcome = OB.UTIL.EL(
        {tag: 'div', attr: {'style': 'margin: 10px; display: none;'}, content: [
          {tag: 'div', attr: {'style': 'float:right;'}, content: [
            'Welcome to Openbravo.'                 
          ]}
        ]}
    );    
    
    var undoclick;
    
    var txtaction = OB.UTIL.EL(
      {tag: 'div', attr: {'style': 'float:left;'}}       
    );
    
    var msgaction = OB.UTIL.EL(
        {tag: 'div', attr: {'style': 'margin: 10px; display: none;'}, content: [
          txtaction,
          {tag: 'button', attr: {'style': 'float:right;'}, content: [
            'Undo'                       
          ], init: function () {
              this.click(function() {
                if (undoclick) {
                  undoclick();
                }
              });
          }}                  
        ]}          
    );
    
    this.$ = OB.UTIL.EL(
      {tag: 'div', content: [
        {tag: 'div', attr: {'style': 'background-color: #7da7d9; color: white; height: 250px; margin: 5px; padding: 5px'}, content: [
          {tag: 'div', content: [
            msgwelcome,
            msgaction
          ]}                    
        ]}        
      ]}
    );
    
    this.receipt = context.get('modelorder');
    
    this.receipt.on('change:undo', function() {
      var undoaction = this.receipt.get('undo');
      if (undoaction) {
        msgwelcome.hide();
        msgaction.show();
        txtaction.text('Added ' + undoaction.line.get('qty') + ' x ' +  undoaction.line.get('productidentifier'));
        undoclick = undoaction.undo;
        
      } else {
        msgaction.hide();
        msgwelcome.show();
      }
    }, this);
    
  };
  
});     