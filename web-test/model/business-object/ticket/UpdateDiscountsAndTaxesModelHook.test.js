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
      ruleImpls: []
    }
  }
};

global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/deepfreeze-2.0.0');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
OB.App.StateAPI.registerModel('Ticket');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/UpdateDiscountsAndTaxesModelHook');

describe('Update Discounts and Taxes Model Hook', () => {
  const ticket = {
    id: '6FD3CDDDBB2A3805895853BB22F2E9F7',
    lines: [
      {
        id: '180FF6C6EAD05641890886CAF750A281',
        promotions: []
      },
      {
        id: '5620C148C3059E0EA2C6C7DC6E0C22B4',
        promotions: []
      }
    ],
    orderManualPromotions: [],
    discountsFromUser: {}
  };

  let hook;
  let generatePayload;

  beforeAll(() => {
    const modelHook = OB.App.StateAPI.Ticket.modelHooks[0];
    hook = modelHook.hook;
    generatePayload = modelHook.generatePayload;
  });

  it('inserts discounts in ticket', () => {
    setDiscountsEngineResultAs({
      lines: {
        '5620C148C3059E0EA2C6C7DC6E0C22B4': {
          discounts: {
            finalLinePrice: 145.93,
            finalUnitPrice: 145.93,
            promotions: [
              {
                ruleId: '4A73666C74B045BCBEE9DD43BF163F81',
                discountType: '5D4BAF6BB86D4D2C9ED3D5A6FC051579',
                name: 'Bivy bag 27%',
                applyNext: true,
                amt: 53.97,
                qtyOffer: 1,
                chunks: 1
              }
            ]
          }
        }
      }
    });

    const result = executeHookForTicket(deepfreeze(ticket));

    expect(result).toEqual({
      id: '6FD3CDDDBB2A3805895853BB22F2E9F7',
      lines: [
        {
          id: '180FF6C6EAD05641890886CAF750A281',
          promotions: []
        },
        {
          id: '5620C148C3059E0EA2C6C7DC6E0C22B4',
          promotions: [
            {
              ruleId: '4A73666C74B045BCBEE9DD43BF163F81',
              discountType: '5D4BAF6BB86D4D2C9ED3D5A6FC051579',
              name: 'Bivy bag 27%',
              applyNext: true,
              amt: 53.97,
              qtyOffer: 1,
              chunks: 1,
              calculatedOnDiscountEngine: true,
              obdiscQtyoffer: 1,
              displayedTotalAmount: 53.97,
              fullAmt: 53.97,
              actualAmt: 53.97
            }
          ]
        }
      ],
      orderManualPromotions: [],
      discountsFromUser: {}
    });
  });

  it('inserts manual discounts', () => {
    const ticketWithDiscounts = { ...ticket };
    ticketWithDiscounts.discountsFromUser = { ...ticket.discountsFromUser };
    ticketWithDiscounts.discountsFromUser.manualPromotions = [
      {
        _identifier: 'disc_fixed_5_euro',
        id: 'AE60075707B34A9D8CEE4D828326CFC0',
        client$_identifier: 'The White Valley Group',
        organization$_identifier: 'The White Valley Group',
        name: 'disc_fixed_5_euro',
        discountAmount: 0,
        discount: 0,
        fixedPrice: null,
        obdiscAmt: 5,
        linesToApply: [
          '180FF6C6EAD05641890886CAF750A281',
          '5620C148C3059E0EA2C6C7DC6E0C22B4'
        ],
        ruleId: 'AE60075707B34A9D8CEE4D828326CFC0',
        rule: {
          _identifier: 'disc_fixed_5_euro',
          id: 'AE60075707B34A9D8CEE4D828326CFC0',
          name: 'disc_fixed_5_euro',
          obdiscAmt: 5
        }
      }
    ];

    setDiscountsEngineResultAs({
      lines: {
        '180FF6C6EAD05641890886CAF750A281': {
          discounts: {
            finalLinePrice: 53,
            finalUnitPrice: 13.25,
            promotions: [
              {
                ruleId: 'AE60075707B34A9D8CEE4D828326CFC0',
                discountType: '7B49D8CC4E084A75B7CB4D85A6A3A578',
                name: 'disc_fixed_5_euro',
                applyNext: true,
                amt: 5,
                qtyOffer: 0,
                manual: true
              }
            ]
          }
        },
        '5620C148C3059E0EA2C6C7DC6E0C22B4': {
          discounts: {
            finalLinePrice: 142.28,
            finalUnitPrice: 142.28,
            promotions: [
              {
                ruleId: 'AE60075707B34A9D8CEE4D828326CFC0',
                discountType: '7B49D8CC4E084A75B7CB4D85A6A3A578',
                name: 'disc_fixed_5_euro',
                applyNext: true,
                amt: 5,
                qtyOffer: 0,
                manual: true
              }
            ]
          }
        }
      }
    });

    const result = executeHookForTicket(deepfreeze(ticketWithDiscounts));

    expect(result).toEqual({
      id: '6FD3CDDDBB2A3805895853BB22F2E9F7',
      lines: [
        {
          id: '180FF6C6EAD05641890886CAF750A281',
          promotions: [
            {
              _identifier: 'disc_fixed_5_euro',
              actualAmt: 5,
              amt: 5,
              applyNext: true,
              calculatedOnDiscountEngine: true,
              client$_identifier: 'The White Valley Group',
              discount: 0,
              discountAmount: 0,
              discountType: '7B49D8CC4E084A75B7CB4D85A6A3A578',
              displayedTotalAmount: 5,
              fixedPrice: null,
              fullAmt: 5,
              id: 'AE60075707B34A9D8CEE4D828326CFC0',
              manual: true,
              name: 'disc_fixed_5_euro',
              obdiscAmt: 5,
              obdiscQtyoffer: 0,
              organization$_identifier: 'The White Valley Group',
              qtyOffer: 0,
              rule: {
                _identifier: 'disc_fixed_5_euro',
                id: 'AE60075707B34A9D8CEE4D828326CFC0',
                name: 'disc_fixed_5_euro',
                obdiscAmt: 5
              },
              ruleId: 'AE60075707B34A9D8CEE4D828326CFC0'
            }
          ]
        },
        {
          id: '5620C148C3059E0EA2C6C7DC6E0C22B4',
          promotions: [
            {
              _identifier: 'disc_fixed_5_euro',
              actualAmt: 5,
              amt: 5,
              applyNext: true,
              calculatedOnDiscountEngine: true,
              client$_identifier: 'The White Valley Group',
              discount: 0,
              discountAmount: 0,
              discountType: '7B49D8CC4E084A75B7CB4D85A6A3A578',
              displayedTotalAmount: 5,
              fixedPrice: null,
              fullAmt: 5,
              id: 'AE60075707B34A9D8CEE4D828326CFC0',
              manual: true,
              name: 'disc_fixed_5_euro',
              obdiscAmt: 5,
              obdiscQtyoffer: 0,
              organization$_identifier: 'The White Valley Group',
              qtyOffer: 0,
              rule: {
                _identifier: 'disc_fixed_5_euro',
                id: 'AE60075707B34A9D8CEE4D828326CFC0',
                name: 'disc_fixed_5_euro',
                obdiscAmt: 5
              },
              ruleId: 'AE60075707B34A9D8CEE4D828326CFC0'
            }
          ]
        }
      ],
      orderManualPromotions: [],
      discountsFromUser: {
        manualPromotions: [
          {
            _identifier: 'disc_fixed_5_euro',
            client$_identifier: 'The White Valley Group',
            discount: 0,
            discountAmount: 0,
            fixedPrice: null,
            id: 'AE60075707B34A9D8CEE4D828326CFC0',
            linesToApply: [
              '180FF6C6EAD05641890886CAF750A281',
              '5620C148C3059E0EA2C6C7DC6E0C22B4'
            ],
            name: 'disc_fixed_5_euro',
            obdiscAmt: 5,
            organization$_identifier: 'The White Valley Group',
            rule: {
              _identifier: 'disc_fixed_5_euro',
              id: 'AE60075707B34A9D8CEE4D828326CFC0',
              name: 'disc_fixed_5_euro',
              obdiscAmt: 5
            },
            ruleId: 'AE60075707B34A9D8CEE4D828326CFC0'
          }
        ]
      }
    });
  });

  function setDiscountsEngineResultAs(obj) {
    OB.Discounts.Pos.calculateLocal = jest.fn().mockReturnValue(obj);
  }

  function executeHookForTicket(ticket) {
    return hook(ticket, generatePayload(ticket));
  }
});
