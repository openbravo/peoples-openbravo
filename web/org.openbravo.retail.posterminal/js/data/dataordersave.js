/*global B,_*/

(function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.OrderSave = function (context) {
    this._id = 'logicOrderSave';
    this.context = context;

    this.receipt = context.modelorder;

    this.receipt.on('closed', function () {
      var me = this,
      docno = this.receipt.get('documentNo'),
      json = this.receipt.serializeToJSON(),
      receiptCopy = this.receipt.clone();
      receiptCopy.set('hasbeenpaid', 'Y');
      receiptCopy.set('isbeingprocessed', 'Y');
      OB.Dal.save(receiptCopy, function(){
        delete json.json;
        me.proc.exec({
          order: json
        }, function (data, message) {
          if (data && data.exception) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgReceiptNotSaved', [docno]));
            //If there is an error while sending the order to the backend,
            //we set the order as not being processed so that it can be sent again
            receiptCopy.set('isbeingprocessed', 'N');
            OB.Dal.save(receiptCopy, null, null);
          } else {  
            //In this case, the order was sent correctly, so we remove it from the buffer
            OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgReceiptSaved', [docno]));
            OB.Dal.remove(receiptCopy, null,null);
          }
        });
      }, function(){
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgReceiptNotSaved', [docno]));
      });
    }, this);
    this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessOrder');
  };

 }());