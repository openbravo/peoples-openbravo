(function (OBPOS) {
  
  OBPOS.Sales.HWManager = function (hw) {
    this.hw = hw;
  };
  
  OBPOS.Sales.HWManager.prototype.setModel = function (receipt, stack) {
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
  
  OBPOS.Sales.HWManager.prototype.editLine = function (line) {
    if (this.line) {
      this.line.off('change', this.printLine);
    }
    
    this.line = line;
    
    if (this.line) {
      this.line.on('change', this.printLine, this);     
    }
    
    this.printLine();    
  };  
  
  OBPOS.Sales.HWManager.prototype.printLine = function () {
    if (this.line) {
      this.hw.print('res/printline.xml', {line: this.line});
    }   
  };
  
  OBPOS.Sales.HWManager.prototype.printOrder = function () {
    
    this.hw.print('res/printreceipt.xml', { order: this.receipt});    
  }
  
}(window.OBPOS)); 