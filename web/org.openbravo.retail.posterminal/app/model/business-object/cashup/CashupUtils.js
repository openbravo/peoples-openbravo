/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global lodash */

/**
 * @fileoverview Define utility functions for the Cashup Model
 */
(function CashupUtilsDefinition() {
  OB.App.StateAPI.Cashup.registerUtilityFunctions({
    isValidTheLocalCashup(cashup) {
      return cashup.id != null;
    },

    async requestNoProcessedCashupFromBackend() {
      const response = await OB.App.Request.mobileServiceRequest(
        'org.openbravo.retail.posterminal.master.Cashup',
        { isprocessed: 'N', isprocessedbo: 'N' }
      );
      return response.response;
    },

    isValidTheBackendCashup(data) {
      if (data && data.exception) {
        throw new Error(data.exception);
      } else if (data && Array.isArray(data) && data.length > 0 && data[0]) {
        return true;
      } else {
        return false;
      }
    },

    async requestProcessedCashupFromBackend() {
      const response = await OB.App.Request.mobileServiceRequest(
        'org.openbravo.retail.posterminal.master.Cashup',
        { isprocessed: 'Y' }
      );
      return response.response;
    },

    getTaxesFromBackendObject(backendTaxes) {
      const taxes = [];
      backendTaxes.forEach(backendTax => {
        taxes.push({
          id: backendTax.id,
          orderType: backendTax.orderType,
          name: backendTax.name,
          amount: backendTax.amount
        });
      });
      return taxes;
    },

    createNewCashupFromScratch(payload) {
      const { cashup } = payload;
      const newCashup = { ...cashup };

      newCashup.id = OB.App.UUID.generate();
      newCashup.netSales = OB.DEC.Zero;
      newCashup.grossSales = OB.DEC.Zero;
      newCashup.netReturns = OB.DEC.Zero;
      newCashup.grossReturns = OB.DEC.Zero;
      newCashup.totalRetailTransactions = OB.DEC.Zero;
      newCashup.totalStartings = OB.DEC.Zero;
      newCashup.creationDate = payload.payload.currentDate.toISOString();
      newCashup.userId = payload.payload.userId;
      newCashup.posterminal = payload.payload.posterminal;
      newCashup.trxOrganization = payload.payload.organization;
      newCashup.isprocessed = false;
      newCashup.cashTaxInfo = [];
      newCashup.cashCloseInfo = [];
      newCashup.totalDeleteTickets = OB.DEC.Zero;
      newCashup.totalCompleteTickets = OB.DEC.Zero;
      newCashup.totalQuantityProducts = OB.DEC.Zero;
      newCashup.totalAmount = OB.DEC.Zero;
      newCashup.totalDiscountAmount = OB.DEC.Zero;
      newCashup.users = [];
      newCashup.productCategories = [];
      newCashup.paymentMethods = [];

      return newCashup;
    },

    createNewCashupFromBackend(payload) {
      const { cashup, currentCashupFromBackend } = payload;
      const newCashup = { ...cashup };

      newCashup.id = currentCashupFromBackend.id;
      newCashup.netSales = currentCashupFromBackend.netSales;
      newCashup.grossSales = currentCashupFromBackend.grossSales;
      newCashup.netReturns = currentCashupFromBackend.netReturns;
      newCashup.grossReturns = currentCashupFromBackend.grossReturns;
      newCashup.totalRetailTransactions =
        currentCashupFromBackend.totalRetailTransactions;
      newCashup.totalStartings = OB.DEC.Zero;
      newCashup.creationDate = currentCashupFromBackend.creationDate;
      newCashup.userId = currentCashupFromBackend.userId;
      newCashup.posterminal = currentCashupFromBackend.posterminal;
      newCashup.trxOrganization = currentCashupFromBackend.organization;
      newCashup.isprocessed = currentCashupFromBackend.isprocessed;
      newCashup.cashTaxInfo = OB.App.State.Cashup.Utils.getTaxesFromBackendObject(
        currentCashupFromBackend.cashTaxInfo
      );
      newCashup.cashCloseInfo = currentCashupFromBackend.cashCloseInfo;

      // TODO: Obtain the value to the backend
      newCashup.totalDeleteTickets = OB.DEC.Zero;
      newCashup.totalCompleteTickets = OB.DEC.Zero;
      newCashup.totalQuantityProducts = OB.DEC.Zero;
      newCashup.totalAmount = OB.DEC.Zero;
      newCashup.totalDiscountAmount = OB.DEC.Zero;
      newCashup.users = [];
      newCashup.productCategories = [];
      newCashup.paymentMethods = [];

      return newCashup;
    },

    resetStatisticsIncludedInCashup() {
      OB.UTIL.localStorage.setItem('transitionsToOnline', 0);

      OB.UTIL.localStorage.setItem('logclientErrors', 0);

      OB.UTIL.localStorage.setItem('totalLatencyTime', 0);
      OB.UTIL.localStorage.setItem('totalLatencyMeasures', 0);
      OB.UTIL.localStorage.setItem('totalUploadBandwidth', 0);
      OB.UTIL.localStorage.setItem('totalUploadMeasures', 0);
      OB.UTIL.localStorage.setItem('totalDownloadBandwidth', 0);
      OB.UTIL.localStorage.setItem('totalDownloadMeasures', 0);
    },

    createMissingStatisticsIncludedInCashup() {
      // when doing initCashup:
      // - from local: it will do nothing
      // - from backend: it will create the varaibles
      // - from scratch: it will create the variables
      if (!OB.UTIL.localStorage.getItem('transitionsToOnline')) {
        OB.UTIL.localStorage.setItem('transitionsToOnline', 0);
      }

      if (!OB.UTIL.localStorage.getItem('logclientErrors')) {
        OB.UTIL.localStorage.setItem('logclientErrors', 0);
      }

      if (!OB.UTIL.localStorage.getItem('totalLatencyTime')) {
        OB.UTIL.localStorage.setItem('totalLatencyTime', 0);
      }
      if (!OB.UTIL.localStorage.getItem('totalLatencyMeasures')) {
        OB.UTIL.localStorage.setItem('totalLatencyMeasures', 0);
      }
      if (!OB.UTIL.localStorage.getItem('totalUploadBandwidth')) {
        OB.UTIL.localStorage.setItem('totalUploadBandwidth', 0);
      }
      if (!OB.UTIL.localStorage.getItem('totalUploadMeasures')) {
        OB.UTIL.localStorage.setItem('totalUploadMeasures', 0);
      }
      if (!OB.UTIL.localStorage.getItem('totalDownloadBandwidth')) {
        OB.UTIL.localStorage.setItem('totalDownloadBandwidth', 0);
      }
      if (!OB.UTIL.localStorage.getItem('totalDownloadMeasures')) {
        OB.UTIL.localStorage.setItem('totalDownloadMeasures', 0);
      }
    },

    getStatisticsToIncludeInCashup() {
      return {
        transitionsToOnline: OB.UTIL.localStorage.getItem(
          'transitionsToOnline'
        ),
        logclientErrors: OB.UTIL.localStorage.getItem('logclientErrors'),
        averageLatency:
          parseInt(OB.UTIL.localStorage.getItem('totalLatencyTime'), 10) /
          parseInt(OB.UTIL.localStorage.getItem('totalLatencyMeasures'), 10),
        averageUploadBandwidth:
          parseInt(OB.UTIL.localStorage.getItem('totalUploadBandwidth'), 10) /
          parseInt(OB.UTIL.localStorage.getItem('totalUploadMeasures'), 10),
        averageDownloadBandwidth:
          parseInt(OB.UTIL.localStorage.getItem('totalDownloadBandwidth'), 10) /
          parseInt(OB.UTIL.localStorage.getItem('totalDownloadMeasures'), 10)
      };
    },

    getCashupFilteredForSendToBackendInEachTicket(payload) {
      const { terminalPayments, cashup, statisticsToIncludeInCashup } = payload;

      const cashupToSend = { ...cashup, ...statisticsToIncludeInCashup };
      const cashupPayments = cashupToSend.cashPaymentMethodInfo;

      cashupToSend.cashPaymentMethodInfo = OB.App.State.Cashup.Utils.getCashupPaymentsThatAreAlsoInTerminalPayments(
        cashupPayments,
        terminalPayments
      );

      return cashupToSend;
    },

    countTicketInCashup(cashup, ticket, countLayawayAsSales, terminalPayments) {
      const newCashup = { ...cashup };

      const { orderType } = ticket;

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
        !ticket.isQuotation &&
        !ticket.isPaid &&
        ((countLayawayAsSales && !(ticket.isLayaway && !ticket.voidLayaway)) ||
          (!countLayawayAsSales &&
            !ticket.cancelLayaway &&
            (ticket.payOnCredit || ticket.completeTicket)))
      ) {
        ticket.lines.forEach(line => {
          const gross = line.grossUnitAmount;
          const net = line.netUnitAmount;
          if (ticket.doCancelAndReplace) {
            if (!line.replacedorderline) {
              netSales = OB.DEC.add(netSales, net);
              grossSales = OB.DEC.add(grossSales, gross);
            }
          } else if (ticket.cancelLayaway) {
            // Cancel Layaway
            netSales = OB.DEC.add(netSales, net);
            grossSales = OB.DEC.add(grossSales, gross);
          } else if (ticket.voidLayaway) {
            // Void Layaway
            netSales = OB.DEC.add(netSales, -net);
            grossSales = OB.DEC.add(grossSales, -gross);
          } else if (line.qty > 0) {
            // Sales order: Positive line
            netSales = OB.DEC.add(netSales, net);
            grossSales = OB.DEC.add(grossSales, gross);
          } else if (line.qty < 0) {
            // Return from customer or Sales with return: Negative line
            netReturns = OB.DEC.add(netReturns, -net);
            grossReturns = OB.DEC.add(grossReturns, -gross);
          }
        });
      }
      newCashup.netSales = OB.DEC.add(newCashup.netSales, netSales);
      newCashup.grossSales = OB.DEC.add(newCashup.grossSales, grossSales);
      newCashup.netReturns = OB.DEC.add(newCashup.netReturns, netReturns);
      newCashup.grossReturns = OB.DEC.add(newCashup.grossReturns, grossReturns);
      newCashup.totalRetailTransactions = OB.DEC.sub(
        newCashup.grossSales,
        newCashup.grossReturns
      );

      // group and sum the taxes
      const newCashupTaxes = [];
      ticket.lines.forEach(line => {
        const { taxes } = line;
        if (
          orderType === 1 ||
          (line.qty < 0 && !ticket.cancelLayaway && !ticket.voidLayaway)
        ) {
          taxOrderType = '1';
        } else {
          taxOrderType = '0';
        }

        Object.values(taxes).forEach(taxLine => {
          if (!ticket.isQuotation) {
            if (ticket.cancelLayaway || (line.qty > 0 && !ticket.isLayaway)) {
              taxAmount = taxLine.amount;
            } else if (
              ticket.voidLayaway ||
              (line.qty < 0 && !ticket.isLayaway)
            ) {
              taxAmount = -taxLine.amount;
            }
          }

          if (taxAmount != null) {
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

      if (newCashup.cashTaxInfo) {
        newCashup.cashTaxInfo = [...newCashup.cashTaxInfo];
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
          newCashup.cashTaxInfo = newCashup.cashTaxInfo.map(cashupTaxMap => {
            if (cashupTaxMap.id === cashupTax.id) {
              const newCashupTaxMap = { ...cashupTaxMap };
              newCashupTaxMap.amount = OB.DEC.add(
                newCashupTaxMap.amount,
                newCashupTax.taxAmount
              );
              return newCashupTaxMap;
            }
            return cashupTaxMap;
          });
        } else {
          newCashup.cashTaxInfo.push({
            id: OB.App.UUID.generate(),
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

      ticket.payments.forEach(orderPayment => {
        const cashupPayment = newCashup.cashPaymentMethodInfo.filter(
          cashupPaymentMethodFilter => {
            return (
              cashupPaymentMethodFilter.searchKey === orderPayment.kind &&
              !orderPayment.isPrePayment
            );
          }
        )[0];
        if (!cashupPayment) {
          // We cannot find this payment in local database, it must be a new payment method, we skip it.
          return;
        }
        precision = terminalPayments.find(
          paymentType =>
            paymentType.payment.searchKey === cashupPayment.searchKey
        ).obposPosprecision;
        amount = lodash.isNumber(orderPayment.amountRounded)
          ? orderPayment.amountRounded
          : orderPayment.amount;
        if (amount < 0) {
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
        if (orderPayment.countPerAmount) {
          cashupPayment.countPerAmount = cashupPayment.countPerAmount || {};
          Object.keys(orderPayment.countPerAmount).forEach(key => {
            const entryAmount = Number(key);
            const currentCount = cashupPayment.countPerAmount[key] || 0;
            const updatedCount =
              entryAmount > 0
                ? currentCount + orderPayment.countPerAmount[key]
                : currentCount - orderPayment.countPerAmount[key];
            cashupPayment.countPerAmount[key] = updatedCount;
          });
        }
        // set used in transaction payment methods to true
        cashupPayment.usedInCurrentTrx = true;
      });

      // Save the date of the first ticket included in the cashup
      newCashup.initialTicketDate =
        newCashup.initialTicketDate || ticket.creationDate;

      return newCashup;
    },

    updateCashupFromTicket(ticket, cashup, payload) {
      // update cashup
      const newCashup = OB.App.State.Cashup.Utils.countTicketInCashup(
        cashup,
        ticket,
        payload.terminal.countLayawayAsSales,
        payload.payments
      );

      // insert cashup in the ticket
      const newTicket = {
        ...ticket,
        obposAppCashup:
          ticket.isPaid && !ticket.isPaymentModified
            ? ticket.obposAppCashup
            : newCashup.id,
        cashUpReportInformation: OB.App.State.Cashup.Utils.getCashupFilteredForSendToBackendInEachTicket(
          {
            cashup: newCashup,
            terminalPayments: payload.payments,
            statisticsToIncludeInCashup: payload.statisticsToIncludeInCashup
          }
        )
      };

      return { ticket: newTicket, cashup: newCashup };
    }
  });
})();
