/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone */

OB.OBPOSCountSafeBox = OB.OBPOSCountSafeBox || {};
OB.OBPOSCountSafeBox.Model = OB.OBPOSCountSafeBox.Model || {};
OB.OBPOSCountSafeBox.UI = OB.OBPOSCountSafeBox.UI || {};

//Window model
OB.OBPOSCountSafeBox.Model.CountSafeBox = OB.OBPOSCloseCash.Model.CloseCash.extend(
  {
    stepsDefinition: [
      {
        name: 'OB.CountSafeBox.StepSafeBoxList',
        loaded: false,
        active: false
      },
      {
        name: 'OB.CloseCash.CashPayments',
        loaded: false,
        active: true,
        classes: 'cashupStepsDefinition-obCashupCashPayments'
      },
      {
        name: 'OB.CloseCash.PaymentMethods',
        loaded: false,
        active: true,
        classes: 'cashupStepsDefinition-obCashupPaymentMethods'
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
        active: true,
        classes: 'cashupStepsDefinition-obCashupPostPrintAndClose'
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
      this.stepsDefinition[
        this.stepIndex('OB.CountSafeBox.StepSafeBoxList')
      ].loaded = false;
      this.stepsDefinition[
        this.stepIndex('OB.CloseCash.CashPayments')
      ].loaded = false;
      this.stepsDefinition[
        this.stepIndex('OB.CloseCash.PaymentMethods')
      ].loaded = false;

      this.set('safeBoxesList', new Backbone.Collection());

      initModelsCallback();

      const indexStepSafeBoxList = this.stepIndex(
        'OB.CountSafeBox.StepSafeBoxList'
      );

      if (
        OB.MobileApp.model.hasPermission('OBPOS_approval.manager.safebox', true)
      ) {
        this.stepsDefinition[indexStepSafeBoxList].active = true;
        this.stepsDefinition[indexStepSafeBoxList].loaded = true;
        this.stepsDefinition[
          this.stepIndex('OB.CloseCash.CashPayments')
        ].loaded = true;
        this.stepsDefinition[
          this.stepIndex('OB.CloseCash.PaymentMethods')
        ].loaded = true;
      } else {
        this.stepsDefinition[indexStepSafeBoxList].loaded = true;
      }

      this.loadSafeBoxesInformation(
        safeBoxes => {
          if (
            OB.MobileApp.model.hasPermission(
              'OBPOS_approval.manager.safebox',
              true
            ) &&
            safeBoxes.length > 0
          ) {
            // Insert all safe boxes in safeBoxList
            this.get('safeBoxesList').reset(safeBoxes);
            this.finishLoad();
          } else if (safeBoxes.length === 1) {
            // At this point, only 1 safeBox should we received
            const safeBox = safeBoxes[0];
            this.prepareCashUpReport(safeBox, () => this.finishLoad());
          }
        },
        () => {
          initModelsCallback();
          this.finishLoad();
        }
      );
    },
    prepareCashUpReport: function(safeBox, callback) {
      let safeBoxPayments = new Backbone.Collection();
      safeBox.paymentMethods.forEach((sbPaymentMethod, index) => {
        OB.MobileApp.model.get('payments').forEach(termPayment => {
          if (
            termPayment.paymentMethod.paymentMethod ===
            sbPaymentMethod.paymentMethodId
          ) {
            const sbPaymentCounting = sbPaymentMethod.safeBoxCounting,
              expected = OB.DEC.sub(
                OB.DEC.add(
                  sbPaymentCounting.initialBalance,
                  sbPaymentCounting.depositBalance
                ),
                sbPaymentCounting.paymentBalance
              );
            let safeBoxPayment = new OB.Model.PaymentMethodCashUp();
            safeBoxPayment.set('_id', termPayment.payment.searchKey);
            safeBoxPayment.set('searchKey', termPayment.payment.searchKey);
            safeBoxPayment.set('name', termPayment.payment.commercialName);
            safeBoxPayment.set('isocode', termPayment.isocode);
            safeBoxPayment.set('rate', parseFloat(termPayment.rate));
            safeBoxPayment.set(
              'startingCash',
              sbPaymentMethod.initialBalance || 0
            );
            safeBoxPayment.set('terminalPaymentId', termPayment.payment.id);
            safeBoxPayment.set('id', sbPaymentMethod.safeBoxPaymentMethodId);
            safeBoxPayment.set(
              'totalDeposits',
              sbPaymentMethod.depositBalance || 0
            );
            safeBoxPayment.set(
              'totalDrops',
              sbPaymentMethod.paymentBalance || 0
            );
            safeBoxPayment.set(
              'expected',
              OB.UTIL.currency.toDefaultCurrency(
                termPayment.paymentMethod.currency,
                expected
              )
            );
            safeBoxPayment.set('foreignExpected', expected);

            // Override paymentMethod information relevant for the cashup process
            let paymentMethod = { ...termPayment.paymentMethod };
            paymentMethod.iscash = sbPaymentMethod.isCash;
            paymentMethod.countcash = sbPaymentMethod.countCash;
            paymentMethod.automatemovementtoother =
              sbPaymentMethod.automateMovementToOtherAccount;
            paymentMethod.keepfixedamount = sbPaymentMethod.keepFixedAmount;
            paymentMethod.amount = sbPaymentMethod.amount;
            paymentMethod.allowvariableamount =
              sbPaymentMethod.allowVariableAmount;
            paymentMethod.allowmoveeverything =
              sbPaymentMethod.allowMoveEverything;
            paymentMethod.allowdontmove = sbPaymentMethod.allowNotToMove;
            paymentMethod.countDiffLimit = sbPaymentMethod.countDifferenceLimit;

            safeBoxPayment.set('paymentMethod', paymentMethod);

            safeBoxPayments.add(safeBoxPayment);
          }
        });
        if (index === safeBox.paymentMethods.length - 1) {
          this.get('paymentList').reset(safeBoxPayments.models);
        }

        // Active/Desactive CashPayments and CashToKeep tabs
        const paymentsIndex = this.stepIndex('OB.CloseCash.CashPayments'),
          toKeepIndex = this.stepIndex('OB.CloseCash.CashToKeep');
        let cashPayments = false,
          cashToKeep = false;

        for (let i = 0; i < safeBoxPayments.length; i++) {
          if (this.closeCashSteps[paymentsIndex].isSubstepAvailable(this, i)) {
            cashPayments = true;
          }
          if (this.closeCashSteps[toKeepIndex].isSubstepAvailable(this, i)) {
            cashToKeep = true;
          }
        }
        this.stepsDefinition[paymentsIndex].active = cashPayments;
        this.stepsDefinition[toKeepIndex].active = cashToKeep;
        this.set(
          'totalExpected',
          this.get('paymentList').models.reduce((total, model) => {
            return OB.DEC.add(total, model.get('expected'));
          }, 0)
        );
        this.set(
          'totalDifference',
          OB.DEC.sub(this.get('totalDifference'), this.get('totalExpected'))
        );
        this.setIgnoreStep3();
      });
      this.stepsDefinition[
        this.stepIndex('OB.CloseCash.CashPayments')
      ].loaded = true;
      this.stepsDefinition[
        this.stepIndex('OB.CloseCash.PaymentMethods')
      ].loaded = true;

      let closeCashReport = new OB.Model.CashUp();
      closeCashReport.set('deposits', []);
      closeCashReport.set('drops', []);
      closeCashReport.set('creationDate', new Date());

      closeCashReport.set(
        'totalStartings',
        safeBoxPayments.models.reduce((accum, safeBoxPayment) => {
          const cStartingCash = OB.UTIL.currency.toDefaultCurrency(
            safeBoxPayment.get('paymentMethod').currency,
            safeBoxPayment.get('startingCash')
          );
          return OB.DEC.add(accum, cStartingCash);
        }, 0)
      );

      closeCashReport.set(
        'totalDeposits',
        safeBoxPayments.models.reduce((accum, safeBoxPayment) => {
          const cTotalDeposits = OB.UTIL.currency.toDefaultCurrency(
            safeBoxPayment.get('paymentMethod').currency,
            safeBoxPayment.get('totalDeposits')
          );
          return OB.DEC.add(accum, cTotalDeposits);
        }, 0)
      );

      closeCashReport.set(
        'totalDrops',
        safeBoxPayments.models.reduce((accum, safeBoxPayment) => {
          var cTotalDrops = OB.UTIL.currency.toDefaultCurrency(
            safeBoxPayment.get('paymentMethod').currency,
            safeBoxPayment.get('totalDrops')
          );
          return OB.DEC.add(accum, cTotalDrops);
        }, 0)
      );

      let startings = [];
      safeBoxPayments.models.forEach(safeBoxPayment => {
        const fromCurrencyId = safeBoxPayment.get('paymentMethod').currency;
        closeCashReport.get('deposits').push(
          new Backbone.Model({
            searchKey: safeBoxPayment.get('searchKey'),
            origAmount: OB.UTIL.currency.toDefaultCurrency(
              fromCurrencyId,
              OB.DEC.add(0, safeBoxPayment.get('totalDeposits'))
            ),
            amount: OB.DEC.add(0, safeBoxPayment.get('totalDeposits')),
            description: safeBoxPayment.get('name'),
            currency: fromCurrencyId,
            isocode: safeBoxPayment.get('isocode'),
            rate: safeBoxPayment.get('rate'),
            countInCashup: true
          })
        );
        closeCashReport.get('drops').push(
          new Backbone.Model({
            searchKey: safeBoxPayment.get('searchKey'),
            origAmount: OB.UTIL.currency.toDefaultCurrency(
              fromCurrencyId,
              OB.DEC.add(0, safeBoxPayment.get('totalDrops'))
            ),
            amount: OB.DEC.add(0, safeBoxPayment.get('totalDrops')),
            description: safeBoxPayment.get('name'),
            currency: fromCurrencyId,
            isocode: safeBoxPayment.get('isocode'),
            rate: safeBoxPayment.get('rate'),
            countInCashup: true
          })
        );
        startings.push(
          new Backbone.Model({
            searchKey: safeBoxPayment.get('searchKey'),
            origAmount: OB.UTIL.currency.toDefaultCurrency(
              fromCurrencyId,
              safeBoxPayment.get('startingCash')
            ),
            amount: OB.DEC.add(0, safeBoxPayment.get('startingCash')),
            description: safeBoxPayment.get('name'),
            currency: fromCurrencyId,
            isocode: safeBoxPayment.get('isocode'),
            rate: safeBoxPayment.get('rate'),
            countInCashup: true
          })
        );
      });
      closeCashReport.set('startings', startings);
      this.get('closeCashReport').add(closeCashReport);

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
      // No need to check orders peding to be ended
      callback();
    },
    closeCashReportChanged: function(
      closeCashReport,
      closeCashReportComponent
    ) {
      const filtered = closeCashReportComponent.filterMovements(
        closeCashReport,
        false
      );
      closeCashReportComponent.$.startingsTable.setCollection(
        filtered.startings
      );
      closeCashReportComponent.$.startingsTable.setValue(
        'totalstartings',
        closeCashReport.get('totalStartings')
      );

      closeCashReportComponent.$.dropsTable.setCollection(filtered.drops);
      closeCashReportComponent.$.dropsTable.setValue(
        'totaldrops',
        closeCashReport.get('totalDrops')
      );

      closeCashReportComponent.$.depositsTable.setCollection(filtered.deposits);
      closeCashReportComponent.$.depositsTable.setValue(
        'totaldeposits',
        closeCashReport.get('totalDeposits')
      );
    },
    loadSafeBoxesInformation: function(successCallback, errorCallback) {
      let params = {};
      if (
        JSON.parse(OB.UTIL.localStorage.getItem('currentSafeBox')) &&
        !OB.MobileApp.model.hasPermission(
          'OBPOS_approval.manager.safebox',
          true
        )
      ) {
        params.safeBoxSearchKey = JSON.parse(
          OB.UTIL.localStorage.getItem('currentSafeBox')
        ).searchKey;
      }

      // Start Safe Box Counting information process
      const process = new OB.DS.Process(
        'org.openbravo.retail.posterminal.SafeBoxes'
      );
      process.exec(
        params,
        data => {
          if (data && data.exception) {
            OB.UTIL.showError(
              OB.I18N.getLabel('OBPOS_ExceptionGettingSafeBoxes', [
                data.exception.message
              ])
            );
            errorCallback();
            return;
          }

          if (data && data.length === 0) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_NoSafeBoxesDefined'));
            errorCallback();
            return;
          }

          successCallback(data);
        },
        err => {
          //error or offline
          OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_ErrorGettingSafeBoxes'));
          errorCallback();
        }
      );
    },
    processAndFinish: function() {
      OB.UTIL.showLoading(true);

      OB.Dal.transaction(tx => {
        const currentSafeBox = JSON.parse(
            OB.UTIL.localStorage.getItem('currentSafeBox')
          ),
          now = new Date();
        let countSafeBox = {};
        // objToSend initialization
        let objToSend = new Backbone.Model({
          safeBox: currentSafeBox.searchKey,
          isprocessed: 'Y',
          isbeingprocessed: 'Y',
          countSafeBoxInfo: [],
          creationDate: now.toISOString(),
          countSafeBoxDate: OB.I18N.normalizeDate(now),
          lastCountSafeBoxDate: OB.I18N.normalizeDate(now),
          timezoneOffset: now.getTimezoneOffset(),
          approvals: this.get('approvals'),
          userId: OB.MobileApp.model.get('context').user.id
        });

        let countSafeBoxArray = [];
        objToSend.set('countSafeBoxInfo', countSafeBoxArray);
        this.get('paymentList').models.forEach(curModel => {
          let paymentMethodInfo,
            countSafeBoxInfo = {
              expected: 0,
              difference: 0,
              paymentTypeId: 0,
              paymentMethod: {}
            };
          // Set cashclose info
          countSafeBoxInfo.id = OB.UTIL.get_UUID();
          countSafeBoxInfo.paymentTypeId = curModel.get('id');
          countSafeBoxInfo.difference = curModel.get('difference');
          countSafeBoxInfo.foreignDifference = curModel.get(
            'foreignDifference'
          );
          countSafeBoxInfo.expected = curModel.get('expected');
          countSafeBoxInfo.foreignExpected = curModel.get('foreignExpected');
          paymentMethodInfo = curModel.get('paymentMethod');
          paymentMethodInfo.amountToKeep = curModel.get('qtyToKeep');
          countSafeBoxInfo.paymentMethod = paymentMethodInfo;
          objToSend.get('countSafeBoxInfo').push(countSafeBoxInfo);
        });

        countSafeBox.isbeingprocessed = 'Y';
        countSafeBox.creationDate = now;
        countSafeBox.safebox = currentSafeBox.searchKey;
        countSafeBox.userId = OB.MobileApp.model.get('context').user.id;
        countSafeBox.objToSend = JSON.stringify(objToSend);
        countSafeBox.isprocessed = 'Y';

        OB.App.State.Global.synchronizeCountSafeBox(countSafeBox)
          .then(sync => {
            const callbackFinishedSuccess = () => {
              OB.UTIL.showLoading(true);
              this.set('finished', true);
              if (OB.MobileApp.model.hasPermission('OBPOS_print.cashup')) {
                var cashUpReport = new OB.Model.CashUp(),
                  countCashSummary = this.getCountCashSummary();
                OB.UTIL.clone(this.get('closeCashReport').at(0), cashUpReport);
                if (
                  OB.MobileApp.model.hasPermission(
                    'OBPOS_retail.cashupRemoveUnusedPayment',
                    true
                  )
                ) {
                  var paymentWithMovement = [];
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

                this.printCloseCash.print(cashUpReport, countCashSummary, true);
              }
              // Remove current safe box at the end of cashup
              OB.UTIL.localStorage.removeItem('currentSafeBox');
            };

            const callbackFinishedWrongly = () => {
              // reset to N
              countSafeBox.isprocessed = 'N';

              OB.App.State.Global.synchronizeCountSafeBox(countSafeBox)
                .then(sync => {
                  this.set('finishedWrongly', true);
                })
                .catch(error => OB.error(error));
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
                function(args) {
                  OB.MobileApp.model.runSyncProcess(
                    function() {
                      OB.MobileApp.model.setSynchronizedPreference(
                        synchronizedPreferenceValue
                      );
                      callbackFinishedSuccess();
                    },
                    function() {
                      OB.MobileApp.model.setSynchronizedPreference(
                        synchronizedPreferenceValue
                      );
                      callbackFinishedWrongly();
                    }
                  );
                }
              );
            };
            callbackFunc();
          })
          .catch(error => OB.error(error));
      });
    }
  }
);
