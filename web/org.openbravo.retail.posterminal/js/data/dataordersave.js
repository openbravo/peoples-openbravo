/*global B,_*/

(function () {

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
        if (data && data.exception) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgReceiptNotSaved', [docno]));
        } else {  
          OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgReceiptSaved', [docno]));
        }
      });

    }, this);

    this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessOrder');
  };

 }());