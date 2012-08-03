/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */


// Numeric keyboard with buttons for each payment method accepting drops/deposits
enyo.kind({
  name: 'OB.OBPOSCasgMgmt.UI.CashMgmtKeyboard',
  kind: 'OB.UI.Keyboard',
  getPayment: function(id, key, name, identifier, type) {
    return {
      permission: key,
      action: function(txt) {
        this.owner.currentPayment = {
          id: id,
          amount: txt,
          identifier: identifier,
          destinationKey: key,
          type: type
        }

        if (type === 'drop') {
          $('#modaldropevents').modal('show');
        } else {
          $('#modaldepositevents').modal('show');
        }
      }
    };
  },

  init: function() {
    var buttons = [];
    this.inherited(arguments);
    _.bind(this.getPayment, this)


    this.owner.model.getData('DataCashMgmtPaymentMethod').each(function(paymentMethod) {
      var payment = paymentMethod.get('payment');
      if (paymentMethod.get('allowdeposits')) {
        buttons.push({
          command: payment.searchKey + '_' + OB.I18N.getLabel('OBPOS_LblDeposit'),
          definition: this.getPayment(payment.id, payment.searchKey, payment._identifier, payment._identifier, 'deposit'),
          label: payment._identifier + ' ' + OB.I18N.getLabel('OBPOS_LblDeposit')
        });
      }

      if (paymentMethod.get('allowdrops')) {
        buttons.push({
          command: payment.searchKey + '_' + OB.I18N.getLabel('OBPOS_LblWithdrawal'),
          definition: this.getPayment(payment.id, payment.searchKey, payment._identifier, payment._identifier, 'drop'),
          label: payment._identifier + ' ' + OB.I18N.getLabel('OBPOS_LblWithdrawal')
        });
      }
    }, this);

    this.addToolbar(buttons);
  }
});