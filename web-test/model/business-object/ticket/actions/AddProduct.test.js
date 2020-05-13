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

const emptyTicket = deepfreeze({ priceIncludesTax: true, lines: [] });

const productA = deepfreeze({
  id: 'pA',
  uOMstandardPrecision: 2,
  standardPrice: 5,
  listPrice: 5
});

const productB = deepfreeze({
  id: 'pB',
  uOMstandardPrecision: 3,
  standardPrice: 10,
  listPrice: 11
});

const serviceProduct = deepfreeze({
  id: 'pS',
  productType: 'S',
  uOMstandardPrecision: 3,
  standardPrice: 10,
  listPrice: 11
});

describe('addProduct', () => {
  describe('basics', () => {
    it('adds new lines if product not present', () => {
      const newTicket = OB.App.StateAPI.Ticket.addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }, { product: productB, qty: 2 }]
      });
      expect(newTicket.lines).toMatchObject([
        {
          qty: 1,
          grossPrice: 5,
          priceList: 5,
          priceIncludesTax: true,
          product: { id: 'pA' }
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
      const baseTicket = OB.App.StateAPI.Ticket.addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }, { product: productB, qty: 2 }]
      });

      const newTicket = OB.App.StateAPI.Ticket.addProduct(baseTicket, {
        products: [{ product: productA, qty: 10 }]
      });

      expect(newTicket.lines).toMatchObject([
        {
          qty: 11,
          product: { id: 'pA' }
        },
        {
          qty: 2,
          product: { id: 'pB' }
        }
      ]);
    });
  });

  describe('delivery mode', () => {
    it('is not set for service products', () => {
      const newTicket = OB.App.StateAPI.Ticket.addProduct(emptyTicket, {
        products: [{ product: serviceProduct, qty: 1 }]
      });

      expect(newTicket.lines[0]).not.toHaveProperty('obrdmDeliveryMode');
    });

    it('is set to PickAndCarry by default', () => {
      const newTicket = OB.App.StateAPI.Ticket.addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }]
      });

      expect(newTicket.lines[0]).toHaveProperty(
        'obrdmDeliveryMode',
        'PickAndCarry'
      );
    });

    it('is set to ticket mode if it has', () => {
      const newTicket = OB.App.StateAPI.Ticket.addProduct(
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
      const newTicket = OB.App.StateAPI.Ticket.addProduct(
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
      const newTicket = OB.App.StateAPI.Ticket.addProduct(
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
      deliveryMode           | productDeliveryDate | expectedDate
      ${'PickupInStoreDate'} | ${'productDate'}    | ${'productDate'}
      ${'HomeDelivery'}      | ${'productDate'}    | ${'productDate'}
      ${'Other'}             | ${'productDate'}    | ${undefined}
      ${'HomeDelivery'}      | ${undefined}        | ${'currentDate'}
      ${undefined}           | ${undefined}        | ${'ticketDeliveryDate'}
    `(
      'sets delivery date: $deliveryMode - $productDeliveryDate ',
      ({ deliveryMode, productDeliveryDate, expectedDate }) => {
        const newTicket = OB.App.StateAPI.Ticket.addProduct(
          {
            ...emptyTicket,
            obrdmDeliveryModeProperty: 'HomeDelivery',
            obrdmDeliveryDateProperty: 'ticketDeliveryDate'
          },
          {
            products: [
              {
                product: {
                  ...productA,
                  obrdmDeliveryMode: deliveryMode,
                  obrdmDeliveryDate: productDeliveryDate
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
      }
    );
  });
});
