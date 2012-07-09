/*global B, define, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  var getPayment = function (receipt, key, name, identifier, type) {
    return ({
      'permission': key,
      'action': function (txt) {
      this.options.amountToDrop=txt;
      this.options.destinationKey=key;
      this.options.identifier=identifier;
      this.options.type=type;
      if(type==='drop'){
        $('#modaldropevents').modal('show');
      }else{
        $('#modaldepositevents').modal('show');
      }
      }
    });
  };

  OB.COMP.ToolbarCashMgmt = function (context) {
    var i, max, payments;
    var ctx = context;
    this.toolbar = [];
    var me = this;
    this.receipt = context.modelorder;
    this.payments = new OB.Model.Collection(context.DataCashMgmtPaymentMethod);
    context.DataCashMgmtPaymentMethod.ds.on('ready', function(){
      me.payments.reset(this.cache);
      for (i = 0, max = me.payments.length; i < max; i++) {
        if(me.payments.at(i).get('allowdeposits')){
          me.toolbar.push({command: me.payments.at(i).get('payment').searchKey+'_'+OB.I18N.getLabel('OBPOS_LblDeposit'), definition: getPayment(this.receipt, me.payments.at(i).get('payment').searchKey, me.payments.at(i).get('payment')._identifier, me.payments.at(i).get('payment')._identifier, 'deposit'), label: me.payments.at(i).get('payment')._identifier+' '+OB.I18N.getLabel('OBPOS_LblDeposit')});
        }
        if(me.payments.at(i).get('allowdrops')){
          me.toolbar.push({command: me.payments.at(i).get('payment').searchKey+'_'+OB.I18N.getLabel('OBPOS_LblDrop'), definition: getPayment(this.receipt, me.payments.at(i).get('payment').searchKey, me.payments.at(i).get('payment')._identifier, me.payments.at(i).get('payment')._identifier, 'drop'), label: me.payments.at(i).get('payment')._identifier+' '+OB.I18N.getLabel('OBPOS_LblDrop')});
        }
      }
      ctx.cashmgmtkeyboard.addToolbar('toolbarcashmgmt', me.toolbar);
      ctx.cashmgmtkeyboard.show('toolbarcashmgmt');
    });
    this.payments.exec();
  };
}());

