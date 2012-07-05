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
          receiptId = this.receipt.get('id');

      this.receipt.set('hasbeenpaid', 'Y');

      delete this.receipt.attributes.json;
      this.receipt.set('json', JSON.stringify(this.receipt.toJSON()));

      // The order will not be processed if the navigator is offline
      if (navigator.onLine) {
        this.receipt.set('isbeingprocessed', 'Y');
      }

      OB.Dal.save(this.receipt, function () {
        if (navigator.onLine) {
          me.proc.exec({
            order: json
          }, function (data, message) {
            var d = data,
                m = message;
            OB.Dal.get(OB.MODEL.Order, receiptId, function (receipt) {
              if (d && d.exception) {
                OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgReceiptNotSaved', [docno]));
                //If there is an error while sending the order to the backend,
                //we set the order as not being processed so that it can be sent again
                receipt.set('isbeingprocessed', 'N');
                OB.Dal.save(receipt, null, function (tx, err) { OB.UTIL.showError(err);});
              } else {
                //In this case, the order was sent correctly, so we remove it from the buffer
                OB.UTIL.showSuccess(OB.I18N.getLabel('OBPOS_MsgReceiptSaved', [docno]));
                OB.Dal.remove(receipt, null, function (tx, err) { OB.UTIL.showError(err);});
              }
            }, null);
          });
        }
      }, function () {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgReceiptNotSaved', [docno]));
      });
    }, this);
    this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessOrder');
  };
}());