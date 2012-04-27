/*global define,_*/

define(['utilities', 'datasource'], function () {
  
  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};
  
  OB.DATA.Order = function (context) {
    this._id = 'DataOrder';
    this.context = context;
    
    this.receipt = context.modelorder;
    
    this.receipt.on('closed', function () {
      
      
      this.proc.exec({
        order: this.receipt.toJSON()
      }, function (data) {
        console.log(JSON.stringify(data));
      });
      
    }, this);
    
    this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessOrder');
    
  };

  OB.DATA.Order.prototype.load = function () {
  };
  
 });