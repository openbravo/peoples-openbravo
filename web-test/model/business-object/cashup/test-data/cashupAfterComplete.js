/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global module*/

module.exports = {
  id: 'FDAB894083D7A22D971A42AF3C63EBF3',
  netSales: 0,
  grossSales: 0,
  netReturns: 0,
  grossReturns: 0,
  totalRetailTransactions: 0,
  totalStartings: 250,
  creationDate: '2020-06-26T14:36:53.173Z',
  userId: '3073EDF96A3C42CC86C7069E379522D2',
  posterminal: '9104513C2D0741D4850AE8493998A7C8',
  isprocessed: false,
  cashTaxInfo: [],
  cashCloseInfo: [],
  cashPaymentMethodInfo: [
    {
      id: '21A3F0739E4DA84D1F31FEF0281B1C23',
      paymentMethodId: '5EA2A7DEBB2A49A69550C7E3D8899ED5',
      searchKey: 'OBPOS_payment.card',
      name: 'Card',
      startingCash: 0,
      totalSales: 0,
      totalReturns: 0,
      totalDeposits: 0,
      totalDrops: 0,
      rate: '1',
      isocode: 'EUR',
      newPaymentMethod: true,
      cashManagements: []
    },
    {
      id: '9CC37F3EDAE5F37D55D37F783A2AE5E1',
      paymentMethodId: '63339A82A49A4AE0BCD9AC5929B0EA3B',
      searchKey: 'OBPOS_payment.cash',
      name: 'Cash',
      startingCash: 200,
      totalSales: 0,
      totalReturns: 0,
      totalDeposits: 0,
      totalDrops: 0,
      rate: '1',
      isocode: 'EUR',
      newPaymentMethod: true,
      cashManagements: []
    },
    {
      id: '582637FC4A84DBC7F3E3BCBEA385BF30',
      paymentMethodId: 'E11EBCB5CF0442618B72B903DCB6A036',
      searchKey: 'OBPOS.payment.usacash',
      name: 'USA Cash',
      startingCash: 50,
      totalSales: 0,
      totalReturns: 0,
      totalDeposits: 0,
      totalDrops: 0,
      rate: '0.76082',
      isocode: 'USD',
      newPaymentMethod: true,
      cashManagements: []
    },
    {
      id: '718C1EC40B8A629F4DA732C46DB2709E',
      paymentMethodId: '6E98C4DE459748BE997693E9ED956D21',
      searchKey: 'OBPOS_payment.voucher',
      name: 'Voucher',
      startingCash: 0,
      totalSales: 0,
      totalReturns: 0,
      totalDeposits: 0,
      totalDrops: 0,
      rate: '1',
      isocode: 'EUR',
      newPaymentMethod: true,
      cashManagements: []
    }
  ]
};
