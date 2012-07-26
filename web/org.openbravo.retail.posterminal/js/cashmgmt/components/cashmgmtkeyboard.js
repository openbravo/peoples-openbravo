/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B,$,_,Backbone */

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.CashMgmtKeyboard = OB.COMP.Keyboard.extend({
    _id: 'cashmgmtkeyboard',
    initialize: function() {
      var buttons = [];
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
      
      OB.COMP.Keyboard.prototype.initialize.call(this); // super.initialize();
      
      this.options.parent.model.getData('DataCashMgmtPaymentMethod').each(function(paymentMethod) {
    	var payment = paymentMethod.get('payment');
        if (paymentMethod.get('allowdeposits')) {
          buttons.push({
            command: payment.searchKey + '_' + OB.I18N.getLabel('OBPOS_LblDeposit'),
            definition: getPayment(payment.id, payment.searchKey, payment._identifier, payment._identifier, 'deposit'),
            label: payment._identifier + ' ' + OB.I18N.getLabel('OBPOS_LblDeposit')
          });
        }
        
        if (paymentMethod.get('allowdrops')) {
            buttons.push({
              command: payment.searchKey + '_' + OB.I18N.getLabel('OBPOS_LblWithdrawal'),
              definition: getPayment(payment.id, payment.searchKey, payment._identifier, payment._identifier, 'drop'),
              label: payment._identifier + ' ' + OB.I18N.getLabel('OBPOS_LblWithdrawal')
            });
          }
      });

      //this.addToolbar('toolbarcashmgmt', new OB.UI.ToolbarCashMgmt(this.options).toolbar);
      this.addToolbar('toolbarcashmgmt', buttons);
      this.show('toolbarcashmgmt');
    }
  });
}());