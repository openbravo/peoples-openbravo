/*global B, define, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var getPayment = function (receipt, key, name, type) {
    return ({
      'permission': key,
      'action': function (txt) {
      this.options.amountToDrop=txt;
      this.options.destinationKey=key;
      this.options.type=type;
         $('#modaldropdepdestinations').modal('show');
      }
    });
  };

  OB.COMP.ToolbarCashMgmt = function (context) {
    var i, max, payments;

    this.toolbar = [];
    this.receipt = context.modelorder;
    payments = OB.POS.modelterminal.get('payments');

    for (i = 0, max = payments.length; i < max; i++) {
      if(payments[i].glitemChanges){//if it has a GLItem to Deposits and Drops
        this.toolbar.push({command: payments[i].searchKey+'_'+OB.I18N.getLabel('OBPOS_LblDeposit'), definition: getPayment(this.receipt, payments[i].searchKey, payments[i]._identifier,'deposit'), label: payments[i]._identifier+' '+OB.I18N.getLabel('OBPOS_LblDeposit')});
        this.toolbar.push({command: payments[i].searchKey+'_'+OB.I18N.getLabel('OBPOS_LblDrop'), definition: getPayment(this.receipt, payments[i].searchKey, payments[i]._identifier,'drop'), label: payments[i]._identifier+' '+OB.I18N.getLabel('OBPOS_LblDrop')});
      }
    }
  };
}());

