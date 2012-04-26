/*global define */

define(['utilities', 'utilitiesui', 'model/order', 'model/terminal'], function () {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};
  
  OB.COMP.HWManager = function (context) {
    this.receipt = context.modelorder;
    this.line = null;
    
    this.receipt.get('lines').on('selected', function (line) {
      if (this.line) {
        this.line.off('change', this.printLine);
      }    
      this.line = line;
      if (this.line) {
        this.line.on('change', this.printLine, this);     
      }      
      this.printLine();
    }, this);   
    
    this.receipt.on('closed print', this.printOrder, this);        
  };
  
  var hwcallback = function (e) {
    if (e.exception) {
      OB.UTIL.showError(e.exception.message);
    }
  };
  
  OB.COMP.HWManager.prototype.printLine = function () {
    if (this.line) {
      OB.POS.hwserver.print(this.templateline, {line: this.line}, hwcallback);
    }   
  };
  
  OB.COMP.HWManager.prototype.printOrder = function () {
    OB.POS.hwserver.print(this.templatereceipt, { order: this.receipt}, hwcallback);    
  };
  
  OB.COMP.HWManager.prototype.attr = function (attrs) {    
    this.templateline = attrs.templateline;
    this.templatereceipt = attrs.templatereceipt;
  };
});