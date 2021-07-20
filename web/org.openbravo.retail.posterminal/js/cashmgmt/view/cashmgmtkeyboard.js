/*
 ************************************************************************************
 * Copyright (C) 2012-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

// Numeric keyboard with buttons for each payment method accepting drops/deposits
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.CashMgmtKeyboard',
  kind: 'OB.UI.Keyboard',
  classes: 'obObposcashmgmtUiCashMgmtKeyboard',
  events: {
    onShowPopup: ''
  },
  processesToListen: ['cashMngPaymentDone'],
  processFinished: function(process, execution, processesInExec) {
    if (processesInExec.models.length === 0) {
      if (execution.get('hasPendigOp')) {
        var commands = _.map(this.buttons, function(b) {
          return b.command;
        });
        this.waterfall('onDisableButton', {
          disabled: true,
          commands: commands
        });
      }
    }
  },
  getPayment: function(
    id,
    key,
    iscash,
    allowopendrawer,
    name,
    identifier,
    type,
    rate,
    isocode,
    glItem,
    paymentMethod
  ) {
    var me = this;
    return {
      permission: key,
      action: function(keyboard, txt) {
        let amt = OB.I18N.parseNumber(txt);
        if (!OB.I18N.isValidNumber(amt)) {
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_NotValidNumber', [txt]));
          return;
        } else if (amt === 0) {
          OB.UTIL.showI18NWarning('OBPOS_NoCashMgmtZero');
          return;
        } else if (amt !== OB.DEC.add(0, amt)) {
          OB.UTIL.showI18NWarning('OBPOS_PrecisionDifferAfterFormat');
          return;
        }
        keyboard.owner.owner.owner.currentPayment = {
          id: id,
          amount: amt,
          identifier: identifier,
          destinationKey: key,
          type: type,
          rate: rate,
          isocode: isocode,
          iscash: iscash,
          allowopendrawer: allowopendrawer,
          glItem: glItem,
          paymentMethod: paymentMethod,
          defaultProcess: 'Y',
          extendedType: ''
        };
        // Restore CashMgmtDropDepositEvents
        me.owner.owner.owner.model.attributes.cashMgmtDropEvents.reset(
          OB.MobileApp.model.attributes.cashMgmtDropEvents
        );
        me.owner.owner.owner.model.attributes.cashMgmtDepositEvents.reset(
          OB.MobileApp.model.attributes.cashMgmtDepositEvents
        );
        var backupDropEvents = new Backbone.Collection();
        var backupDepositEvents = new Backbone.Collection();

        if (type === 'drop') {
          me.owner.owner.owner.model.attributes.cashMgmtDropEvents.each(
            function(cmevent) {
              backupDropEvents.add(cmevent);
            }
          );
          backupDropEvents.each(function(cmevent) {
            if (
              cmevent.attributes.paymentmethod !== paymentMethod ||
              cmevent.get('isocode') !== isocode
            ) {
              me.owner.owner.owner.model.attributes.cashMgmtDropEvents.remove(
                cmevent
              );
            }
          });
          me.doShowPopup({
            popup: 'modaldropevents'
          });
        } else {
          me.owner.owner.owner.model.attributes.cashMgmtDepositEvents.each(
            function(cmevent) {
              backupDepositEvents.add(cmevent);
            }
          );
          backupDepositEvents.each(function(cmevent) {
            if (
              cmevent.attributes.paymentmethod !== paymentMethod ||
              cmevent.get('isocode') !== isocode
            ) {
              me.owner.owner.owner.model.attributes.cashMgmtDepositEvents.remove(
                cmevent
              );
            }
          });
          me.doShowPopup({
            popup: 'modaldepositevents'
          });
        }
      }
    };
  },

  init: function() {
    this.buttons = [];
    this.inherited(arguments);
    this.buttons.push({
      command: 'opendrawer',
      i18nLabel: 'OBPOS_OpenDrawer',
      stateless: true,
      definition: {
        stateless: true,
        action: function(keyboard, amt) {
          OB.POS.hwserver.openCheckDrawer(
            false,
            OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerCount
          );
        }
      }
    });
    _.bind(this.getPayment, this);
    _.each(
      OB.MobileApp.model.get('payments'),
      function(paymentMethod) {
        if (
          OB.POS.modelterminal.get('terminal').isslave &&
          paymentMethod.paymentMethod.isshared
        ) {
          return true;
        }
        if (
          paymentMethod.paymentMethod.issafebox &&
          OB.UTIL.isNullOrUndefined(
            OB.UTIL.localStorage.getItem('currentSafeBox')
          )
        ) {
          return true;
        }
        var payment = paymentMethod.payment,
          i;
        if (paymentMethod.paymentMethod.allowdeposits) {
          for (
            i = 0;
            i < OB.MobileApp.model.get('cashMgmtDepositEvents').length;
            i++
          ) {
            if (
              OB.MobileApp.model.get('cashMgmtDepositEvents')[i].isocode ===
                paymentMethod.isocode &&
              paymentMethod.paymentMethod.paymentMethod ===
                OB.MobileApp.model.get('cashMgmtDepositEvents')[i].paymentmethod
            ) {
              this.buttons.push({
                idSufix: 'Deposit.' + paymentMethod.isocode,
                command:
                  payment.searchKey +
                  '_' +
                  OB.I18N.getLabel('OBPOS_LblDeposit'),
                definition: this.getPayment(
                  payment.id,
                  payment.searchKey,
                  paymentMethod.paymentMethod.iscash,
                  paymentMethod.paymentMethod.allowopendrawer,
                  payment._identifier,
                  payment._identifier,
                  'deposit',
                  paymentMethod.rate,
                  paymentMethod.isocode,
                  paymentMethod.paymentMethod.gLItemForDeposits,
                  paymentMethod.paymentMethod.paymentMethod
                ),
                label:
                  payment._identifier +
                  ' ' +
                  OB.I18N.getLabel('OBPOS_LblDeposit')
              });
              break;
            }
          }
        }

        if (paymentMethod.paymentMethod.allowdrops) {
          for (
            i = 0;
            i < OB.MobileApp.model.get('cashMgmtDropEvents').length;
            i++
          ) {
            if (
              OB.MobileApp.model.get('cashMgmtDropEvents')[i].isocode ===
                paymentMethod.isocode &&
              paymentMethod.paymentMethod.paymentMethod ===
                OB.MobileApp.model.get('cashMgmtDropEvents')[i].paymentmethod
            ) {
              this.buttons.push({
                idSufix: 'Withdrawal.' + paymentMethod.isocode,
                command:
                  payment.searchKey +
                  '_' +
                  OB.I18N.getLabel('OBPOS_LblWithdrawal'),
                definition: this.getPayment(
                  payment.id,
                  payment.searchKey,
                  paymentMethod.paymentMethod.iscash,
                  paymentMethod.paymentMethod.allowopendrawer,
                  payment._identifier,
                  payment._identifier,
                  'drop',
                  paymentMethod.rate,
                  paymentMethod.isocode,
                  paymentMethod.paymentMethod.gLItemForDrops,
                  paymentMethod.paymentMethod.paymentMethod
                ),
                label:
                  payment._identifier +
                  ' ' +
                  OB.I18N.getLabel('OBPOS_LblWithdrawal')
              });
              break;
            }
          }
        }
      },
      this
    );

    OB.UTIL.HookManager.executeHooks(
      'OBPOS_AddButtonToCashManagement',
      {
        context: this,
        buttons: []
      },
      function(args) {
        _.each(args.buttons, function(btn) {
          args.context.buttons.push(btn);
        });
        args.context.addToolbar({
          name: 'cashMgmtToolbar',
          buttons: args.context.buttons
        });
        args.context.showToolbar('cashMgmtToolbar');
      }
    );
  },
  initComponents: function() {
    this.inherited(arguments);
    this.keyMatcher = new RegExp(
      '^([0-9]|\\' + OB.Format.defaultDecimalSymbol + ')$',
      'g'
    );
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
  },
  destroyComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  }
});
