/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
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
  sideBarEnabled: true,
  init: function (model) {
    this.model = model;
    var me = this;
    this.inherited(arguments);
    this.showSidepad('sidecashup');
    this.disableCommandKey(this, {
      disabled: true,
      commands: ['%']
    });
    this.addCommand('-', {
      stateless: true,
      action: function (keyboard, txt) {
        var t = keyboard.$.editbox.getContent();
        keyboard.$.editbox.setContent(t + '-');
      }
    });
    this.addToolbar({
      name: 'toolbarempty',
      buttons: []
    });
    this.addToolbar({
      name: 'toolbarother',
      buttons: [{
        command: 'allowvariableamount',
        definition: {
          holdActive: true,
          action: function (keyboard, amt) {
            me.model.set('otherInput', OB.I18N.parseNumber(amt));
          }
        },
        label: OB.I18N.getLabel('OBPOS_LblOther')
      }]
    });

    // CashPayments step.
    this.addToolbar({
      name: 'toolbarcashpayments',
      buttons: [{
        command: 'cashpayments',
        i18nLabel: 'OBPOS_SetQuantity',
        stateless: true,
        definition: {
          stateless: true,
          action: function (keyboard, amt) {
            keyboard.model.trigger('action:SelectCoin', {
              keyboard: keyboard,
              txt: amt
            });
          }
        }
      }, {
        command: 'resetallcoins',
        i18nLabel: 'OBPOS_ResetAllCoins',
        stateless: true,
        definition: {
          stateless: true,
          action: function (keyboard, amt) {
            keyboard.model.trigger('action:resetAllCoins');
          }
        }
      }, {
        command: 'keepfixedamount',
        stateless: true,
        definition: {
          stateless: true,
          action: function () {}
        }
      }, {
        command: 'allowdontmove',
        stateless: true,
        definition: {
          stateless: true,
          action: function () {}
        }
      }, {
        command: 'allowmoveeverything',
        stateless: true,
        definition: {
          stateless: true,
          action: function () {}
        }
      }, {
        command: 'opendrawer',
        i18nLabel: 'OBPOS_OpenDrawer',
        stateless: true,
        definition: {
          stateless: true,
          action: function (keyboard, amt) {
            OB.UTIL.Approval.requestApproval(
            me.model, 'OBPOS_approval.opendrawer.cashup', function (approved, supervisor, approvalType) {
              if (approved) {
                OB.POS.hwserver.openCheckDrawer(false, OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerCount);
              }
            });
          }
        }
      }]
    });

    this.addCommand('coin', {
      action: function (keyboard, txt) {
        keyboard.model.trigger('action:SelectCoin', {
          keyboard: keyboard,
          txt: txt
        });
      }
    });
    this.model.on('action:SetStatusCoin', function () {
      this.setStatus('coin');
    }, this);
    this.model.on('action:ResetStatusCoin', function () {
      this.setStatus('');
    }, this);

    this.showToolbar('toolbarempty');

    this.model.get('paymentList').on('reset', function () {
      var buttons = [];
      this.model.get('paymentList').each(function (payment) {
        if (!payment.get('paymentMethod').iscash || !payment.get('paymentMethod').countcash) {
          buttons.push({
            command: payment.get('_id'),
            definition: {
              action: function (keyboard, amt) {
                var convAmt = OB.I18N.parseNumber(amt);
                if (_.isNaN(convAmt)) {
                  OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidNumber', [amt]));
                  return;
                }
                if (payment.get('paymentMethod').iscash && convAmt < 0) {
                  OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_CashUpNegativeAmtForCashPayment', [amt]));
                  return;
                }
                payment.set('foreignCounted', OB.DEC.add(0, convAmt));
                payment.set('counted', OB.DEC.mul(convAmt, payment.get('rate')));
              }
            },
            label: payment.get('name')
          });
        }
      }, this);
      if (this.model.get('paymentList').length !== 0) {
        this.addToolbar({
          name: 'toolbarcountcash',
          buttons: buttons
        });
      }
    }, this);
  }

});