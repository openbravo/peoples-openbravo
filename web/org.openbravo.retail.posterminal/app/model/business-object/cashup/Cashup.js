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
    totalDeleteTickets: OB.DEC.Zero,
    totalCompleteTickets: OB.DEC.Zero,
    totalQuantityProducts: OB.DEC.Zero,
    totalAmount: OB.DEC.Zero,
    totalDiscountAmount: OB.DEC.Zero,
    users: [],
    productCategories: [],
    paymentMethods: []
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

  OB.App.StateAPI.registerIdentifierForMessages('OBPOS_CashUp', message => {
    return new Date(message.creationDate).toLocaleString();
  });

  OB.App.StateAPI.registerIdentifierForMessages(
    'OBPOS_CashManagment',
    message => {
      return `${message.type}: ${message.user} - ${new Date(
        message.creationDate
      ).toLocaleString()}`;
    }
  );
})();
