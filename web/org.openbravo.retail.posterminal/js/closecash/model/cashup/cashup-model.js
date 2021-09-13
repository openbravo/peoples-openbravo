/*
 ************************************************************************************
 * Copyright (C) 2014-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.OBPOSCashUp = OB.OBPOSCashUp || {};
OB.OBPOSCashUp.Model = OB.OBPOSCashUp.Model || {};
OB.OBPOSCashUp.UI = OB.OBPOSCashUp.UI || {};

//Window model
OB.OBPOSCashUp.Model.CashUp = OB.OBPOSCloseCash.Model.CloseCash.extend({
  models: [],
  stepsDefinition: [
    {
      name: 'OB.CashUp.StepPendingOrders',
      loaded: false,
      classes: 'cashupStepsDefinition-obCashupStepPendingOrders',
      active: true
    },
    {
      name: 'OB.CashUp.Master',
      loaded: true,
      classes: 'cashupStepsDefinition-obCashupMaster',
      active: false
    },
    {
      name: 'OB.CloseCash.CashPayments',
      loaded: false,
      classes: 'cashupStepsDefinition-obCashupCashPayments',
      active: true
    },
    {
      name: 'OB.CloseCash.PaymentMethods',
      loaded: false,
      classes: 'cashupStepsDefinition-obCashupPaymentMethods',
      active: true
    },
    {
      name: 'OB.CloseCash.CashToKeep',
      loaded: true,
      active: true,
      classes: 'cashupStepsDefinition-obCashupCashToKeep'
    },
    {
      name: 'OB.CloseCash.PostPrintAndClose',
      loaded: true,
      classes: 'cashupStepsDefinition-obCashupPostPrintAndClose',
      active: true
    }
  ],
  init: function() {
    OB.error(
      'This init method should never be called for this model. Call initModels and loadModels instead'
    );
    this.initModels(function() {
      return this;
    });
    this.loadModels(function() {
      return this;
    });
  },
  initializationProcess: function(initModelsCallback) {
    //Check for orders which are being processed in this moment.
    //cancel -> back to point of sale
    //Ok -> Continue closing without these orders
    const terminalSlave =
      !OB.POS.modelterminal.get('terminal').ismaster &&
      OB.POS.modelterminal.get('terminal').isslave;
    let closeCashReport,
      synch1 = false,
      synch2 = false,
      synch3 = false;

    const finish = () => {
      if (synch1 && synch2 && synch3) {
        this.finishLoad();
      }
    };

    this.stepsDefinition[
      this.stepIndex('OB.CashUp.Master')
    ].active = OB.POS.modelterminal.get('terminal').ismaster;
    this.stepsDefinition[
      this.stepIndex('OB.CashUp.StepPendingOrders')
    ].loaded = false;
    this.stepsDefinition[
      this.stepIndex('OB.CloseCash.CashPayments')
    ].loaded = false;
    this.stepsDefinition[
      this.stepIndex('OB.CloseCash.PaymentMethods')
    ].loaded = false;

    this.set('orderlist', new Backbone.Collection());

    const payMthds = new Backbone.Collection(
      OB.App.State.getState().Cashup.cashPaymentMethodInfo
    );

    this.setPaymentList(payMthds, true);

    this.stepsDefinition[
      this.stepIndex('OB.CloseCash.CashPayments')
    ].loaded = true;
    this.stepsDefinition[
      this.stepIndex('OB.CloseCash.PaymentMethods')
    ].loaded = true;
    synch1 = true;
    finish();

    closeCashReport = new Backbone.Model(OB.App.State.getState().Cashup);

    initModelsCallback();

    closeCashReport.set('deposits', []);
    closeCashReport.set('drops', []);
    this.deposit = 0;
    this.drop = 0;
    const cashMgmts = new OB.Collection.CashManagementList();
    OB.App.State.Cashup.Utils.getCashManagements(
      OB.App.State.getState().Cashup.cashPaymentMethodInfo
    ).forEach(cashManagement =>
      cashMgmts.add(OB.Dal.transform(OB.Model.CashManagement, cashManagement))
    );

    cashMgmts.models.forEach((cashMgmt, index) => {
      const payment = OB.MobileApp.model.get('payments').filter(pay => {
        return (
          pay.payment.id === cashMgmt.get('paymentMethodId') &&
          !pay.paymentMethod.issafebox
        );
      })[0];
      if (payment) {
        cashMgmt.set(
          'countInCashup',
          payment.paymentMethod.countpaymentincashup
        );
        cashMgmt.set(
          'searchKey',
          (cashMgmt.get('type') === 'deposit'
            ? 'cashMgmtDeposit'
            : 'cashMgmtDrop') +
            index +
            payment.payment.searchKey.replace('_', '') +
            cashMgmt.get('amount')
        );
        closeCashReport.get(cashMgmt.get('type') + 's').push(cashMgmt);
      }
      this[cashMgmt.get('type')] = OB.DEC.add(
        this[cashMgmt.get('type')],
        cashMgmt.get('origAmount')
      );
    });
    closeCashReport.set('totalDeposits', this.deposit);
    closeCashReport.set('totalDrops', this.drop);
    delete this.deposit;
    delete this.drop;

    const cashup = OB.App.State.getState().Cashup;
    closeCashReport.set(
      'salesTaxes',
      new Backbone.Collection(
        cashup.cashTaxInfo.filter(tax => {
          return tax.orderType !== '1';
        })
      ).models
    );
    closeCashReport.set(
      'returnsTaxes',
      new Backbone.Collection(
        cashup.cashTaxInfo.filter(tax => {
          return tax.orderType === '1';
        })
      ).models
    );
    //OB.Dal.find success
    closeCashReport.set(
      'totalStartings',
      payMthds.models.reduce((accum, trx) => {
        if (
          OB.MobileApp.model.paymentnames[trx.get('searchKey')] &&
          OB.MobileApp.model.paymentnames[trx.get('searchKey')].paymentMethod
            .countpaymentincashup
        ) {
          // Not accumulate shared payments on slave terminal
          if (
            terminalSlave &&
            OB.MobileApp.model.paymentnames[trx.get('searchKey')].paymentMethod
              .isshared
          ) {
            return accum;
          }
          // Not accumulate if payment method is defined in safe box
          if (
            OB.MobileApp.model.paymentnames[trx.get('searchKey')].paymentMethod
              .issafebox
          ) {
            return accum;
          }
          const fromCurrencyId =
            OB.MobileApp.model.paymentnames[trx.get('searchKey')].paymentMethod
              .currency;
          const cStartingCash = OB.UTIL.currency.toDefaultCurrency(
            fromCurrencyId,
            trx.get('startingCash')
          );
          return OB.DEC.add(accum, cStartingCash);
        }
        return accum;
      }, 0)
    );

    closeCashReport.set(
      'totalDeposits',
      payMthds.models.reduce((accum, trx) => {
        if (OB.MobileApp.model.paymentnames[trx.get('searchKey')]) {
          // Not accumulate shared payments on slave terminal
          if (
            terminalSlave &&
            OB.MobileApp.model.paymentnames[trx.get('searchKey')].paymentMethod
              .isshared
          ) {
            return accum;
          }
          // Not accumulate if payment method is defined in safe box
          if (
            OB.MobileApp.model.paymentnames[trx.get('searchKey')].paymentMethod
              .issafebox
          ) {
            return accum;
          }
          const fromCurrencyId =
            OB.MobileApp.model.paymentnames[trx.get('searchKey')].paymentMethod
              .currency;
          const cTotalDeposits = OB.UTIL.currency.toDefaultCurrency(
            fromCurrencyId,
            OB.DEC.add(trx.get('totalDeposits'), trx.get('totalSales'))
          );
          return OB.DEC.add(accum, cTotalDeposits);
        }
        return accum;
      }, 0)
    );

    closeCashReport.set(
      'totalDrops',
      payMthds.models.reduce((accum, trx) => {
        if (OB.MobileApp.model.paymentnames[trx.get('searchKey')]) {
          // Not accumulate shared payments on slave terminal
          if (
            terminalSlave &&
            OB.MobileApp.model.paymentnames[trx.get('searchKey')].paymentMethod
              .isshared
          ) {
            return accum;
          }
          // Not accumulate if payment method is defined in safe box
          if (
            OB.MobileApp.model.paymentnames[trx.get('searchKey')].paymentMethod
              .issafebox
          ) {
            return accum;
          }
          const fromCurrencyId =
            OB.MobileApp.model.paymentnames[trx.get('searchKey')].paymentMethod
              .currency;
          const cTotalDrops = OB.UTIL.currency.toDefaultCurrency(
            fromCurrencyId,
            OB.DEC.add(trx.get('totalDrops'), trx.get('totalReturns'))
          );
          return OB.DEC.add(accum, cTotalDrops);
        }
        return accum;
      }, 0)
    );

    let startings = [];
    payMthds.models.forEach(p => {
      const auxPay = OB.MobileApp.model.get('payments').filter(pay => {
        return pay.payment.id === p.get('id') && !pay.paymentMethod.issafebox;
      })[0];
      if (!auxPay) {
        //We cannot find this payment in local database, it must be a new payment method, we skip it.
        return;
      }
      // Not add shared payment to slave terminal
      if (
        terminalSlave &&
        OB.MobileApp.model.paymentnames[auxPay.payment.searchKey].paymentMethod
          .isshared
      ) {
        return;
      }

      if (
        !auxPay.paymentMethod.countpaymentincashup &&
        !auxPay.paymentMethod.isRounding
      ) {
        return;
      }

      const fromCurrencyId = auxPay.paymentMethod.currency,
        paymentShared =
          (OB.POS.modelterminal.get('terminal').ismaster ||
            OB.POS.modelterminal.get('terminal').isslave) &&
          OB.MobileApp.model.paymentnames[auxPay.payment.searchKey]
            .paymentMethod.isshared,
        paymentSharedStr = paymentShared
          ? OB.I18N.getLabel('OBPOS_LblPaymentMethodShared')
          : '';

      closeCashReport.get('deposits').push(
        new Backbone.Model({
          searchKey: p.get('searchKey'),
          origAmount: OB.UTIL.currency.toDefaultCurrency(
            fromCurrencyId,
            OB.DEC.add(0, p.get('totalSales'))
          ),
          amount: OB.DEC.add(0, p.get('totalSales')),
          description: p.get('name') + paymentSharedStr,
          currency: fromCurrencyId,
          isocode: auxPay.isocode,
          rate: p.get('rate'),
          countInCashup: auxPay.paymentMethod.countpaymentincashup
        })
      );
      closeCashReport.get('drops').push(
        new Backbone.Model({
          searchKey: p.get('searchKey'),
          origAmount: OB.UTIL.currency.toDefaultCurrency(
            fromCurrencyId,
            OB.DEC.add(0, p.get('totalReturns'))
          ),
          amount: OB.DEC.add(0, p.get('totalReturns')),
          description: p.get('name') + paymentSharedStr,
          currency: fromCurrencyId,
          isocode: auxPay.isocode,
          rate: p.get('rate'),
          countInCashup: auxPay.paymentMethod.countpaymentincashup
        })
      );

      if (auxPay.paymentMethod.countpaymentincashup) {
        startings.push(
          new Backbone.Model({
            searchKey: p.get('searchKey'),
            origAmount: OB.UTIL.currency.toDefaultCurrency(
              fromCurrencyId,
              p.get('startingCash')
            ),
            amount: OB.DEC.add(0, p.get('startingCash')),
            description:
              OB.I18N.getLabel('OBPOS_LblStarting') +
              ' ' +
              p.get('name') +
              paymentSharedStr,
            currency: fromCurrencyId,
            isocode: auxPay.isocode,
            rate: p.get('rate'),
            paymentId: p.get('id'),
            countInCashup: auxPay.paymentMethod.countpaymentincashup
          })
        );
      }
    });
    closeCashReport.set('startings', startings);
    //FIXME: We are not sure if other finds are done.
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_EditCashupReport',
      {
        cashUpReport: closeCashReport
      },
      args => {
        this.get('closeCashReport').add(args.cashUpReport);
        synch2 = true;
        finish();
      }
    );

    this.get('paymentList').on('change:counted', mod => {
      mod.set(
        'difference',
        OB.DEC.sub(mod.get('counted'), mod.get('expected'))
      );
      if (
        mod.get('foreignCounted') !== null &&
        mod.get('foreignCounted') !== undefined &&
        mod.get('foreignExpected') !== null &&
        mod.get('foreignExpected') !== undefined
      ) {
        mod.set(
          'foreignDifference',
          OB.DEC.sub(mod.get('foreignCounted'), mod.get('foreignExpected'))
        );
      }
      this.set(
        'totalCounted',
        this.get('paymentList').models.reduce((total, model) => {
          return model.get('counted')
            ? OB.DEC.add(total, model.get('counted'))
            : total;
        }, 0),
        0
      );
      if (mod.get('counted') === OB.DEC.Zero) {
        this.trigger('change:totalCounted');
      }
    });

    const pendingTickets = OB.App.State.TicketList.Utils.getAllTickets().filter(
      ticket => ticket.hasbeenpaid === 'N' && ticket.lines.length > 0
    );

    OB.App.State.Global.markIgnoreCheckIfIsActiveOrderToPendingTickets({
      session: OB.MobileApp.model.get('session')
    })
      .then(() => {
        const orderList = pendingTickets.map(ticket =>
          OB.App.StateBackwardCompatibility.getInstance(
            'Ticket'
          ).toBackboneObject(ticket)
        );
        this.get('orderlist').reset(orderList);
        const stepDefinition = this.stepsDefinition[
          this.stepIndex('OB.CashUp.StepPendingOrders')
        ];
        stepDefinition.active = pendingTickets.length > 0;
        stepDefinition.loaded = true;
        synch3 = true;
        finish();
      })
      .catch(OB.UTIL.showError);
  },
  isFinishedWizard: function(step) {
    // Adjust step to array index
    var postPrintAndClose = this.stepIndex('OB.CloseCash.PostPrintAndClose');
    if (this.stepsDefinition[postPrintAndClose].active) {
      return step === postPrintAndClose + 2;
    }
    return false;
  },
  isPaymentMethodListVisible: function() {
    // Adjust step to array index
    return this.get('step') - 1 === this.stepIndex('OB.CloseCash.CashPayments');
  },
  processAndFinish: function() {
    const cashupext = {};
    OB.UTIL.HookManager.executeHooks(
      'OBPOS_ProcessCashup',
      { cashupext },
      () => {
        this.processAndFinishExt(cashupext);
      }
    );
  },
  processAndFinishExt: function(cashupext) {
    OB.UTIL.showLoading(true);

    const cashUp = new Backbone.Collection([OB.App.State.getState().Cashup]);

    const now = new Date();
    let paymentMethodInfo,
      objToSend = {}; // created empty instead load from cashup, since in state is no longer stored the objToSend

    objToSend.cashUpDate = OB.I18N.normalizeDate(now);
    objToSend.lastcashupeportdate = OB.I18N.normalizeDate(now);
    objToSend.timezoneOffset = now.getTimezoneOffset();
    if (OB.UTIL.localStorage.getItem('currentSafeBox')) {
      objToSend.currentSafeBox = JSON.parse(
        OB.UTIL.localStorage.getItem('currentSafeBox')
      ).searchKey;
    }
    for (let i = 0; i < this.additionalProperties.length; i++) {
      objToSend[this.additionalProperties[i]] = this.propertyFunctions[i](
        OB.POS.modelterminal.get('terminal').id,
        cashUp.at(0)
      );
    }
    let cashCloseArray = [];
    objToSend.cashCloseInfo = cashCloseArray;
    this.get('paymentList').models.forEach(curModel => {
      let cashCloseInfo = {
        expected: 0,
        difference: 0,
        paymentTypeId: 0,
        paymentMethod: {}
      };
      // Set cashclose info
      cashCloseInfo.id = OB.UTIL.get_UUID();
      cashCloseInfo.paymentTypeId = curModel.get('id');
      cashCloseInfo.difference = curModel.get('difference');
      cashCloseInfo.foreignDifference = curModel.get('foreignDifference');
      cashCloseInfo.expected = curModel.get('expected');
      cashCloseInfo.foreignExpected = curModel.get('foreignExpected');
      paymentMethodInfo = curModel.get('paymentMethod');
      paymentMethodInfo.amountToKeep = curModel.get('qtyToKeep');
      cashCloseInfo.paymentMethod = paymentMethodInfo;
      objToSend.cashCloseInfo.push(cashCloseInfo);
    });
    objToSend.approvals = this.get('approvals');
    let cashMgmtIds = [];
    objToSend.cashMgmtIds = cashMgmtIds;
    const cashMgmts = new OB.Collection.CashManagementList();
    OB.App.State.Cashup.Utils.getCashManagements(
      OB.App.State.getState().Cashup.cashPaymentMethodInfo
    ).forEach(cashManagement =>
      cashMgmts.add(OB.Dal.transform(OB.Model.CashManagement, cashManagement))
    );
    cashMgmts.models.forEach(cashMgmt => {
      objToSend.cashMgmtIds.push(cashMgmt.get('id'));
    });
    cashUp.at(0).set('userId', OB.MobileApp.model.get('context').user.id);
    objToSend = Object.assign(objToSend, cashupext); // Add all properties added by the hook OBPOS_ProcessCashup
    objToSend.userId = OB.MobileApp.model.get('context').user.id;
    objToSend.isprocessed = 'Y';
    cashUp.at(0).set('objToSend', JSON.stringify(objToSend));
    cashUp.at(0).set('isprocessed', 'Y');

    const callbackFinishedSuccess = () => {
      OB.UTIL.showLoading(true);
      this.set('finished', true);
      if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
        OB.UTIL.localStorage.setItem(
          'lastProcessedCashup',
          cashUp.at(0).get('id')
        );
      }
      if (OB.MobileApp.model.hasPermission('OBPOS_print.cashup')) {
        let cashUpReport = new OB.Model.CashUp(),
          countCashSummary = this.getCountCashSummary();
        OB.UTIL.clone(this.get('closeCashReport').at(0), cashUpReport);
        if (
          OB.MobileApp.model.hasPermission(
            'OBPOS_retail.cashupRemoveUnusedPayment',
            true
          )
        ) {
          let paymentWithMovement = [];
          if (OB.MobileApp.view.currentWindow !== 'retail.cashuppartial') {
            OB.UTIL.closeCashAddPaymentWithSummaryMovement(
              paymentWithMovement,
              countCashSummary.countedSummary
            );
            OB.UTIL.closeCashAddPaymentWithSummaryMovement(
              paymentWithMovement,
              countCashSummary.differenceSummary
            );
            OB.UTIL.closeCashAddPaymentWithSummaryMovement(
              paymentWithMovement,
              countCashSummary.qtyToKeepSummary
            );
            OB.UTIL.closeCashAddPaymentWithSummaryMovement(
              paymentWithMovement,
              countCashSummary.qtyToDepoSummary
            );
          }
          OB.UTIL.closeCashAddPaymentWithSummaryMovement(
            paymentWithMovement,
            countCashSummary.expectedSummary
          );
          OB.UTIL.closeCashAddPaymentWithMovement(
            paymentWithMovement,
            cashUpReport.get('startings')
          );
          OB.UTIL.closeCashAddPaymentWithMovement(
            paymentWithMovement,
            cashUpReport.get('drops')
          );
          OB.UTIL.closeCashAddPaymentWithMovement(
            paymentWithMovement,
            cashUpReport.get('deposits')
          );
          countCashSummary.expectedSummary = OB.UTIL.closeCashGetPaymentWithMovement(
            paymentWithMovement,
            countCashSummary.expectedSummary
          );
          cashUpReport.set(
            'startings',
            OB.UTIL.closeCashGetPaymentWithMovement(
              paymentWithMovement,
              cashUpReport.get('startings')
            )
          );
          cashUpReport.set(
            'drops',
            OB.UTIL.closeCashGetPaymentWithMovement(
              paymentWithMovement,
              cashUpReport.get('drops')
            )
          );
          cashUpReport.set(
            'deposits',
            OB.UTIL.closeCashGetPaymentWithMovement(
              paymentWithMovement,
              cashUpReport.get('deposits')
            )
          );
          if (OB.MobileApp.view.currentWindow !== 'retail.cashuppartial') {
            countCashSummary.countedSummary = OB.UTIL.closeCashGetPaymentWithMovement(
              paymentWithMovement,
              countCashSummary.countedSummary
            );
            countCashSummary.differenceSummary = OB.UTIL.closeCashGetPaymentWithMovement(
              paymentWithMovement,
              countCashSummary.differenceSummary
            );
            countCashSummary.qtyToKeepSummary = OB.UTIL.closeCashGetPaymentWithMovement(
              paymentWithMovement,
              countCashSummary.qtyToKeepSummary
            );
            countCashSummary.qtyToDepoSummary = OB.UTIL.closeCashGetPaymentWithMovement(
              paymentWithMovement,
              countCashSummary.qtyToDepoSummary
            );
          }
        }

        this.printCloseCash.print(cashUpReport, countCashSummary, true, null);

        // Remove current safe box at the end of cashup
        OB.UTIL.localStorage.removeItem('currentSafeBox');
      }
    };
    const callbackFinishedWrongly = () => {
      this.set('finishedWrongly', true);
    };

    const callbackFunc = () => {
      var synchronizedPreferenceValue;
      // prevent synchronized mode for cashups
      synchronizedPreferenceValue = OB.MobileApp.model.setSynchronizedPreference(
        false
      );
      OB.UTIL.HookManager.executeHooks(
        'OBPOS_PrePrintCashupHook',
        {
          cashupModel: this
        },
        args => {
          OB.App.State.Global.completeCashupAndCreateNew({
            completedCashupParams: {
              closeCashupInfo: JSON.parse(cashUp.at(0).get('objToSend')),
              terminalName: OB.MobileApp.model.get('logConfiguration')
                .deviceIdentifier,
              cacheSessionId: OB.UTIL.localStorage.getItem('cacheSessionId'),
              statisticsToIncludeInCashup: OB.App.State.Cashup.Utils.getStatisticsToIncludeInCashup()
            },
            newCashupParams: {
              currentDate: new Date(),
              userId: OB.MobileApp.model.get('context').user.id,
              terminalId: OB.MobileApp.model.get('terminal').id,
              terminalIsSlave: OB.POS.modelterminal.get('terminal').isslave,
              terminalIsMaster: OB.POS.modelterminal.get('terminal').ismaster,
              terminalPayments: OB.MobileApp.model.get('payments')
            }
          })
            .then(() => {
              OB.App.State.Cashup.Utils.resetStatisticsIncludedInCashup();
              OB.MobileApp.model.setSynchronizedPreference(
                synchronizedPreferenceValue
              );
              callbackFinishedSuccess();
            })
            .catch(e => {
              OB.error(e.stack);
              callbackFinishedWrongly();
            });
        }
      );
    };
    callbackFunc();
  },
  closeCashReportChanged: function(closeCashReport, closeCashReportComponent) {
    closeCashReportComponent.$.openingtime.setContent(
      OB.I18N.getLabel('OBPOS_LblOpenTime') +
        ': ' +
        OB.I18N.formatDate(new Date(closeCashReport.get('creationDate'))) +
        ' - ' +
        OB.I18N.formatHour(new Date(closeCashReport.get('creationDate')))
    );
    closeCashReportComponent.$.sales.setValue(
      'netsales',
      closeCashReport.get('netSales')
    );
    closeCashReportComponent.$.sales.setCollection(
      closeCashReport.get('salesTaxes')
    );
    closeCashReportComponent.$.sales.setValue(
      'totalsales',
      closeCashReport.get('grossSales')
    );

    closeCashReportComponent.$.returns.setValue(
      'netreturns',
      closeCashReport.get('netReturns')
    );
    closeCashReportComponent.$.returns.setCollection(
      closeCashReport.get('returnsTaxes')
    );
    closeCashReportComponent.$.returns.setValue(
      'totalreturns',
      closeCashReport.get('grossReturns')
    );

    closeCashReportComponent.$.totaltransactions.setValue(
      'totaltransactionsline',
      closeCashReport.get('totalRetailTransactions')
    );

    if (!OB.POS.modelterminal.get('terminal').ismaster) {
      closeCashReportComponent.closeCashReportChanged(closeCashReport);
    }
  }
});

OB.OBPOSCashUp.Model.CashUpPartial = OB.OBPOSCashUp.Model.CashUp.extend({
  initialStep: 6,
  finishButtonLabel: 'OBPOS_LblPrintClose',
  reportTitleLabel: 'OBPOS_LblPartialCashUpTitle',
  isPartialCashup: true,

  processAndFinish: function() {
    if (OB.MobileApp.model.hasPermission('OBPOS_print.cashup')) {
      this.printCloseCash.print(
        this.get('closeCashReport').at(0),
        this.getCountCashSummary(),
        false,
        () => {
          this.set('finished', true);
        }
      );
    }
  },
  allowPrevious: function() {
    return false;
  },
  finishLoad: function() {
    var finish = true;
    this.stepsDefinition.forEach(step => {
      if (!step.loaded) {
        finish = false;
      }
    });
    if (finish && !this.get('loadFinished')) {
      this.set('loadFinished', true);
    }
  }
});
