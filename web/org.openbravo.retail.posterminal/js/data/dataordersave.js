/*global define,_*/

define(['utilities', 'utilities', 'i18n', 'datasource'], function () {
  
  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};
  
  OB.DATA.OrderSave = function (context) {
    this._id = 'logicOrderSave';
    this.context = context;
    
    this.receipt = context.modelorder;
    
    this.receipt.on('closed', function () {
      
      var docno = this.receipt.get('documentNo');
      this.proc.exec({
        order: this.receipt.serializeToJSON()
      }, function (data, message) {
        OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgReceiptSaved', [docno]));
//        console.log(JSON.stringify(data));
//        console.log(JSON.stringify(message));
      });
      
    }, this);
    
    this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessOrder');
  };

 });