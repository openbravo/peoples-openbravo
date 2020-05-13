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
    StateBackwardCompatibility: { setProperties: jest.fn() },
    Class: {},
    UUID: { generate: jest.fn() }
  },
  UTIL: { HookManager: { registerHook: jest.fn() } },
  MobileApp: { model: { get: jest.fn(() => jest.fn()) } }
};

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/AddProduct');

const emptyTicket = { priceIncludesTax: true, lines: [] };

const productA = {
  id: 'stdProduct',
  groupProduct: true,
  uOMstandardPrecision: 2,
  standardPrice: 5,
  listPrice: 5
};

const productB = {
  id: 'pB',
  groupProduct: true,
  uOMstandardPrecision: 3,
  standardPrice: 10,
  listPrice: 11
};

const serviceProduct = {
  id: 'serviceProduct',
  productType: 'S',
  uOMstandardPrecision: 3,
  standardPrice: 10,
  listPrice: 11
};

const scaleProduct = {
  id: 'scaleProduct',
  obposScale: true,
  productType: 'S',
  uOMstandardPrecision: 3,
  standardPrice: 10,
  listPrice: 11
};

const ungroupProduct = {
  id: 'ungroupProduct',
  groupProduct: false,
  uOMstandardPrecision: 2,
  standardPrice: 5,
  listPrice: 5
};

const products = [
  productA,
  productB,
  serviceProduct,
  scaleProduct,
  ungroupProduct
];

const addProduct = (ticket, payload) => {
  return OB.App.StateAPI.Ticket.addProduct(
    deepfreeze(ticket),
    deepfreeze(payload)
  );
};

describe('addProduct', () => {
  describe('basics', () => {
    it('adds new lines if product not present', () => {
      const newTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }, { product: productB, qty: 2 }]
      });

      expect(newTicket.lines).toMatchObject([
        {
          qty: 1,
          grossPrice: 5,
          priceList: 5,
          priceIncludesTax: true,
          product: { id: 'stdProduct' }
        },
        {
          qty: 2,
          grossPrice: 10,
          priceList: 11,
          priceIncludesTax: true,
          product: { id: 'pB' }
        }
      ]);
    });

    it('adds units to existing lines with same product', () => {
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }, { product: productB, qty: 2 }]
      });

      const newTicket = addProduct(baseTicket, {
        products: [{ product: productA, qty: 10 }]
      });

      expect(newTicket.lines).toMatchObject([
        {
          qty: 11,
          product: { id: 'stdProduct' }
        },
        {
          qty: 2,
          product: { id: 'pB' }
        }
      ]);
    });

    it.each`
      productType         | expectNewLine
      ${'stdProduct'}     | ${false}
      ${'scaleProduct'}   | ${true}
      ${'ungroupProduct'} | ${true}
    `(
      'adds or edits line depending on product: $productType',
      ({ productType, expectNewLine }) => {
        const product = products.find(p => p.id === productType);
        const baseTicket = addProduct(emptyTicket, {
          products: [{ product: product, qty: 1 }]
        });

        const newTicket = addProduct(baseTicket, {
          products: [{ product: product, qty: 1 }]
        });

        expect(newTicket.lines).toHaveLength(expectNewLine ? 2 : 1);
      }
    );

    it('creates new lines if adding product with not editable line', () => {
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }],
        options: { isEditable: false }
      });

      const newTicket = addProduct(baseTicket, {
        products: [{ product: productA, qty: 1 }]
      });

      expect(newTicket.lines).toHaveLength(2);
    });

    it('creates new lines if adding product to a return line', () => {
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: -2 }]
      });

      const newTicket = addProduct(baseTicket, {
        products: [{ product: productA, qty: 1 }]
      });

      expect(newTicket.lines).toHaveLength(2);
    });

    it('new lines are editable by default', () => {
      const newTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }]
      });

      expect(newTicket.lines[0]).toMatchObject({
        isEditable: true,
        isDeletable: true
      });
    });

    it('can make new lines not editable', () => {
      const newTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }],
        options: { isEditable: false, isDeletable: false }
      });

      expect(newTicket.lines[0]).toMatchObject({
        isEditable: false,
        isDeletable: false
      });
    });
  });

  describe('delivery mode', () => {
    it('is not set for service products', () => {
      const newTicket = addProduct(emptyTicket, {
        products: [{ product: serviceProduct, qty: 1 }]
      });

      expect(newTicket.lines[0]).not.toHaveProperty('obrdmDeliveryMode');
    });

    it('is set to PickAndCarry by default', () => {
      const newTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }]
      });

      expect(newTicket.lines[0]).toHaveProperty(
        'obrdmDeliveryMode',
        'PickAndCarry'
      );
    });

    it('is set to ticket mode if it has', () => {
      const newTicket = addProduct(
        { ...emptyTicket, obrdmDeliveryModeProperty: 'testMode' },
        {
          products: [{ product: productA, qty: 1 }]
        }
      );

      expect(newTicket.lines[0]).toHaveProperty(
        'obrdmDeliveryMode',
        'testMode'
      );
    });

    it('is set to product mode if it has', () => {
      const newTicket = addProduct(
        { ...emptyTicket, obrdmDeliveryModeProperty: 'testMode' },
        {
          products: [
            {
              product: {
                ...productA,
                obrdmDeliveryMode: 'prodDeliveryMode',
                obrdmDeliveryModeLyw: 'lywDeliveryMode'
              },
              qty: 1
            }
          ]
        }
      );

      expect(newTicket.lines[0]).toHaveProperty(
        'obrdmDeliveryMode',
        'prodDeliveryMode'
      );
    });

    it('is set to layaway product mode if it has', () => {
      const newTicket = addProduct(
        {
          ...emptyTicket,
          isLayaway: true,
          obrdmDeliveryModeProperty: 'testMode'
        },
        {
          products: [
            {
              product: {
                ...productA,
                obrdmDeliveryMode: 'prodDeliveryMode',
                obrdmDeliveryModeLyw: 'lywDeliveryMode'
              },
              qty: 1
            }
          ]
        }
      );

      expect(newTicket.lines[0]).toHaveProperty(
        'obrdmDeliveryMode',
        'lywDeliveryMode'
      );
    });

    it.each`
      deliveryMode           | productDeliveryDate | expectedDate            | expectedTime
      ${'PickupInStoreDate'} | ${'productDate'}    | ${'productDate'}        | ${undefined}
      ${'HomeDelivery'}      | ${'productDate'}    | ${'productDate'}        | ${'currentTime'}
      ${'Other'}             | ${'productDate'}    | ${undefined}            | ${undefined}
      ${'HomeDelivery'}      | ${undefined}        | ${'currentDate'}        | ${'currentTime'}
      ${undefined}           | ${undefined}        | ${'ticketDeliveryDate'} | ${'ticketDeliveryTime'}
    `(
      'sets delivery date: $deliveryMode - $productDeliveryDate ',
      ({ deliveryMode, productDeliveryDate, expectedDate, expectedTime }) => {
        const newTicket = addProduct(
          {
            ...emptyTicket,
            obrdmDeliveryModeProperty: 'HomeDelivery',
            obrdmDeliveryDateProperty: 'ticketDeliveryDate',
            obrdmDeliveryTimeProperty: 'ticketDeliveryTime'
          },
          {
            products: [
              {
                product: {
                  ...productA,
                  obrdmDeliveryMode: deliveryMode,
                  obrdmDeliveryDate: productDeliveryDate,
                  productDeliveryTime: 'productDeliveryTime'
                },
                qty: 1
              }
            ]
          }
        );
        const currentDate = new Date();
        currentDate.setHours(0);
        currentDate.setMinutes(0);
        currentDate.setSeconds(0);
        currentDate.setMilliseconds(0);
        const dateToExpect =
          expectedDate === 'currentDate' ? currentDate : expectedDate;

        expect(newTicket.lines[0].obrdmDeliveryDate).toStrictEqual(
          dateToExpect
        );

        const currentTime = new Date();
        currentTime.setSeconds(0);
        currentTime.setMilliseconds(0);
        const timeToExpect =
          expectedTime === 'currentTime' ? currentTime : expectedTime;

        expect(newTicket.lines[0].obrdmDeliveryTime).toStrictEqual(
          timeToExpect
        );
      }
    );
  });
});
