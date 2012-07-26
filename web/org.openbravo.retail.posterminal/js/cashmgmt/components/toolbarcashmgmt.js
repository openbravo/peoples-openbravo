/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, define, $ */

(function() {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

  var getPayment = function(id, key, name, identifier, type) {
      return {
        'permission': key,
        'action': function(txt) {
          this.options.id = id;
          this.options.amountToDrop = txt;
          this.options.destinationKey = key;
          this.options.identifier = identifier;
          this.options.type = type;
          if (type === 'drop') {
            $('#modaldropevents').modal('show');
          } else {
            $('#modaldepositevents').modal('show');
          }
        }
      };
   };

  OB.UI.ToolbarCashMgmt = function(context) {
    var i, max, payments, ctx = context,
        me = this;
    this.toolbar = [];
    this.receipt = context.modelorder;
    debugger;
    this.payments = new OB.Model.Collection(context.DataCashMgmtPaymentMethod);
    context.DataCashMgmtPaymentMethod.ds.on('ready', function() {
      me.payments.reset(this.cache);
      for (i = 0, max = me.payments.length; i < max; i++) {
        if (me.payments.at(i).get('allowdeposits')) {
          me.toolbar.push({
            command: me.payments.at(i).get('payment').searchKey + '_' + OB.I18N.getLabel('OBPOS_LblDeposit'),
            definition: getPayment(this.receipt, 
            		me.payments.at(i).get('payment').id, 
                    me.payments.at(i).get('payment').searchKey, 
                    me.payments.at(i).get('payment')._identifier, 
                    me.payments.at(i).get('payment')._identifier, 
                    'deposit'),
            label: me.payments.at(i).get('payment')._identifier + ' ' + OB.I18N.getLabel('OBPOS_LblDeposit')
          });
        }
        if (me.payments.at(i).get('allowdrops')) {
          me.toolbar.push({
            command: me.payments.at(i).get('payment').searchKey + '_' + OB.I18N.getLabel('OBPOS_LblWithdrawal'),
            definition: getPayment(this.receipt, me.payments.at(i).get('payment').id, me.payments.at(i).get('payment').searchKey, me.payments.at(i).get('payment')._identifier, me.payments.at(i).get('payment')._identifier, 'drop'),
            label: me.payments.at(i).get('payment')._identifier + ' ' + OB.I18N.getLabel('OBPOS_LblWithdrawal')
          });
        }
      }
      ctx.cashmgmtkeyboard.addToolbar('toolbarcashmgmt', me.toolbar);
      ctx.cashmgmtkeyboard.show('toolbarcashmgmt');
    });
    this.payments.exec();
  };
}());