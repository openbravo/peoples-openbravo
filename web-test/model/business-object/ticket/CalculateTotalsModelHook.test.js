/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */

require('./SetupTicket');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/CalculateTotalsModelHook');
require('./SetupTicketUtils');
const deepfreeze = require('deepfreeze');

global.OB = {
  ...global.OB,
  App: { ...global.OB.App, TerminalProperty: { get: jest.fn() } },
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

// test data
const tickets = [
  {
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
  },
  {
    id: '6FD3CDDDBB2A3805895853BB22F2E9F2',
    priceIncludesTax: false,
    lines: [
      {
        id: 'BB66D8D151964A8A39586FCD7D9A3942',
        qty: 2,
        baseNetUnitPrice: 200
      }
    ],
    payments: []
  }
];

const discountResults = [
  {
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
  },
  {
    lines: [
      {
        id: 'BB66D8D151964A8A39586FCD7D9A3942',
        netUnitAmount: 380,
        netUnitPrice: 190,
        discounts: [
          {
            ruleId: '08C2C89DB15443478B659A8645828F63',
            discountType: '20E4EC27397344309A2185097392D964',
            name: 'disc_5_ var_perc',
            applyNext: true,
            amt: 10,
            qtyOffer: 0,
            manual: true
          }
        ]
      }
    ]
  }
];

const taxesResults = [
  {
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
  },
  {
    id: '6FD3CDDDBB2A3805895853BB22F2E9F2',
    grossAmount: 459.8,
    netAmount: 380,
    taxes: {
      '5235D8E99A2749EFA17A5C92A52AEFC6': {
        id: '5235D8E99A2749EFA17A5C92A52AEFC6',
        net: 380,
        amount: 79.8,
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
        id: 'BB66D8D151964A8A39586FCD7D9A3942',
        grossUnitAmount: 459.8,
        netUnitAmount: 380,
        grossUnitPrice: 229.9,
        netUnitPrice: 190,
        qty: 2,
        tax: '5235D8E99A2749EFA17A5C92A52AEFC6',
        taxRate: 1.21,
        taxes: {
          '5235D8E99A2749EFA17A5C92A52AEFC6': {
            id: '5235D8E99A2749EFA17A5C92A52AEFC6',
            net: 380,
            amount: 79.8,
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
  }
];

const results = [
  {
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
  },
  {
    id: '6FD3CDDDBB2A3805895853BB22F2E9F2',
    grossAmount: 459.8,
    netAmount: 380,
    priceIncludesTax: false,
    isNegative: false,
    qty: 2,
    change: 0,
    payment: 0,
    paymentWithSign: 0,
    lines: [
      {
        id: 'BB66D8D151964A8A39586FCD7D9A3942',
        qty: 2,
        grossUnitAmount: 459.8,
        netUnitAmount: 380,
        netUnitAmountWithoutTicketDiscounts: 390,
        taxRate: 1.21,
        baseNetUnitPrice: 200,
        grossUnitPrice: 229.9,
        netUnitPrice: 190,
        netUnitPriceWithoutTicketDiscounts: 195,
        baseGrossUnitAmount: 0,
        baseNetUnitAmount: 400,
        promotions: [
          {
            ruleId: '08C2C89DB15443478B659A8645828F63',
            amt: 10,
            discountType: '20E4EC27397344309A2185097392D964',
            name: 'disc_5_ var_perc',
            manual: true,
            qtyOffer: 0,
            applyNext: true
          }
        ],
        tax: '5235D8E99A2749EFA17A5C92A52AEFC6',
        taxes: {
          '5235D8E99A2749EFA17A5C92A52AEFC6': {
            id: '5235D8E99A2749EFA17A5C92A52AEFC6',
            amount: 79.8,
            cascade: false,
            docTaxAmount: 'D',
            lineNo: 10,
            name: 'Entregas IVA 21%',
            net: 380,
            rate: 21,
            taxBase: null
          }
        }
      }
    ],
    taxes: {
      '5235D8E99A2749EFA17A5C92A52AEFC6': {
        id: '5235D8E99A2749EFA17A5C92A52AEFC6',
        amount: 79.8,
        cascade: false,
        docTaxAmount: 'D',
        lineNo: 10,
        name: 'Entregas IVA 21%',
        net: 380,
        rate: 21,
        taxBase: null
      }
    },
    payments: []
  }
];

describe('Apply Discounts and Taxes Model Hook', () => {
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

  test.each`
    index | ticket
    ${0}  | ${'ticket #0'}
    ${1}  | ${'ticket #1'}
  `('Calculate totals of $ticket', ({ index }) => {
    setDiscountsEngineResultAs(discountResults[index]);
    setTaxesEngineResultAs(taxesResults[index]);
    const result = hook(deepfreeze(tickets[index]), payload());
    expect(result).toEqual(results[index]);
  });

  it('Negative lines are not computed for the ticket qty', () => {
    const ticketWithNegativeLines = {
      id: '0',
      priceIncludesTax: true,
      lines: [
        {
          id: '1',
          qty: 1,
          baseGrossUnitPrice: 300
        },
        {
          id: '2',
          qty: -1,
          baseGrossUnitPrice: 100
        },
        {
          id: '3',
          qty: 2,
          baseGrossUnitPrice: 200
        },
        {
          id: '4',
          qty: -1,
          baseGrossUnitPrice: 50
        }
      ],
      payments: []
    };
    setDiscountsEngineResultAs({ lines: [] });
    setTaxesEngineResultAs({ grossAmount: 650, lines: [] });
    const result = hook(deepfreeze(ticketWithNegativeLines), payload());
    expect(result.qty).toEqual(3);
  });

  it('Ticket with no lines results in qty 0', () => {
    const ticketWithNegativeLines = {
      id: '0',
      priceIncludesTax: true,
      lines: [],
      payments: []
    };
    setDiscountsEngineResultAs({ lines: [] });
    setTaxesEngineResultAs({ grossAmount: 10, lines: [] });
    const result = hook(deepfreeze(ticketWithNegativeLines), payload());
    expect(result.qty).toEqual(0);
  });

  test.each`
    payloadTicket                                                                                                                                                                                             | discountsEngineResult | taxEngineResult                                                         | resultTicket
    ${{ id: '0', priceIncludesTax: true, lines: [{ id: '1', grossUnitPrice: 0, baseGrossUnitPrice: 100, qty: -1, quantity: 1, skipApplyPromotions: true, promotions: [{ amt: -100 }] }], payments: [] }}      | ${{ lines: [] }}      | ${{ grossAmount: 0, lines: [{ id: '1', grossUnitAmount: 0 }] }}         | ${{ id: '0', grossAmount: 0, lines: [{ id: '1', qty: -1, grossUnitAmount: 0, promotions: [{ amt: -100 }] }] }}
    ${{ id: '0', priceIncludesTax: true, lines: [{ id: '1', grossUnitPrice: 90, baseGrossUnitPrice: 100, qty: -1, quantity: 1, skipApplyPromotions: true, promotions: [{ amt: -10 }] }], payments: [] }}      | ${{ lines: [] }}      | ${{ grossAmount: -90, lines: [{ id: '1', grossUnitAmount: -90 }] }}     | ${{ id: '0', grossAmount: -90, lines: [{ id: '1', qty: -1, grossUnitAmount: -90, promotions: [{ amt: -10 }] }] }}
    ${{ id: '0', priceIncludesTax: true, lines: [{ id: '1', grossUnitPrice: 3.56, baseGrossUnitPrice: 3.95, qty: -2, quantity: 2, skipApplyPromotions: true, promotions: [{ amt: -0.79 }] }], payments: [] }} | ${{ lines: [] }}      | ${{ grossAmount: -7.11, lines: [{ id: '1', grossUnitAmount: -7.11 }] }} | ${{ id: '0', grossAmount: -7.11, lines: [{ id: '1', qty: -2, grossUnitAmount: -7.11, promotions: [{ amt: -0.79 }] }] }}
    ${{ id: '0', priceIncludesTax: false, lines: [{ id: '1', netUnitPrice: 3.56, baseNetUnitPrice: 3.95, qty: -2, quantity: 2, skipApplyPromotions: true, promotions: [{ amt: -0.79 }] }], payments: [] }}    | ${{ lines: [] }}      | ${{ grossAmount: -7.11, lines: [{ id: '1', netUnitAmount: -7.11 }] }}   | ${{ id: '0', grossAmount: -7.11, lines: [{ id: '1', qty: -2, netUnitAmount: -7.11, promotions: [{ amt: -0.79 }] }] }}
  `(
    'Skip calculate line gross amount if line skipApplyPromotions is true',
    ({
      payloadTicket,
      discountsEngineResult,
      taxEngineResult,
      resultTicket
    }) => {
      setDiscountsEngineResultAs(discountsEngineResult);
      OB.Taxes.Pos.translateTaxes = jest.fn().mockReturnValue(taxEngineResult);
      OB.Taxes.Pos.applyTaxes = jest.fn().mockImplementation(ticket => {
        expect(ticket.lines).toMatchObject(resultTicket.lines);
        return taxEngineResult;
      });

      const result = hook(deepfreeze(payloadTicket), payload());
      expect(result).toMatchObject(resultTicket);
    }
  );

  it('Skip tax calculation if ticket has skipTaxCalculation flag', () => {
    const ticket = {
      id: '0',
      priceIncludesTax: true,
      skipTaxCalculation: true,
      grossAmount: 100,
      lines: [
        {
          id: '1',
          baseGrossUnitPrice: 100,
          qty: 1,
          quantity: 1,
          tax: undefined,
          taxRate: undefined
        }
      ],
      payments: [],
      taxes: {}
    };
    setDiscountsEngineResultAs({ lines: [] });
    setTaxesEngineResultAs({
      grossAmount: 10,
      lines: [
        {
          id: '1',
          grossUnitAmount: 100,
          tax: '1',
          taxRate: 1,
          taxes: { '1': { id: '1' } }
        }
      ]
    });
    const result = hook(deepfreeze(ticket), payload());
    expect(result).toMatchObject(ticket);
  });
});
