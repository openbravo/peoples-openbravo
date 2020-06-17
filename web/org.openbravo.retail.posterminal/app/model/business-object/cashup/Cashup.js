/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Defines the Cashup model
 */
(function CashupModelDefinition() {
  OB.App.StateAPI.registerModel('Cashup', {
    id: null,
    netSales: null,
    grossSales: null,
    netReturns: null,
    grossReturns: null,
    totalRetailTransactions: null,
    totalStartings: null,
    creationDate: null,
    userId: null,
    cashTaxInfo: [], // taxCashupId, name, amount, orderType
    cashPaymentMethodInfo: [], // paymentMethodCashupId, paymentMethodId, searchKey, name, startingCash, totalSales, totalReturns, totalDeposits, totalDrops, rate, isocode, newPaymentMethod
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

  OB.App.StateAPI.Cashup.registerActions({
    resetNewPayments(cashup) {
      const newCashup = { ...cashup };

      newCashup.cashPaymentMethodInfo = cashup.cashPaymentMethodInfo.map(
        payment => {
          return { ...payment, newPaymentMethod: false };
        }
      );

      return newCashup;
    }
  });
})();
