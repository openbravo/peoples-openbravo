/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */

global.OB = {
  App: {
    Class: {},
    TerminalProperty: { get: jest.fn() }
  },
  Discounts: {
    Pos: {
      ruleImpls: [],
      bpSets: [],
      getTicketDiscountsDiscountTypeIds: () => {
        return [];
      }
    }
  },
  Format: {
    formats: { qtyEdition: '#0.###' }
  },
  Taxes: {
    Pos: {
      ruleImpls: []
    }
  }
};

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3.min.js');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic.js');
OB.App.StateAPI.registerModel('Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/CalculateTotalsModelHook');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/AddPayment');

// set Ticket model "calculateTotals" utility function
OB.App.State = {
  Ticket: {
    Utils: {
      calculateTotals: OB.App.StateAPI.Ticket.utilities
        .filter(util => util.functionName === 'calculateTotals')
        .map(util => util.implementation)
        .pop()
    }
  }
};

OB.App.TerminalProperty = {
  get: jest.fn().mockImplementation(property => {
    if (property === 'payments') {
      return [
        {
          currencySymbolAtTheRight: true,
          isocode: 'EUR',
          mulrate: '1.000000000000',
          obposPosprecision: 2,
          payment: { searchKey: 'OBPOS_payment.cash' },
          paymentMethod: { iscash: true },
          rate: '1'
        }
      ];
    }
    return {};
  })
};

// test data
const ticket = {
  id: '6FD3CDDDBB2A3805895853BB22F2E9F1',
  priceIncludesTax: true,
  lines: [
    {
      id: 'BB66D8D151964A8A39586FCD7D9A3941',
      qty: 2,
      baseGrossUnitPrice: 300
    }
  ],
  payments: []
};
const discountResults = {
  lines: [
    {
      id: 'BB66D8D151964A8A39586FCD7D9A3941',
      grossUnitAmount: 540,
      grossUnitPrice: 270,
      discounts: [
        {
          ruleId: 'C26B841C84B14FE2AB1A334DD3672E87',
          discountType: '697A7AB9FD9C4EE0A3E891D3D3CCA0A7',
          name: 'GPS_10_per',
          applyNext: true,
          amt: 30,
          qtyOffer: 1
        }
      ]
    }
  ]
};
const taxesResults = {
  id: '6FD3CDDDBB2A3805895853BB22F2E9F1',
  grossAmount: 540,
  netAmount: 446.28,
  taxes: {
    '5235D8E99A2749EFA17A5C92A52AEFC6': {
      id: '5235D8E99A2749EFA17A5C92A52AEFC6',
      net: 446.28,
      amount: 93.72,
      name: 'Entregas IVA 21%',
      docTaxAmount: 'D',
      rate: 21,
      taxBase: null,
      cascade: false,
      lineNo: 10
    }
  },
  lines: [
    {
      id: 'BB66D8D151964A8A39586FCD7D9A3941',
      grossUnitAmount: 540,
      netUnitAmount: 446.28,
      grossUnitPrice: 270,
      netUnitPrice: 223.14,
      qty: 2,
      tax: '5235D8E99A2749EFA17A5C92A52AEFC6',
      taxRate: 1.21,
      taxes: {
        '5235D8E99A2749EFA17A5C92A52AEFC6': {
          id: '5235D8E99A2749EFA17A5C92A52AEFC6',
          net: 446.28,
          amount: 93.72,
          name: 'Entregas IVA 21%',
          docTaxAmount: 'D',
          rate: 21,
          taxBase: null,
          cascade: false,
          lineNo: 10
        }
      }
    }
  ]
};
const result = {
  id: '6FD3CDDDBB2A3805895853BB22F2E9F1',
  grossAmount: 540,
  netAmount: 446.28,
  priceIncludesTax: true,
  isNegative: false,
  qty: 2,
  change: 0,
  payment: 0,
  paymentWithSign: 0,
  lines: [
    {
      id: 'BB66D8D151964A8A39586FCD7D9A3941',
      qty: 2,
      grossUnitAmount: 540,
      grossUnitAmountWithoutTicketDiscounts: 570,
      netUnitAmount: 446.28,
      taxRate: 1.21,
      baseGrossUnitPrice: 300,
      grossUnitPrice: 270,
      grossUnitPriceWithoutTicketDiscounts: 285,
      netUnitPrice: 223.14,
      baseGrossUnitAmount: 600,
      baseNetUnitAmount: 0,
      promotions: [
        {
          ruleId: 'C26B841C84B14FE2AB1A334DD3672E87',
          amt: 30,
          discountType: '697A7AB9FD9C4EE0A3E891D3D3CCA0A7',
          name: 'GPS_10_per',
          qtyOffer: 1,
          applyNext: true
        }
      ],
      tax: '5235D8E99A2749EFA17A5C92A52AEFC6',
      taxes: {
        '5235D8E99A2749EFA17A5C92A52AEFC6': {
          id: '5235D8E99A2749EFA17A5C92A52AEFC6',
          amount: 93.72,
          cascade: false,
          docTaxAmount: 'D',
          lineNo: 10,
          name: 'Entregas IVA 21%',
          net: 446.28,
          rate: 21,
          taxBase: null
        }
      }
    }
  ],
  taxes: {
    '5235D8E99A2749EFA17A5C92A52AEFC6': {
      id: '5235D8E99A2749EFA17A5C92A52AEFC6',
      amount: 93.72,
      cascade: false,
      docTaxAmount: 'D',
      lineNo: 10,
      name: 'Entregas IVA 21%',
      net: 446.28,
      rate: 21,
      taxBase: null
    }
  },
  payments: []
};
const result2 = {
  id: '6FD3CDDDBB2A3805895853BB22F2E9F1',
  grossAmount: 540,
  netAmount: 446.28,
  isEditable: false,
  priceIncludesTax: true,
  isNegative: false,
  qty: 2,
  change: 0,
  payment: 10,
  paymentWithSign: 10,
  lines: [
    {
      id: 'BB66D8D151964A8A39586FCD7D9A3941',
      qty: 2,
      grossUnitAmount: 540,
      grossUnitAmountWithoutTicketDiscounts: 570,
      netUnitAmount: 446.28,
      taxRate: 1.21,
      baseGrossUnitPrice: 300,
      grossUnitPrice: 270,
      grossUnitPriceWithoutTicketDiscounts: 285,
      netUnitPrice: 223.14,
      baseGrossUnitAmount: 600,
      baseNetUnitAmount: 0,
      promotions: [
        {
          ruleId: 'C26B841C84B14FE2AB1A334DD3672E87',
          amt: 30,
          discountType: '697A7AB9FD9C4EE0A3E891D3D3CCA0A7',
          name: 'GPS_10_per',
          qtyOffer: 1,
          applyNext: true
        }
      ],
      tax: '5235D8E99A2749EFA17A5C92A52AEFC6',
      taxes: {
        '5235D8E99A2749EFA17A5C92A52AEFC6': {
          id: '5235D8E99A2749EFA17A5C92A52AEFC6',
          amount: 93.72,
          cascade: false,
          docTaxAmount: 'D',
          lineNo: 10,
          name: 'Entregas IVA 21%',
          net: 446.28,
          rate: 21,
          taxBase: null
        }
      }
    }
  ],
  taxes: {
    '5235D8E99A2749EFA17A5C92A52AEFC6': {
      id: '5235D8E99A2749EFA17A5C92A52AEFC6',
      amount: 93.72,
      cascade: false,
      docTaxAmount: 'D',
      lineNo: 10,
      name: 'Entregas IVA 21%',
      net: 446.28,
      rate: 21,
      taxBase: null
    }
  },
  receiptTaxes: [result.taxes['5235D8E99A2749EFA17A5C92A52AEFC6']],
  payments: [
    {
      amount: 10,
      paid: 10,
      origAmount: 10,
      kind: 'OBPOS_payment.cash',
      precision: 2
    }
  ]
};

describe('Add payment on completed ticket with discount', () => {
  const setDiscountsEngineResultAs = obj => {
    OB.Discounts.Pos.applyDiscounts = jest.fn().mockReturnValue(obj);
  };

  const setTaxesEngineResultAs = obj => {
    OB.Taxes.Pos.translateTaxes = jest.fn().mockReturnValue(obj);
    OB.Taxes.Pos.applyTaxes = jest.fn().mockReturnValue(obj);
  };

  let hook;
  let payload;

  beforeAll(() => {
    const modelHook = OB.App.StateAPI.Ticket.modelHooks[0];
    hook = modelHook.hook;
    payload = modelHook.generatePayload;
  });

  it('add payment on partial paid ticket with discount', () => {
    setDiscountsEngineResultAs(discountResults);
    setTaxesEngineResultAs(taxesResults);
    const expectedResult = hook(deepfreeze(ticket), payload());
    expect(expectedResult).toEqual(result);

    const nonEditableTicketWitPayment = {
      ...ticket,
      isEditable: false,
      payment: 10,
      payments: [
        {
          amount: 10,
          paid: 10,
          origAmount: 10,
          kind: 'OBPOS_payment.cash',
          precision: 2
        }
      ],
      receiptTaxes: [result.taxes['5235D8E99A2749EFA17A5C92A52AEFC6']]
    };
    nonEditableTicketWitPayment.lines = [...result.lines];

    setDiscountsEngineResultAs({
      lines: []
    });

    const expectedResult2 = hook(
      deepfreeze(nonEditableTicketWitPayment),
      payload()
    );

    expect(expectedResult2).toEqual(result2);
  });
});
