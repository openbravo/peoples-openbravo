/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, enyo */

// Numeric keyboard with buttons for each payment method accepting drops/deposits
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.CashMgmtKeyboard',
  kind: 'OB.UI.Keyboard',
  events: {
    onShowPopup: ''
  },
  getPayment: function (id, key, iscash, allowopendrawer, name, identifier, type, rate, isocode, glItem) {
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
          isocode: isocode,
          iscash: iscash,
          allowopendrawer: allowopendrawer,
          glItem: glItem
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
    buttons.push({
      command: 'opendrawer',
      i18nLabel: 'OBPOS_OpenDrawer',
      stateless: true,
      definition: {
        stateless: true,
        action: function (keyboard, amt) {
          OB.POS.hwserver.openCheckDrawer(false, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerCount);
        }
      }
    });
    _.bind(this.getPayment, this);
    _.each(OB.MobileApp.model.get('payments'), function (paymentMethod) {
      var payment = paymentMethod.payment;
      if (paymentMethod.paymentMethod.allowdeposits) {
        buttons.push({
          idSufix: 'Deposit.' + paymentMethod.isocode,
          command: payment.searchKey + '_' + OB.I18N.getLabel('OBPOS_LblDeposit'),
          definition: this.getPayment(payment.id, payment.searchKey, paymentMethod.paymentMethod.iscash, paymentMethod.paymentMethod.allowopendrawer, payment._identifier, payment._identifier, 'deposit', paymentMethod.rate, paymentMethod.isocode, paymentMethod.paymentMethod.gLItemForDeposits),
          label: payment._identifier + ' ' + OB.I18N.getLabel('OBPOS_LblDeposit')
        });
      }

      if (paymentMethod.paymentMethod.allowdrops) {
        buttons.push({
          idSufix: 'Withdrawal.' + paymentMethod.isocode,
          command: payment.searchKey + '_' + OB.I18N.getLabel('OBPOS_LblWithdrawal'),
          definition: this.getPayment(payment.id, payment.searchKey, paymentMethod.paymentMethod.iscash, paymentMethod.paymentMethod.allowopendrawer, payment._identifier, payment._identifier, 'drop', paymentMethod.rate, paymentMethod.isocode, paymentMethod.paymentMethod.gLItemForDrops),
          label: payment._identifier + ' ' + OB.I18N.getLabel('OBPOS_LblWithdrawal')
        });
      }
    }, this);

    this.addToolbar({
      name: 'cashMgmtToolbar',
      buttons: buttons
    });
    this.showToolbar('cashMgmtToolbar');
  }
});