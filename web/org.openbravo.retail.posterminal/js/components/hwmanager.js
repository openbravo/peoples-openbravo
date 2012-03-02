define(['utilities', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.HWManager = function (hw) {
    this.hw = hw;
  };
  
  OB.COMP.HWManager.prototype.setModel = function (receipt, stack) {
    this.receipt = receipt;
    this.stack = stack;
    this.line = null;
    
    this.stack.on('change:selected', function() {
      var index = this.stack.get('selected');
      var lines = this.receipt.get('lines');
      if (index >= 0 && index < lines.length) {  
        this.editLine(lines.at(index));     
      } else {
        this.editLine(null);
      }
    }, this);
    
    this.receipt.on('closed', this.printOrder, this);        
  };
  
  OB.COMP.HWManager.prototype.editLine = function (line) {
    if (this.line) {
      this.line.off('change', this.printLine);
    }
    
    this.line = line;
    
    if (this.line) {
      this.line.on('change', this.printLine, this);     
    }
    
    this.printLine();    
  };  
  
  OB.COMP.HWManager.prototype.printLine = function () {
    if (this.line) {
      this.hw.print('res/printline.xml', {line: this.line});
    }   
  };
  
  OB.COMP.HWManager.prototype.printOrder = function () {
    
    this.hw.print('res/printreceipt.xml', { order: this.receipt});    
  }
  
});