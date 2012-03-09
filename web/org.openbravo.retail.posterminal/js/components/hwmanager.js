define(['utilities', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.HWManager = function (context) {
    this.hw = context.get('hwserver');
    this.receipt = context.get('modelorder');
    this.stack = context.get('stackorder');
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
    
    this.receipt.on('closed print', this.printOrder, this);        
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
      this.hw.print(this.templateline, {line: this.line});
    }   
  };
  
  OB.COMP.HWManager.prototype.printOrder = function () {
    
    this.hw.print(this.templatereceipt, { order: this.receipt});    
  }
  
  OB.COMP.HWManager.prototype.attrs = function (attrs) {    
    this.templateline = attrs.templateline;
    this.templatereceipt = attrs.templatereceipt;
  };
});