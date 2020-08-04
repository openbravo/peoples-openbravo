/*
 ************************************************************************************
 * Copyright (C) 2016-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone*/

(function() {
  OB.UTIL.CashManagementUtils = {};

  OB.UTIL.CashManagementUtils.addCashManagementTransaction = function(
    newCashManagementTransaction,
    cashup,
    successCallback,
    errorCallback,
    options
  ) {
    //Doc:
    //newCashMangmentTransaction is a backbone model which includes the following fields
    //amount -> the amount to be drop or deposited.
    //cashManagementEvent -> An item from OB.MobileApp.model.get('cashMgmtDropEvents') or OB.MobileApp.model.get('cashMgmtDepEvents')
    //                      including type = drop or deposit
    //paymentMethod -> An item from OB.MobileApp.model.get('payments') which will be used to drop or deposit the amount.

    var warns = [];
    var cashManagementTransactionToAdd = null;
    var optionsObj = {};
    var printCashManagementModel = null;

    //Manage default values
    if (options && !OB.UTIL.isNullOrUndefined(options.printTicket)) {
      optionsObj.printTicket = options.printTicket;
    } else {
      optionsObj.printTicket = true;
    }
    if (options && !OB.UTIL.isNullOrUndefined(options.ticketTemplate)) {
      optionsObj.ticketTemplate = options.ticketTemplate;
    } else {
      optionsObj.ticketTemplate = new OB.OBPOSCashMgmt.Print.CashMgmt();
    }
    if (options && !OB.UTIL.isNullOrUndefined(options.executeSync)) {
      optionsObj.executeSync = options.executeSync;
    } else {
      optionsObj.executeSync = true;
    }

    if (options && !OB.UTIL.isNullOrUndefined(options.customGLItem)) {
      optionsObj.glItem = options.customGLItem;
    } else {
      if (
        newCashManagementTransaction.get('cashManagementEvent').type === 'drop'
      ) {
        optionsObj.glItem = newCashManagementTransaction.get(
          'paymentMethod'
        ).paymentMethod.gLItemForDrops;
      } else if (
        newCashManagementTransaction.get('cashManagementEvent').type ===
        'deposit'
      ) {
        optionsObj.glItem = newCashManagementTransaction.get(
          'paymentMethod'
        ).paymentMethod.gLItemForDeposits;
      } else {
        errorCallback('Error getting the G/L item to create a cash managment');
        return;
      }
    }

    //execute cash management event
    //1st load current cash up
    const cashUp = OB.Dal.transform(OB.Model.CashUp, cashup);

    // Prepare object to Send
    //cashUp.set('objToSend', JSON.stringify(cashUp));

    //validate
    if (
      newCashManagementTransaction.get('cashManagementEvent').type === 'drop' &&
      !newCashManagementTransaction.get('paymentMethod').paymentMethod
        .allowdrops
    ) {
      errorCallback("Current payment method doesn't allow drops");
    } else if (
      newCashManagementTransaction.get('cashManagementEvent').type ===
        'deposit' &&
      !newCashManagementTransaction.get('paymentMethod').paymentMethod
        .allowdeposits
    ) {
      errorCallback("Current payment method doesn't allow deposits");
      return;
    } else if (
      newCashManagementTransaction.get('cashManagementEvent').type !==
        'deposit' &&
      newCashManagementTransaction.get('cashManagementEvent').type !== 'drop'
    ) {
      errorCallback('Cash Managment event type is not valid.');
      return;
    }
    if (
      newCashManagementTransaction.get('cashManagementEvent').type === 'drop'
    ) {
      if (
        OB.DEC.sub(
          OB.App.State.Cashup.Utils.getPaymentMethodCurrentCash(
            cashup.cashPaymentMethodInfo,
            newCashManagementTransaction.get('paymentMethod').payment.id,
            OB.MobileApp.model.paymentnames,
            OB.UTIL.currency.webPOSDefaultCurrencyId(),
            OB.UTIL.currency.conversions
          ).currentCash,
          newCashManagementTransaction.get('amount')
        ) < 0
      ) {
        errorCallback(
          'Amount to drop is greater than available amount (' +
            OB.I18N.formatCurrency(
              OB.App.State.Cashup.Utils.getPaymentMethodCurrentCash(
                cashup.cashPaymentMethodInfo,
                newCashManagementTransaction.get('paymentMethod').payment.id,
                OB.MobileApp.model.paymentnames,
                OB.UTIL.currency.webPOSDefaultCurrencyId(),
                OB.UTIL.currency.conversions
              ).currentCash
            ) +
            ')'
        );
        return;
      }
    }

    var now = new Date();
    cashManagementTransactionToAdd = new OB.Model.CashManagement({
      id: OB.UTIL.get_UUID(),
      description:
        newCashManagementTransaction.get('paymentMethod').payment._identifier +
        ' - ' +
        newCashManagementTransaction.get('cashManagementEvent').name,
      amount: newCashManagementTransaction.get('amount'),
      origAmount: OB.DEC.mul(
        newCashManagementTransaction.get('amount'),
        newCashManagementTransaction.get('paymentMethod').rate
      ),
      type: newCashManagementTransaction.get('cashManagementEvent').type,
      reasonId: newCashManagementTransaction.get('cashManagementEvent').id,
      paymentMethodId: newCashManagementTransaction.get('paymentMethod').payment
        .id,
      user: OB.MobileApp.model.get('context').user._identifier,
      userId: OB.MobileApp.model.get('context').user.id,
      creationDate: OB.I18N.normalizeDate(now),
      timezoneOffset: now.getTimezoneOffset(),
      isocode: newCashManagementTransaction.get('paymentMethod').isocode,
      glItem: optionsObj.glItem,
      cashup_id: cashUp.get('id'),
      posTerminal: OB.MobileApp.model.get('terminal').id,
      isbeingprocessed: 'N',
      cashUpReportInformation: null
    });

    OB.UTIL.HookManager.executeHooks(
      'OBPOS_cashManagementTransactionHook',
      {
        newCashManagementTransaction: newCashManagementTransaction,
        cashManagementTransactionToAdd: cashManagementTransactionToAdd
      },
      async function(args) {
        if (args && args.cancelOperation) {
          errorCallback(args.errorMessage);
        }
        await OB.App.State.Cashup.createCashManagement({
          cashManagement: JSON.parse(
            JSON.stringify(cashManagementTransactionToAdd)
          )
        });

        OB.App.State.Global.processCashManagements({
          parameters: {
            terminalName: OB.MobileApp.model.get('logConfiguration')
              .deviceIdentifier,
            cacheSessionId: OB.UTIL.localStorage.getItem('cacheSessionId'),
            terminalPayments: OB.MobileApp.model.get('payments')
          }
        })
          .then(function() {
            if (optionsObj.printTicket) {
              if (
                OB.MobileApp.model.hasPermission('OBPOS_print.cashmanagement')
              ) {
                var toPrint = new Backbone.Collection();
                toPrint.add(cashManagementTransactionToAdd);
                printCashManagementModel = optionsObj.ticketTemplate;
                printCashManagementModel.print(toPrint.toJSON());
                successCallback();
              } else {
                warns.push({
                  msg: OB.I18N.getLabel(
                    'OBPOS_NoPermissionToPrintCashManagment'
                  ),
                  errObj: {}
                });
                successCallback({
                  warnings: warns
                });
              }
            } else {
              successCallback();
            }
          })
          .catch(function(error) {
            warns.push({
              msg: OB.I18N.getLabel('OBPOS_CashManagmentNoSync'),
              errObj: error
            });
            if (optionsObj.printTicket) {
              if (
                OB.MobileApp.model.hasPermission('OBPOS_print.cashmanagement')
              ) {
                var toPrint = new Backbone.Collection();
                toPrint.add(cashManagementTransactionToAdd);
                printCashManagementModel = optionsObj.ticketTemplate;
                printCashManagementModel.print(toPrint.toJSON());
                successCallback();
              } else {
                warns.push({
                  msg: OB.I18N.getLabel(
                    'OBPOS_NoPermissionToPrintCashManagment'
                  ),
                  errObj: {}
                });
                successCallback({
                  warnings: warns
                });
              }
            } else {
              successCallback();
            }
          });
      }
    );
  };
})();
