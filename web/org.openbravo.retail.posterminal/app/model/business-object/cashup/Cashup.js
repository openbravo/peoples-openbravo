/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Defines the Cashup model and its actions
 */
(function CashupModelDefinition() {
  OB.App.StateAPI.registerModel('Cashup', {
    id: null,
    netSales: null,
    grossSales: null,
    netReturns: null,
    grossReturns: null,
    totalRetailTransactions: null,
    creationDate: null,
    userId: null,
    cashTaxInfo: [], // taxCashupId, name, amount, orderType
    cashPaymentMethodInfo: [], // paymentMethodCashupId, paymentMethodId, searchKey, name, startingCash, totalSales, totalReturns, totalDeposits, totalDrops, rate, isocode, newPaymentMethod
    cashManagements: [],
    isprocessed: null,
    statistics: {
      lastcashupeportdate: null,
      transitionsToOnline: null, // 0 ?
      logclientErrors: null, // 0 ?
      averageLatency: null,
      averageUploadBandwidth: null,
      averageDownloadBandwidth: null,
      //
      terminalLastfullrefresh: null,
      terminalLastincrefresh: null,
      terminalLastcachegeneration: null,
      terminalLastjsgeneration: null,
      terminalLastbenchmark: null,
      terminalLastlogindate: null,
      terminalLastloginuser: null,
      terminalLasttimeinoffline: null,
      terminalLasttimeinonline: null,
      terminalLasthwmversion: null,
      terminalLasthwmrevision: null,
      terminalLasthwmjavainfo: null
    }
  });

  /**
   * Initialize the cashup
   */
  OB.App.StateAPI.Cashup.registerActions({
    initCashup(cashup, payload) {
      let newCashup;

      const { initCashupFrom, terminalPayments, lastCashUpPayments } = payload;
      if (initCashupFrom === 'local') {
        // init from local
        newCashup = OB.App.State.Cashup.Utils.addNewPaymentMethodsToCurrentCashup(
          { cashup, terminalPayments: payload.terminalPayments }
        );
      } else if (initCashupFrom === 'backend') {
        // init from backend
        const { currentCashupFromBackend } = payload;

        OB.App.State.Cashup.Utils.resetStatistics();

        newCashup = OB.App.State.Cashup.Utils.createNewCashupFromBackend({
          cashup,
          currentCashupFromBackend
        });

        if (currentCashupFromBackend.cashPaymentMethodInfo.length !== 0) {
          newCashup.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.addPaymentsFromBackendCashup(
            { currentCashupFromBackend, terminalPayments }
          );
        } else {
          newCashup.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.initializePaymentMethodCashup(
            {
              terminalPayments
            }
          );
        }
      } else if (initCashupFrom === 'scratch') {
        // init from scratch
        const { terminalIsSlave } = payload;

        OB.App.State.Cashup.Utils.resetStatistics();

        newCashup = OB.App.State.Cashup.Utils.createNewCashupFromScratch({
          cashup,
          payload
        });

        newCashup.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.initializePaymentMethodCashup(
          {
            terminalPayments,
            lastCashUpPayments,
            terminalIsSlave
          }
        );
      }
      return newCashup;
    }
  });

  OB.App.StateAPI.Cashup.initCashup.addActionPreparation(
    async (state, payload) => {
      const newPayload = { ...payload };

      if (OB.App.State.Cashup.Utils.isValidTheLocalCashup(state.Cashup)) {
        // init from local
        newPayload.initCashupFrom = 'local';
      } else {
        const backendCashupData = await OB.App.State.Cashup.Utils.requestNoProcessedCashupFromBackend();
        if (
          OB.App.State.Cashup.Utils.isValidTheBackendCashup(backendCashupData)
        ) {
          // init from backend
          newPayload.initCashupFrom = 'backend';
          // eslint-disable-next-line prefer-destructuring
          newPayload.currentCashupFromBackend = backendCashupData[0];
        } else {
          // init from scratch
          newPayload.initCashupFrom = 'scratch';
          const lastBackendCashupData = await OB.App.State.Cashup.Utils.requestProcessedCashupFromBackend();
          if (lastBackendCashupData[0]) {
            // payments from backend last cashup
            newPayload.lastCashUpPayments =
              lastBackendCashupData[0].cashPaymentMethodInfo;
          } else {
            if (lastBackendCashupData.exception) {
              // error reading payments of backend last cashup, show popup and reload
              OB.App.State.Cashup.Utils.showPopupNotEnoughDataInCache();
              // TODO throw ;
            }
            // is the first cashup of the terminal, initialize all payments to 0
            newPayload.lastCashUpPayments = null;
          }
        }
      }
      newPayload.newUuid = OB.UTIL.get_UUID();
      newPayload.creationDate = new Date().toISOString();
      newPayload.currentDate = new Date();
      newPayload.userId = OB.MobileApp.model.get('context').user.id;
      newPayload.posterminal = OB.MobileApp.model.get('terminal').id;
      newPayload.terminalIsSlave = OB.POS.modelterminal.get('terminal').isslave;

      newPayload.terminalPayments = [...OB.MobileApp.model.get('payments')];

      newPayload.terminalPayments.forEach(payment => {
        // eslint-disable-next-line no-param-reassign
        payment.newUuid = OB.UTIL.get_UUID();
      });

      return newPayload;
    }
  );

  /**
   * Update the cashup , after a ticket done
   */
  OB.App.StateAPI.Cashup.registerActions({
    updateCashup(cashup, payload) {
      const { tickets } = payload;
      const newCashup = { ...cashup };

      // TODO: update statistics

      tickets.forEach(order => {
        const orderType = order.get('orderType');

        let gross;
        let taxOrderType;
        let taxAmount;
        let amount;
        let precision;
        let netSales = OB.DEC.Zero;
        let grossSales = OB.DEC.Zero;
        let netReturns = OB.DEC.Zero;
        let grossReturns = OB.DEC.Zero;
        let taxSales = OB.DEC.Zero;
        let taxReturns = OB.DEC.Zero;
        let ctaxSales;
        const maxtaxSales = OB.DEC.Zero;
        let ctaxReturns;
        const maxtaxReturns = OB.DEC.Zero;
        if (
          !(order.has('isQuotation') && order.get('isQuotation')) &&
          !order.get('isPaid') &&
          // / PAYLOAD !!!!

          ((OB.MobileApp.model.get('terminal').countLayawayAsSales &&
            !(order.get('isLayaway') && !order.get('voidLayaway'))) ||
            (!OB.MobileApp.model.get('terminal').countLayawayAsSales &&
              order.isFullyPaid() &&
              (order.get('isLayaway') && !order.get('voidLayaway'))))
        ) {
          order.get('lines').models.forEach(line => {
            if (order.get('priceIncludesTax')) {
              gross = line.get('lineGrossAmount');
            } else {
              gross = line.get('discountedGross');
            }
            if (order.get('doCancelAndReplace')) {
              if (!line.get('replacedorderline')) {
                netSales = OB.DEC.add(netSales, line.get('net'));
                grossSales = OB.DEC.add(grossSales, gross);
              }
            } else if (order.get('cancelLayaway')) {
              // Cancel Layaway
              netSales = OB.DEC.add(netSales, line.get('net'));
              grossSales = OB.DEC.add(grossSales, gross);
            } else if (order.get('voidLayaway')) {
              // Void Layaway
              netSales = OB.DEC.add(netSales, -line.get('net'));
              grossSales = OB.DEC.add(grossSales, -gross);
            } else if (line.get('qty') > 0) {
              // Sales order: Positive line
              netSales = OB.DEC.add(netSales, line.get('net'));
              grossSales = OB.DEC.add(grossSales, gross);
            } else if (line.get('qty') < 0) {
              // Return from customer or Sales with return: Negative line
              netReturns = OB.DEC.add(netReturns, -line.get('net'));
              grossReturns = OB.DEC.add(grossReturns, -gross);
            }
          });
          newCashup.netSales = OB.DEC.add(newCashup.netSales, netSales);
          newCashup.grossSales = OB.DEC.add(newCashup.grossSales, grossSales);
          newCashup.netReturns = OB.DEC.add(newCashup.netReturns, netReturns);
          newCashup.grossReturns = OB.DEC.add(
            newCashup.grossReturns,
            grossReturns
          );
          newCashup.totalRetailTransactions = OB.DEC.sub(
            newCashup.grossSales,
            newCashup.grossReturns
          );

          // group and sum the taxes
          const newCashupTaxes = [];
          order.get('lines').each(function perLine(line) {
            const taxLines = line.get('taxLines');
            if (
              orderType === 1 ||
              (line.get('qty') < 0 &&
                !order.get('cancelLayaway') &&
                !order.get('voidLayaway'))
            ) {
              taxOrderType = '1';
            } else {
              taxOrderType = '0';
            }

            Object.values(taxLines).forEach(taxLine => {
              if (!(order.has('isQuotation') && order.get('isQuotation'))) {
                if (
                  order.get('cancelLayaway') ||
                  (line.get('qty') > 0 && !order.get('isLayaway'))
                ) {
                  taxAmount = taxLine.amount;
                } else if (
                  order.get('voidLayaway') ||
                  (line.get('qty') < 0 && !order.get('isLayaway'))
                ) {
                  taxAmount = -taxLine.amount;
                }
              }

              if (!OB.UTIL.isNullOrUndefined(taxAmount)) {
                newCashupTaxes.push({
                  taxName: taxLine.name,
                  taxAmount,
                  taxOrderType
                });
              }
            });
          });

          // Calculate adjustment taxes
          newCashupTaxes.forEach(t => {
            if (t.taxOrderType === '0') {
              // sale
              taxSales = OB.DEC.add(taxSales, t.taxAmount);
              if (t.taxAmount > maxtaxSales) {
                ctaxSales = t;
              }
            } else {
              // return
              taxReturns = OB.DEC.add(taxReturns, t.taxAmount);
              if (t.taxAmount > maxtaxReturns) {
                ctaxReturns = t;
              }
            }
          });

          // Do the adjustment
          if (ctaxSales) {
            ctaxSales.taxAmount = OB.DEC.add(
              ctaxSales.taxAmount,
              OB.DEC.sub(OB.DEC.sub(grossSales, netSales), taxSales)
            );
          }
          if (ctaxReturns) {
            ctaxReturns.taxAmount = OB.DEC.add(
              ctaxReturns.taxAmount,
              OB.DEC.sub(OB.DEC.sub(grossReturns, netReturns), taxReturns)
            );
          }

          // save the calculated taxes into the cashup
          newCashupTaxes.forEach(newCashupTax => {
            const cashupTax = newCashup.cashTaxInfo.filter(function filter(
              cashupTaxFilter
            ) {
              return (
                cashupTaxFilter.name === newCashupTax.taxName &&
                cashupTaxFilter.orderType === newCashupTax.taxOrderType
              );
            })[0];
            if (cashupTax) {
              cashupTax.amount = OB.DEC.add(
                cashupTax.amount,
                newCashupTax.taxAmount
              );
            } else {
              newCashup.cashTaxInfo.push({
                id: OB.UTIL.get_UUID(), // not totally pure, but acepted since cannot predict in the action preparation how many new uuid will be needed
                name: newCashupTax.taxName,
                amount: newCashupTax.taxAmount,
                orderType: newCashupTax.taxOrderType
              });
            }
          });

          // set all payment methods to not used in the trx
          newCashup.cashPaymentMethodInfo.forEach(paymentMethod => {
            if (paymentMethod.usedInCurrentTrx !== false) {
              // eslint-disable-next-line no-param-reassign
              paymentMethod.usedInCurrentTrx = false;
            }
          });

          order.get('payments').models.forEach(orderPayment => {
            const cashupPayment = newCashup.cashPaymentMethodInfo.filter(
              function filter(cashupPaymentMethodFilter) {
                return (
                  cashupPaymentMethodFilter.searchKey ===
                    orderPayment.get('kind') &&
                  !orderPayment.get('isPrePayment')
                );
              }
            )[0];
            if (!cashupPayment) {
              // We cannot find this payment in local database, it must be a new payment method, we skip it.
              return;
            }
            precision =
              OB.MobileApp.model.paymentnames[cashupPayment.searchKey]
                .obposPosprecision;
            amount = _.isNumber(orderPayment.get('amountRounded'))
              ? orderPayment.get('amountRounded')
              : orderPayment.get('amount');
            if (
              amount < 0 ||
              (orderPayment.has('paymentRounding') &&
                orderPayment.get('paymentRounding') &&
                orderPayment.get('isReturnOrder'))
            ) {
              cashupPayment.totalReturns = OB.DEC.sub(
                cashupPayment.totalReturns,
                amount,
                precision
              );
            } else {
              cashupPayment.totalSales = OB.DEC.add(
                cashupPayment.totalSales,
                amount,
                precision
              );
            }
            // set used in transaction payment methods to true
            cashupPayment.usedInCurrentTrx = true;
          });
        }
      });

      return newCashup;
    }
  });

  OB.App.StateAPI.Cashup.updateCashup.addActionPreparation(
    async (state, payload) => {
      const newPayload = { ...payload };
      const { tickets } = payload;

      newPayload.tickets = [];
      tickets.forEach(ticket => {
        if (!ticket.has('obposIsDeleted')) {
          newPayload.tickets.push(ticket);
        }
      });
      if (newPayload.tickets.lenth === 0) {
        throw new OB.App.Class.ActionCanceled('');
      }

      return newPayload;
    }
  );

  /**
   * Complete the cashup
   */
  OB.App.StateAPI.Global.registerActions({
    completeCashup(state, payload) {
      const newState = { ...state };

      // read cashup from cashup window,
      const cashupWindowCashup = payload.newCashup;

      const oldCashup = { ...newState.Cashup };

      const objToSend = JSON.parse(cashupWindowCashup.get('objToSend'));
      oldCashup.cashCloseInfo = objToSend.cashCloseInfo;
      oldCashup.cashMgmtIds = objToSend.cashMgmtIds;
      oldCashup.cashUpDate = objToSend.cashUpDate;
      oldCashup.timezoneOffset = objToSend.timezoneOffset;
      oldCashup.lastcashupeportdate = objToSend.lastcashupeportdate;
      oldCashup.approvals = objToSend.approvals;

      oldCashup.isprocessed = 'Y';

      // create new message with current cashup
      const newMessagePayload = {
        id: OB.UTIL.get_UUID(),
        terminal: payload.terminal,
        cacheSessionId: payload.cacheSessionId,
        data: [oldCashup]
      };
      const newMessage = OB.App.State.Messages.Utils.createNewMessage(
        'Cash Up',
        'org.openbravo.retail.posterminal.ProcessCashClose',
        newMessagePayload
      );
      newState.Messages = [...newState.Messages, newMessage];

      // initialize the new cashup
      let newCashup = {};
      const { terminalIsSlave, terminalPayments } = payload;

      OB.App.State.Cashup.Utils.resetStatistics();

      newCashup = OB.App.State.Cashup.Utils.createNewCashupFromScratch({
        newCashup,
        payload
      });

      const lastCashUpPayments = oldCashup.cashCloseInfo;

      newCashup.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.initializePaymentMethodCashup(
        {
          terminalPayments,
          lastCashUpPayments,
          terminalIsSlave
        }
      );

      newState.Cashup = newCashup;

      return newState;
    }
  });
  OB.App.StateAPI.Global.completeCashup.addActionPreparation(
    async (state, payload) => {
      const newPayload = { ...payload };

      newPayload.newUuid = OB.UTIL.get_UUID();
      newPayload.creationDate = new Date().toISOString();
      newPayload.currentDate = new Date();
      newPayload.userId = OB.MobileApp.model.get('context').user.id;
      newPayload.posterminal = OB.MobileApp.model.get('terminal').id;
      newPayload.terminalIsSlave = OB.POS.modelterminal.get('terminal').isslave;

      newPayload.terminalPayments = [...OB.MobileApp.model.get('payments')];

      newPayload.terminalPayments.forEach(payment => {
        // eslint-disable-next-line no-param-reassign
        payment.newUuid = OB.UTIL.get_UUID();
      });

      newPayload.terminal = OB.MobileApp.model.get(
        'logConfiguration'
      ).deviceIdentifier;
      newPayload.cacheSessionId = OB.UTIL.localStorage.getItem(
        'cacheSessionId'
      );

      return newPayload;
    }
  );
})();
