/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashUpKeyboard',
  published: {
    payments: null
  },
  kind: 'OB.UI.Keyboard',

  init: function () {
    this.inherited(arguments);

    this.addToolbar({
      name: 'toolbarempty',
      buttons: []
    });

    this.showToolbar('toolbarempty');
  },

  paymentsChanged: function () {
    var buttons = [];
    this.payments.each(function (payment) {
      buttons.push({
        command: payment.get('_id'),
        definition: {
          action: function (keyboard, amt) {
            payment.set('foreignCounted', OB.DEC.add(0, amt));
            payment.set('counted', OB.DEC.mul(amt, payment.get('rate')));
          }
        },
        label: payment.get('name')
      });
    }, this);
    this.addToolbar({
      name: 'toolbarcountcash',
      buttons: buttons
    });
  }

});