/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, $, _,  enyo */

// Numeric keyboard with buttons for each payment method accepting drops/deposits
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.CashMgmtKeyboard',
  kind: 'OB.UI.Keyboard',
  events: {
    onShowPopup: ''
  },
  getPayment: function (id, key, name, identifier, type, rate, isocode) {
    var me = this;
    return {
      permission: key,
      action: function (keyboard, txt) {
        txt = OB.I18N.parseNumber(txt);
        keyboard.owner.owner.owner.currentPayment = {
          id: id,
          amount: txt,
          identifier: identifier,
          destinationKey: key,
          type: type,
          rate: rate,
          isocode: isocode
        };

        if (type === 'drop') {
          me.doShowPopup({
            popup: 'modaldropevents'
          });
        } else {
          me.doShowPopup({
            popup: 'modaldepositevents'
          });
        }
      }
    };
  },

  init: function () {
    var buttons = [];
    this.inherited(arguments);
    _.bind(this.getPayment, this);


    this.owner.owner.owner.model.getData('DataCashMgmtPaymentMethod').each(function (paymentMethod) {
      var payment = paymentMethod.get('payment');
      if (paymentMethod.get('allowdeposits')) {
        buttons.push({
          command: payment.searchKey + '_' + OB.I18N.getLabel('OBPOS_LblDeposit'),
          definition: this.getPayment(payment.id, payment.searchKey, payment._identifier, payment._identifier, 'deposit', paymentMethod.get('rate'), paymentMethod.get('isocode')),
          label: payment._identifier + ' ' + OB.I18N.getLabel('OBPOS_LblDeposit')
        });
      }

      if (paymentMethod.get('allowdrops')) {
        buttons.push({
          command: payment.searchKey + '_' + OB.I18N.getLabel('OBPOS_LblWithdrawal'),
          definition: this.getPayment(payment.id, payment.searchKey, payment._identifier, payment._identifier, 'drop', paymentMethod.get('rate'), paymentMethod.get('isocode')),
          label: payment._identifier + ' ' + OB.I18N.getLabel('OBPOS_LblWithdrawal')
        });
      }
    }, this);

    this.addToolbar({
      name: 'cashmgmt',
      buttons: buttons
    });
  }
});