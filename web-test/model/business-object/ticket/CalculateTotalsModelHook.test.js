/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */

OB = {
  App: {
    Class: {}
  },
  Discounts: {
    Pos: {
      ruleImpls: [],
      bpSets: []
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

global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3.min.js');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic.js');
OB.App.StateAPI.registerModel('Ticket');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/CalculateTotalsModelHook');

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
    ]
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
    ]
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
    qty: 2,
    lines: [
      {
        id: 'BB66D8D151964A8A39586FCD7D9A3941',
        qty: 2,
        grossUnitAmount: 540,
        netUnitAmount: 446.28,
        taxRate: 1.21,
        baseGrossUnitPrice: 300,
        grossUnitPrice: 270,
        netUnitPrice: 223.14,
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
    }
  },
  {
    id: '6FD3CDDDBB2A3805895853BB22F2E9F2',
    grossAmount: 459.8,
    netAmount: 380,
    priceIncludesTax: false,
    qty: 2,
    lines: [
      {
        id: 'BB66D8D151964A8A39586FCD7D9A3942',
        qty: 2,
        grossUnitAmount: 459.8,
        netUnitAmount: 380,
        taxRate: 1.21,
        baseNetUnitPrice: 200,
        grossUnitPrice: 229.9,
        netUnitPrice: 190,
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
    }
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
      ]
    };
    setDiscountsEngineResultAs({ lines: [] });
    setTaxesEngineResultAs({ lines: [] });
    const result = hook(deepfreeze(ticketWithNegativeLines), payload());
    expect(result.qty).toEqual(3);
  });

  it('Ticket with no lines results in qty 0', () => {
    const ticketWithNegativeLines = {
      id: '0',
      priceIncludesTax: true,
      lines: []
    };
    setDiscountsEngineResultAs({ lines: [] });
    setTaxesEngineResultAs({ lines: [] });
    const result = hook(deepfreeze(ticketWithNegativeLines), payload());
    expect(result.qty).toEqual(0);
  });
});
