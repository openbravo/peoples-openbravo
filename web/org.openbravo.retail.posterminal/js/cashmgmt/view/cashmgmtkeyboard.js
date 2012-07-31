/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

// Numeric keyboard with buttons for each payment method accepting drops/deposits

OB.OBPOSCasgMgmt.UI.CashMgmtKeyboard = OB.COMP.Keyboard.extend({
  _id: 'cashmgmtkeyboard',
  getPayment: function(id, key, name, identifier, type) {
    return {
      permission: key,
      action: function(txt) {
        this.options.parent.options.currentPayment = {
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
  
  initialize: function() {
    var buttons = [];

    _.bind(this.getPayment, this)

    OB.COMP.Keyboard.prototype.initialize.call(this); // super.initialize();
    this.options.parent.model.getData('DataCashMgmtPaymentMethod').each(function(paymentMethod) {
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

    this.addToolbar('toolbarcashmgmt', buttons);
    this.show('toolbarcashmgmt');
  }
});