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
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/ApplyDiscountsAndTaxesModelHook');

describe('Apply Discounts and Taxes Model Hook', () => {
  const ticket = {
    id: '6FD3CDDDBB2A3805895853BB22F2E9F7',
    qty: 1,
    priceIncludesTax: true,
    lines: [
      {
        id: 'BB66D8D151964A8A39586FCD7D9A394D',
        qty: 1,
        price: 300,
        pricenet: 223.14
      }
    ]
  };

  const discountResult = {
    lines: {
      BB66D8D151964A8A39586FCD7D9A394D: {
        discounts: {
          finalLinePrice: 270,
          finalUnitPrice: 270,
          promotions: [
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
      }
    }
  };

  const taxesResult = {
    header: {
      id: '6FD3CDDDBB2A3805895853BB22F2E9F7',
      grossAmount: 270,
      netAmount: 223.14,
      taxes: {
        '5235D8E99A2749EFA17A5C92A52AEFC6': {
          id: '5235D8E99A2749EFA17A5C92A52AEFC6',
          net: 223.14,
          amount: 46.86,
          name: 'Entregas IVA 21%',
          docTaxAmount: 'D',
          rate: 21,
          taxBase: null,
          cascade: false,
          lineNo: 10
        }
      }
    },
    lines: [
      {
        id: 'BB66D8D151964A8A39586FCD7D9A394D',
        grossAmount: 270,
        netAmount: 223.14,
        grossPrice: 270,
        netPrice: 223.14,
        qty: 1,
        tax: '5235D8E99A2749EFA17A5C92A52AEFC6',
        taxRate: 1.21,
        taxes: {
          '5235D8E99A2749EFA17A5C92A52AEFC6': {
            id: '5235D8E99A2749EFA17A5C92A52AEFC6',
            net: 223.14,
            amount: 46.86,
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

  it('apply discounts and taxes', () => {
    setDiscountsEngineResultAs(discountResult);
    setTaxesEngineResultAs(taxesResult);

    const result = hook(deepfreeze(ticket), payload());

    expect(result).toMatchObject({
      id: '6FD3CDDDBB2A3805895853BB22F2E9F7',
      grossAmount: 270,
      netAmount: 223.14,
      priceIncludesTax: true,
      qty: 1,
      lines: [
        {
          id: 'BB66D8D151964A8A39586FCD7D9A394D',
          qty: 1,
          discountedGrossAmount: 270,
          discountedNetAmount: 223.14,
          taxRate: 1.21,
          netAmount: 223.14,
          price: 300,
          netPrice: 223.14,
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
              amount: 46.86,
              cascade: false,
              docTaxAmount: 'D',
              lineNo: 10,
              name: 'Entregas IVA 21%',
              net: 223.14,
              rate: 21,
              taxBase: null
            }
          }
        }
      ],
      taxes: {
        '5235D8E99A2749EFA17A5C92A52AEFC6': {
          id: '5235D8E99A2749EFA17A5C92A52AEFC6',
          amount: 46.86,
          cascade: false,
          docTaxAmount: 'D',
          lineNo: 10,
          name: 'Entregas IVA 21%',
          net: 223.14,
          rate: 21,
          taxBase: null
        }
      }
    });
  });
});
