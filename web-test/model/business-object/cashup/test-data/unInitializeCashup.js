/*global module*/

module.exports = {
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
};
