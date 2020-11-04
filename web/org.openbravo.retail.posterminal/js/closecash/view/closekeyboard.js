/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.CloseCashKeyboard',
  classes: 'obObposCloseCashUiCloseCashKeyboard',
  published: {
    payments: null
  },
  kind: 'OB.UI.Keyboard',
  sideBarEnabled: true,
  init: function(model) {
    this.model = model;
    this.inherited(arguments);
    this.showSidepad('sidecashup');
    this.disableCommandKey(this, {
      disabled: true,
      commands: ['%']
    });
    this.addToolbar({
      name: 'toolbarempty',
      buttons: []
    });
    this.addToolbar({
      name: 'toolbarother',
      buttons: [
        {
          command: 'allowvariableamount',
          definition: {
            holdActive: true,
            action: (keyboard, amt) => {
              this.model.set('otherInput', OB.I18N.parseNumber(amt));
            }
          },
          label: OB.I18N.getLabel('OBPOS_LblOther')
        }
      ]
    });

    // CashPayments step.
    this.addToolbar({
      name: 'toolbarcashpayments',
      buttons: [
        {
          command: 'cashpayments',
          i18nLabel: 'OBPOS_SetQuantity',
          stateless: true,
          definition: {
            stateless: true,
            action: (keyboard, amt) => {
              keyboard.model.trigger('action:SelectCoin', {
                keyboard: keyboard,
                txt: amt
              });
            }
          }
        },
        {
          command: 'resetallcoins',
          i18nLabel: 'OBPOS_ResetAllCoins',
          stateless: true,
          definition: {
            stateless: true,
            action: (keyboard, amt) => {
              keyboard.model.trigger('action:resetAllCoins');
            }
          }
        },
        {
          command: 'opendrawer',
          i18nLabel: 'OBPOS_OpenDrawer',
          stateless: true,
          definition: {
            stateless: true,
            action: (keyboard, amt) => {
              OB.UTIL.Approval.requestApproval(
                this.model,
                'OBPOS_approval.opendrawer.cashup',
                (approved, supervisor, approvalType) => {
                  if (approved) {
                    OB.POS.hwserver.openCheckDrawer(
                      false,
                      OB.MobileApp.model.get('permissions')
                        .OBPOS_timeAllowedDrawerCount
                    );
                  }
                }
              );
            }
          }
        }
      ]
    });

    this.addCommand('coin', {
      action: (keyboard, txt) => {
        keyboard.model.trigger('action:SelectCoin', {
          keyboard: keyboard,
          txt: txt
        });
      }
    });
    this.model.on('action:SetStatusCoin', () => {
      this.setStatus('coin');
    });
    this.model.on('action:ResetStatusCoin', () => {
      this.setStatus('');
    });

    this.showToolbar('toolbarempty');

    this.model.get('paymentList').on('reset', () => {
      let buttons = [];
      this.model.get('paymentList').forEach(payment => {
        if (
          !payment.get('paymentMethod').iscash ||
          !payment.get('paymentMethod').countcash
        ) {
          buttons.push({
            command: payment.get('_id'),
            definition: {
              action: (keyboard, amt) => {
                let txtVal = amt;
                if (_.last(amt) === '%') {
                  txtVal = amt.substring(0, amt.length - 1);
                }
                if (isNaN(txtVal)) {
                  OB.UTIL.showWarning(
                    OB.I18N.getLabel('OBPOS_NotValidNumber', [amt])
                  );
                  return;
                }
                const convAmt = OB.I18N.parseNumber(amt);
                payment.set('foreignCounted', OB.DEC.add(0, convAmt));
                payment.set(
                  'counted',
                  OB.DEC.mul(convAmt, payment.get('rate'))
                );
              }
            },
            label: payment.get('name')
          });
        }
      });
      if (this.model.get('paymentList').length !== 0) {
        this.addToolbar({
          name: 'toolbarcountcash',
          buttons: buttons
        });
      }
    });
  },
  initComponents: function() {
    this.addCommand('-', {
      stateless: true,
      action: (keyboard, txt) => {
        const t = keyboard.$.editbox.getContent();
        keyboard.$.editbox.setContent(t + '-');
      }
    });
    this.inherited(arguments);
    this.keyMatcher = new RegExp(
      '^([0-9]|\\' + OB.Format.defaultDecimalSymbol + ')$',
      'g'
    );
  }
});
