/*global define,_*/

define(['utilities', 'utilities', 'i18n', 'datasource'], function () {
  
  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};
  
  OB.DATA.Order = function (context) {
    this._id = 'DataOrder';
    this.context = context;
    
    this.receipt = context.modelorder;
    
    this.receipt.on('closed', function () {
      
      var docno = this.receipt.get('documentNo');
      this.proc.exec({
        order: this.receipt.toJSON()
      }, function (data, message) {
        OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgReceiptSaved', [docno]));
//        console.log(JSON.stringify(data));
//        console.log(JSON.stringify(message));
      });
      
    }, this);
    
    this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessOrder');
    
  };

  OB.DATA.Order.prototype.load = function () {
  };
  
 });