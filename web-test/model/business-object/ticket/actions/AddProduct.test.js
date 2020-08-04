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
    Class: {},
    StateBackwardCompatibility: { setProperties: jest.fn() },
    TerminalProperty: { get: jest.fn() },
    UUID: { generate: jest.fn() }
  },
  MobileApp: { model: { get: jest.fn(() => jest.fn()) } },
  Taxes: { Pos: { applyTaxes: jest.fn() } },
  TerminalProperty: { get: jest.fn() },
  UTIL: { HookManager: { registerHook: jest.fn() } }
};

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/util/ArrayUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/AddProduct');

// set Ticket model utility functions
OB.App.State = { Ticket: { Utils: {} } };
OB.App.StateAPI.Ticket.utilities.forEach(
  util => (OB.App.State.Ticket.Utils[util.functionName] = util.implementation)
);

OB.App.TerminalProperty.get.mockImplementation(property => {
  if (property === 'warehouses') {
    return [
      {
        priority: 10,
        warehouseid: 'A154EC30A296479BB078B0AFFD74CA22',
        warehousename: 'Vall Blanca Store Warehouse'
      }
    ];
  } else if (property === 'terminal') {
    return {
      organization: 'D270A5AC50874F8BA67A88EE977F8E3B',
      organization$_identifier: 'Vall Blanca Store',
      country: '106',
      region: 'AF310D01B53B461283EB40DB21DCA6B5'
    };
  }
  return {};
});

const emptyTicket = {
  priceIncludesTax: true,
  lines: [],
  businessPartner: {},
  isEditable: true
};

const productA = {
  id: 'stdProduct',
  groupProduct: true,
  uOMstandardPrecision: 2,
  standardPrice: 5,
  listPrice: 5,
  returnable: true
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
  groupProduct: true,
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
  const preparedPayload = { ...payload, extraData: {} };
  preparedPayload.products = preparedPayload.products.map(pi => {
    const options = pi.options || {};
    const attrs = pi.attrs || {};
    return { ...pi, options, attrs };
  });
  return OB.App.StateAPI.Ticket.addProduct(
    deepfreeze(ticket),
    deepfreeze(preparedPayload)
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
          baseGrossUnitPrice: 5,
          grossListPrice: 5,
          priceIncludesTax: true,
          product: { id: 'stdProduct' }
        },
        {
          qty: 2,
          baseGrossUnitPrice: 10,
          grossListPrice: 11,
          priceIncludesTax: true,
          product: { id: 'pB' }
        }
      ]);
    });

    it('adds new lines if product not present (price not including taxes)', () => {
      const newTicket = addProduct(
        { ...emptyTicket, priceIncludesTax: false },
        {
          products: [
            { product: productA, qty: 1 },
            { product: productB, qty: 2 }
          ]
        }
      );

      expect(newTicket.lines).toMatchObject([
        {
          qty: 1,
          baseNetUnitPrice: 5,
          netListPrice: 5,
          priceIncludesTax: false,
          product: { id: 'stdProduct' }
        },
        {
          qty: 2,
          baseNetUnitPrice: 10,
          netListPrice: 11,
          priceIncludesTax: false,
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

    it('adds external attributes to new line', () => {
      const newTicket = addProduct(emptyTicket, {
        products: [
          { product: productA, qty: 1, attrs: { externalProperty: 'dummy' } }
        ]
      });

      expect(newTicket.lines).toMatchObject([
        {
          qty: 1,
          baseGrossUnitPrice: 5,
          grossListPrice: 5,
          priceIncludesTax: true,
          product: { id: 'stdProduct' },
          externalProperty: 'dummy'
        }
      ]);
    });

    it('adds external attributes to existing line', () => {
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }]
      });

      const newTicket = addProduct(baseTicket, {
        products: [
          { product: productA, qty: 10, attrs: { externalProperty: 'dummy' } }
        ]
      });

      expect(newTicket.lines).toMatchObject([
        {
          qty: 11,
          baseGrossUnitPrice: 5,
          grossListPrice: 5,
          priceIncludesTax: true,
          product: { id: 'stdProduct' },
          externalProperty: 'dummy'
        }
      ]);
    });

    it('do not add product to existing line if has splitline', () => {
      const baseTicket = addProduct(emptyTicket, {
        products: [
          {
            product: productA,
            qty: 1,
            attrs: {
              splitline: true
            }
          }
        ]
      });

      const newTicket = addProduct(baseTicket, {
        products: [{ product: productA, qty: 10 }]
      });

      expect(newTicket.lines).toMatchObject([
        {
          qty: 1,
          product: { id: 'stdProduct' }
        },
        {
          qty: 10,
          product: { id: 'stdProduct' }
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
        products: [
          { product: productA, qty: 1, options: { isEditable: false } }
        ]
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

    it('can select the line to work with', () => {
      const newTicket = addProduct(
        {
          lines: [
            { id: 'l1', qty: 1, product: productA },
            { id: 'l2', qty: 1, product: productA }
          ]
        },
        { products: [{ qty: 1, product: productA, options: { line: 'l1' } }] }
      );
      expect(newTicket.lines).toMatchObject([
        { id: 'l1', qty: 2 },
        { id: 'l2', qty: 1 }
      ]);
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
        products: [
          {
            product: productA,
            qty: 1,
            options: { isEditable: false, isDeletable: false }
          }
        ]
      });

      expect(newTicket.lines[0]).toMatchObject({
        isEditable: false,
        isDeletable: false
      });
    });

    it('create multiple lines of ungrouped product', () => {
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product: ungroupProduct, qty: 5 }]
      });

      expect(baseTicket.lines[0]).toMatchObject({
        qty: 1,
        product: { id: 'ungroupProduct' }
      });

      expect(baseTicket.lines).toHaveLength(5);
    });

    it('create multiple lines of ungrouped product (negative qty)', () => {
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product: ungroupProduct, qty: -5 }]
      });

      expect(baseTicket.lines[0]).toMatchObject({
        qty: -1,
        product: { id: 'ungroupProduct' }
      });

      expect(baseTicket.lines).toHaveLength(5);
    });

    it('delete lines with quantity zero', () => {
      const returned = { ...emptyTicket, orderType: 1 };
      const lineId = '1'; // id of the new line
      OB.App.UUID.generate.mockReturnValueOnce(lineId);
      const baseTicket = addProduct(returned, {
        products: [{ product: productA, qty: 1 }]
      });

      const finalTicket = addProduct(baseTicket, {
        products: [{ product: productA, qty: -1, options: { line: lineId } }]
      });

      expect(finalTicket.lines).toMatchObject([]);
    });
  });

  describe('services', () => {
    it('add service', () => {
      const lineId = '1';
      OB.App.UUID.generate.mockReturnValueOnce(lineId);
      // add product
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }]
      });

      // add service related with product
      const ticketWithService = addProduct(baseTicket, {
        products: [
          {
            product: serviceProduct,
            qty: 1,
            attrs: { relatedLines: [{ orderlineId: lineId }] }
          }
        ]
      });

      expect(ticketWithService.lines).toMatchObject([
        {
          id: lineId,
          product: productA,
          qty: 1
        },
        {
          product: serviceProduct,
          qty: 1,
          relatedLines: [{ orderlineId: lineId }]
        }
      ]);
    });

    test.each`
      quantityRule | expectedQty
      ${'UQ'}      | ${1}
      ${'PP'}      | ${2}
    `(
      "Add service with quantity rule '$quantityRule' for ungrouped product",
      ({ quantityRule, expectedQty }) => {
        const service = { ...serviceProduct, quantityRule };
        const line1Id = '1';
        const line2Id = '2';
        OB.App.UUID.generate
          .mockReturnValueOnce(line1Id)
          .mockReturnValueOnce(line2Id);
        // add product
        const baseTicket = addProduct(emptyTicket, {
          products: [
            { product: ungroupProduct, qty: 1 },
            { product: ungroupProduct, qty: 1 }
          ]
        });

        // add service related with line '1' product
        let ticketWithService = addProduct(baseTicket, {
          products: [
            {
              product: service,
              qty: 1,
              attrs: { relatedLines: [{ orderlineId: line1Id }] }
            }
          ]
        });

        // add service related with line '2' product
        ticketWithService = addProduct(ticketWithService, {
          products: [
            {
              product: service,
              qty: 1,
              attrs: { relatedLines: [{ orderlineId: line2Id }] }
            }
          ]
        });

        expect(ticketWithService.lines).toMatchObject([
          {
            id: line1Id,
            product: ungroupProduct,
            qty: 1
          },
          {
            id: line2Id,
            product: ungroupProduct,
            qty: 1
          },
          {
            product: serviceProduct,
            qty: expectedQty,
            relatedLines: [{ orderlineId: line1Id }, { orderlineId: line2Id }]
          }
        ]);
      }
    );

    it('add service multiple times (Unique quantity rule)', () => {
      const service = { ...serviceProduct, quantityRule: 'UQ' };
      const lineId = '1';
      OB.App.UUID.generate.mockReturnValueOnce(lineId);
      // add product
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }]
      });

      // add service related with product
      let ticketWithService = addProduct(baseTicket, {
        products: [
          {
            product: service,
            qty: 1,
            attrs: { relatedLines: [{ orderlineId: lineId }] }
          }
        ]
      });

      // add service related with product again
      ticketWithService = addProduct(ticketWithService, {
        products: [
          {
            product: service,
            qty: 1,
            attrs: { relatedLines: [{ orderlineId: lineId }] }
          }
        ]
      });

      expect(ticketWithService.lines).toMatchObject([
        {
          id: lineId,
          product: productA,
          qty: 1
        },
        {
          product: serviceProduct,
          qty: 1,
          relatedLines: [{ orderlineId: lineId }]
        }
      ]);
    });

    it('add multiple qty of service (Unique quantity rule)', () => {
      const service = { ...serviceProduct, quantityRule: 'UQ' };
      const lineId = '1';
      OB.App.UUID.generate.mockReturnValueOnce(lineId);
      // add product
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }]
      });

      // add multiple qty of service related with product
      let ticketWithService = addProduct(baseTicket, {
        products: [
          {
            product: service,
            qty: 2,
            attrs: { relatedLines: [{ orderlineId: lineId }] }
          }
        ]
      });

      expect(ticketWithService.lines).toMatchObject([
        {
          id: lineId,
          product: productA,
          qty: 1
        },
        {
          product: serviceProduct,
          qty: 1,
          relatedLines: [{ orderlineId: lineId }]
        }
      ]);
    });

    it('add returnable service related to negative line', () => {
      const service = { ...serviceProduct, quantityRule: 'UQ' };
      const lineId = '1';
      OB.App.UUID.generate.mockReturnValueOnce(lineId);
      // add product
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: -1 }]
      });

      // add service related with product
      let ticketWithService = addProduct(baseTicket, {
        products: [
          {
            product: service,
            qty: 1,
            attrs: { relatedLines: [{ orderlineId: lineId }] }
          }
        ]
      });

      expect(ticketWithService.lines).toMatchObject([
        {
          id: lineId,
          product: productA,
          qty: -1
        },
        {
          product: serviceProduct,
          qty: -1,
          relatedLines: [{ orderlineId: lineId }]
        }
      ]);
    });

    it('update related line tax information', () => {
      const product = { ...productA, productCategory: '1', taxCategory: '1' };
      const lineId = '0';
      OB.App.UUID.generate.mockReturnValueOnce(lineId);
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product, qty: 1 }]
      });
      baseTicket.lines[0].taxRate = 1.21; // simulate tax calculation

      const service = {
        ...serviceProduct,
        productServiceLinked: [
          {
            id: lineId,
            product: serviceProduct.id,
            productCategory: '1',
            taxCategory: '2'
          }
        ]
      };
      OB.App.State.Ticket.Utils.calculateTotals = jest
        .fn()
        .mockImplementation(ticket => {
          const newTicket = { ...ticket };
          newTicket.lines = ticket.lines.map(l => {
            const nl = { ...l };
            if (nl.id === lineId) {
              return { ...nl, taxRate: 1.11 };
            }
            return nl;
          });
          return newTicket;
        });
      const newTicket = addProduct(baseTicket, {
        products: [
          {
            product: service,
            qty: 1,
            attrs: {
              relatedLines: [
                {
                  orderlineId: lineId,
                  productCategory: '1',
                  productId: product.id,
                  productName: product.name
                }
              ]
            }
          }
        ]
      });

      expect(newTicket.lines).toMatchObject([
        {
          qty: 1,
          baseGrossUnitPrice: 4.58,
          previousBaseGrossUnitPrice: 5,
          grossListPrice: 5,
          priceIncludesTax: true,
          product: {
            id: 'stdProduct',
            taxCategory: '2',
            previousTaxCategory: '1'
          }
        },
        {
          qty: 1,
          baseGrossUnitPrice: 10,
          grossListPrice: 11,
          priceIncludesTax: true,
          product: { id: 'serviceProduct' }
        }
      ]);
    });

    it('merge related lines', () => {
      const line1Id = '1';
      const line2Id = '2';
      OB.App.UUID.generate
        .mockReturnValueOnce(line1Id)
        .mockReturnValueOnce(line2Id);
      const baseTicket = addProduct(emptyTicket, {
        products: [{ product: productA, qty: 1 }, { product: productB, qty: 1 }]
      });
      baseTicket.lines[0].taxRate = 1.21;
      baseTicket.lines[1].taxRate = 1.21;

      const groupedService = {
        ...serviceProduct,
        quantityRule: 'UQ',
        productServiceLinked: [
          {
            id: line1Id,
            product: serviceProduct.id,
            productCategory: '1',
            taxCategory: '2'
          }
        ]
      };

      let ticket = addProduct(baseTicket, {
        products: [
          {
            product: groupedService,
            qty: 1,
            attrs: {
              relatedLines: [
                {
                  orderlineId: line1Id,
                  productCategory: '1',
                  productId: productA.id,
                  productName: productA.name
                }
              ]
            }
          }
        ]
      });

      ticket = addProduct(ticket, {
        products: [
          {
            product: groupedService,
            qty: 1,
            attrs: {
              relatedLines: [
                {
                  orderlineId: line2Id,
                  productCategory: '1',
                  productId: productB.id,
                  productName: productB.name
                }
              ]
            }
          }
        ]
      });

      ticket = addProduct(ticket, {
        products: [
          {
            product: groupedService,
            qty: 1,
            attrs: {
              relatedLines: [
                {
                  orderlineId: line1Id,
                  productCategory: '1',
                  productId: productA.id,
                  productName: productA.name
                }
              ]
            }
          }
        ]
      });

      expect(ticket.lines).toMatchObject([
        {
          id: line1Id,
          qty: 1,
          baseGrossUnitPrice: 5,
          grossListPrice: 5,
          priceIncludesTax: true,
          product: { id: 'stdProduct' }
        },
        {
          id: line2Id,
          qty: 1,
          baseGrossUnitPrice: 10,
          grossListPrice: 11,
          priceIncludesTax: true,
          product: { id: 'pB' }
        },
        {
          qty: 1,
          baseGrossUnitPrice: 10,
          grossListPrice: 11,
          priceIncludesTax: true,
          product: { id: 'serviceProduct' },
          relatedLines: [
            {
              orderlineId: line1Id,
              productCategory: '1',
              productId: productA.id,
              productName: productA.name
            },
            {
              orderlineId: line2Id,
              productCategory: '1',
              productId: productB.id,
              productName: productB.name
            }
          ]
        }
      ]);
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
        currentDate.setHours(0, 0, 0, 0);
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
