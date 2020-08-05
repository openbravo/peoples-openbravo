/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */
/* eslint-disable jest/expect-expect */

global.OB = {
  App: {
    Class: {},
    DAL: { remoteSearch: jest.fn() },
    MasterdataModels: {
      Product: { find: jest.fn() },
      ProductBOM: { find: jest.fn() },
      ProductCharacteristicValue: { find: jest.fn() },
      ProductServiceLinked: { find: jest.fn() },
      ProductPrice: { find: jest.fn() }
    },
    ProductPackProvider: { getPack: jest.fn() },
    Request: { mobileServiceRequest: jest.fn() },
    Security: { hasPermission: jest.fn(), requestApprovalForAction: jest.fn() },
    SpecialCharacters: { bullet: jest.fn() },
    StateBackwardCompatibility: { setProperties: jest.fn() },
    TerminalProperty: { get: jest.fn() },
    View: { DialogUIHandler: { inputData: jest.fn() } }
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
  UTIL: {
    HookManager: { registerHook: jest.fn() },
    servicesFilter: jest.fn()
  },
  POS: {
    hwserver: {
      getAsyncWeight: jest.fn()
    }
  },
  Taxes: {
    Pos: {
      ruleImpls: [],
      taxCategoryBOM: [{ id: 'FF80818123B7FC160123B804AB8C0019' }]
    }
  },
  error: jest.fn()
};

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
const {
  executeActionPreparations
} = require('../../../../../../org.openbravo.mobile.core/web-test/base/state-utils');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/exception/TranslatableError');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionCanceled');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionSilentlyCanceled');

require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-logic/stock/StockChecker');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/AddProduct');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/util/UUID');

// set Ticket model utility functions
OB.App.State = { Ticket: { Utils: {} } };
OB.App.StateAPI.Ticket.utilities.forEach(
  util => (OB.App.State.Ticket.Utils[util.functionName] = util.implementation)
);

OB.App.Class.Criteria = class MockedCriteria {
  criterion() {
    return {
      build: jest.fn(),
      criterion: () => {
        return { build: jest.fn };
      }
    };
  }

  build() {
    return {};
  }
};

const Product = {
  productA: {
    id: '0',
    _identifier: 'pA',
    uOMstandardPrecision: 3,
    standardPrice: 10,
    listPrice: 11
  },
  productB: {
    id: '1',
    _identifier: 'pB',
    uOMstandardPrecision: 3,
    standardPrice: 10,
    listPrice: 11,
    returnable: true,
    groupProduct: true
  }
};

const Ticket = {
  empty: {
    priceIncludesTax: true,
    priceList: '5D47B13F42A44352B09C97A72EE42ED8',
    lines: [],
    businessPartner: { id: '1', priceList: '5D47B13F42A44352B09C97A72EE42ED8' },
    orderType: 0
  },
  emptyReturn: {
    priceIncludesTax: true,
    priceList: '5D47B13F42A44352B09C97A72EE42ED8',
    lines: [],
    businessPartner: { id: '1', priceList: '5D47B13F42A44352B09C97A72EE42ED8' },
    orderType: 1
  },
  emptyLayaway: {
    priceIncludesTax: true,
    priceList: '5D47B13F42A44352B09C97A72EE42ED8',
    lines: [],
    businessPartner: { id: '1', priceList: '5D47B13F42A44352B09C97A72EE42ED8' },
    orderType: 2
  },
  returned: {
    priceIncludesTax: true,
    priceList: '5D47B13F42A44352B09C97A72EE42ED8',
    lines: [{ id: '1', product: Product.productB, qty: -1, isEditable: true }],
    businessPartner: { id: '1', priceList: '5D47B13F42A44352B09C97A72EE42ED8' },
    orderType: 1
  },
  singleLine: {
    priceIncludesTax: true,
    priceList: '5D47B13F42A44352B09C97A72EE42ED8',
    lines: [{ id: '1', product: Product.productA, qty: 1 }],
    businessPartner: { id: '1', priceList: '5D47B13F42A44352B09C97A72EE42ED8' },
    orderType: 0
  },
  returnedLine: {
    priceIncludesTax: true,
    priceList: '5D47B13F42A44352B09C97A72EE42ED8',
    lines: [{ id: '1', product: Product.productA, qty: -1 }],
    businessPartner: { id: '1', priceList: '5D47B13F42A44352B09C97A72EE42ED8' },
    orderType: 0
  },
  attributeLine: {
    priceIncludesTax: true,
    priceList: '5D47B13F42A44352B09C97A72EE42ED8',
    lines: [
      { id: '1', product: Product.productA, qty: 1, attributeValue: '1234' }
    ],
    businessPartner: { id: '1', priceList: '5D47B13F42A44352B09C97A72EE42ED8' },
    orderType: 0
  },
  cancelAndReplace: {
    priceIncludesTax: true,
    priceList: '5D47B13F42A44352B09C97A72EE42ED8',
    lines: [
      { id: '1', product: Product.productA, qty: -1, replacedorderline: true }
    ],
    businessPartner: { id: '1', priceList: '5D47B13F42A44352B09C97A72EE42ED8' },
    orderType: 0
  }
};

const prepareAction = async (payload, ticket = Ticket.empty) => {
  const newPayload = await executeActionPreparations(
    OB.App.StateAPI.Ticket.addProduct,
    deepfreeze(ticket),
    deepfreeze(payload)
  );
  return newPayload;
};

const expectError = async (action, expectedError) => {
  let error;
  try {
    await action();
  } catch (e) {
    error = e;
  }
  expect(error).toMatchObject({ info: expectedError });
};

describe('addProduct preparation', () => {
  const setDefaultMocks = () => {
    OB.POS.hwserver.getAsyncWeight.mockImplementation(() =>
      Promise.resolve({ result: 10 })
    );
    OB.App.StockChecker.hasStock = jest.fn().mockResolvedValue(true);
    OB.UTIL.servicesFilter.mockResolvedValue(new OB.App.Class.Criteria());
    OB.App.TerminalProperty.get.mockImplementation(property => {
      if (property === 'productStatusList') {
        return [
          {
            id: '1A62CC9E44364EA6881A0A86417D61AF',
            name: 'Ramp-Up',
            restrictsalefrompos: false,
            restrictsaleoutofstock: false
          },
          {
            id: '7E4B33B5FB6444409E45D61668269FA3',
            name: 'Obsolete',
            restrictsalefrompos: true,
            restrictsaleoutofstock: true
          }
        ];
      }
      if (property === 'terminal') {
        return { priceList: '5D47B13F42A44352B09C97A72EE42ED8' };
      }
      return {};
    });
  };

  beforeEach(() => {
    jest.resetAllMocks();
    setDefaultMocks();
  });

  describe('check restrictions', () => {
    it('quantities check in quotation', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_ReturnLine'
      );
      const quotation = {
        ...Ticket.singleLine,
        isQuotation: true
      };
      await expectError(
        () =>
          prepareAction(
            {
              products: [
                { product: Product.productA, qty: -2, options: { line: '1' } }
              ]
            },
            quotation
          ),
        {
          errorMsg: 'OBPOS_MsgCannotAddNegative'
        }
      );
    });

    it('quantities check in return', async () => {
      await expectError(
        () =>
          prepareAction(
            {
              products: [
                { product: Product.productB, qty: -2, options: { line: '1' } }
              ]
            },
            Ticket.returned
          ),
        {
          errorMsg: 'OBPOS_MsgCannotAddPostiveToReturn'
        }
      );
    });

    it('product without price check', async () => {
      const productWithoutPrice = { ...Product.productA, listPrice: undefined };
      await expectError(
        () =>
          prepareAction({
            products: [{ product: productWithoutPrice, qty: 1 }]
          }),
        {
          warningMsg: 'OBPOS_productWithoutPriceInPriceList',
          messageParams: ['pA']
        }
      );
    });

    it('product without price check (allow products without price preference)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_allowProductsNoPriceInMainPricelist'
      );
      const productWithoutPrice = { ...Product.productA, listPrice: undefined };
      const newPayload = await prepareAction({
        products: [{ product: productWithoutPrice, qty: 1 }]
      });
      expect(newPayload.products).toMatchObject([
        { product: productWithoutPrice, qty: 1 }
      ]);
    });

    it('generic product check', async () => {
      await expectError(
        () =>
          prepareAction({
            products: [{ product: { ...Product.productA, isGeneric: true } }]
          }),
        {
          warningMsg: 'OBPOS_GenericNotAllowed'
        }
      );
    });

    it('cancel and replace qty check', async () => {
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: Product.productA, options: { line: '1' } }]
            },
            Ticket.cancelAndReplace
          ),
        {
          errorConfirmation: 'OBPOS_CancelReplaceQtyEditReturn'
        }
      );
    });

    it('anonymous business partner check', async () => {
      await expectError(
        () =>
          prepareAction(
            {
              products: [
                {
                  product: {
                    ...Product.productA,
                    oBPOSAllowAnonymousSale: false
                  },
                  options: { businessPartner: '1' }
                }
              ]
            },
            Ticket.empty
          ),
        {
          errorConfirmation: 'OBPOS_AnonymousSaleNotAllowed'
        }
      );
    });

    it('anonymous business partner check (deferred order)', async () => {
      await expectError(
        () =>
          prepareAction(
            {
              products: [
                {
                  product: {
                    ...Product.productA,
                    oBPOSAllowAnonymousSale: false
                  },
                  options: { businessPartner: '1' }
                }
              ]
            },
            { ...Ticket.empty, deferredOrder: true }
          ),
        {
          errorConfirmation: 'OBPOS_AnonymousSaleNotAllowedDeferredSale'
        }
      );
    });

    it('not returnable check (1)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_ReturnLine'
      );
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: Product.productA, qty: 1 }]
            },
            Ticket.emptyReturn
          ),
        {
          errorConfirmation: 'OBPOS_UnreturnableProductMessage'
        }
      );
    });

    it('not returnable check (2)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_ReturnLine'
      );
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: Product.productA, qty: -1 }]
            },
            Ticket.empty
          ),
        {
          errorConfirmation: 'OBPOS_UnreturnableProductMessage'
        }
      );
    });

    it('not returnable check (3)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_ReturnLine'
      );
      await expectError(
        () =>
          prepareAction(
            {
              products: [
                { product: Product.productA, qty: -2, options: { line: '1' } }
              ]
            },
            Ticket.singleLine
          ),
        {
          errorConfirmation: 'OBPOS_UnreturnableProductMessage'
        }
      );
    });

    it('not returnable service', async () => {
      const unreturnableService = {
        ...Product.productB,
        productType: 'S',
        returnable: false
      };
      await expectError(
        () =>
          prepareAction(
            {
              products: [
                {
                  product: unreturnableService,
                  qty: 1,
                  attrs: { relatedLines: [{ orderlineId: '1' }] }
                }
              ]
            },
            Ticket.returnedLine
          ),
        {
          errorConfirmation: 'OBPOS_UnreturnableProductMessage'
        }
      );
    });

    it('editable check', async () => {
      const notEditable = {
        ...Ticket.singleLine,
        isEditable: false
      };
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: Product.productA, qty: 1 }]
            },
            notEditable
          ),
        {
          title: 'OBPOS_modalNoEditableHeader',
          errorConfirmation: 'OBPOS_modalNoEditableBody'
        }
      );
    });

    it('closed quotation check', async () => {
      const closedQuotation = {
        ...Ticket.singleLine,
        isQuotation: true,
        hasbeenpaid: 'Y'
      };
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: Product.productA, qty: 1 }]
            },
            closedQuotation
          ),
        {
          errorMsg: 'OBPOS_QuotationClosed'
        }
      );
    });

    it('product locked', async () => {
      const lockedProduct = {
        ...Product.productA,
        productStatus: '7E4B33B5FB6444409E45D61668269FA3'
      };
      await expectError(
        () =>
          prepareAction({
            products: [{ product: lockedProduct, qty: 1 }]
          }),
        {
          errorConfirmation: 'OBPOS_ErrorProductLocked',
          messageParams: ['pA', 'Obsolete']
        }
      );
    });

    it('allow sales with return', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_ReturnLine' || p === 'OBPOS_NotAllowSalesWithReturn'
      );

      const newPayload = await prepareAction(
        {
          products: [
            {
              product: Product.productB,
              qty: -1,
              options: { allowLayawayWithReturn: true }
            }
          ]
        },
        Ticket.singleLine
      );

      expect(newPayload.products).toMatchObject([
        {
          product: Product.productB,
          qty: -1
        }
      ]);
    });

    it('not allow sales with return (1)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_NotAllowSalesWithReturn'
      );
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: Product.productB, qty: -1 }]
            },
            Ticket.singleLine
          ),
        {
          errorMsg: 'OBPOS_MsgCannotAddNegative'
        }
      );
    });

    it('not allow sales with return (2)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_NotAllowSalesWithReturn'
      );
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: Product.productB, qty: 1 }]
            },
            Ticket.returnedLine
          ),
        {
          errorMsg: 'OBPOS_MsgCannotAddPositive'
        }
      );
    });

    it('allow layaways with negative lines', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p !== 'OBPOS_AllowLayawaysNegativeLines'
      );

      const newPayload = await prepareAction(
        {
          products: [
            {
              product: Product.productB,
              qty: -1,
              options: { allowLayawayWithReturn: true }
            }
          ]
        },
        Ticket.emptyLayaway
      );

      expect(newPayload.products).toMatchObject([
        {
          product: Product.productB,
          qty: -1
        }
      ]);
    });

    it('not allow layaways with negative lines', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p !== 'OBPOS_AllowLayawaysNegativeLines'
      );
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: Product.productB, qty: -1 }]
            },
            Ticket.emptyLayaway
          ),
        {
          errorMsg: 'OBPOS_layawaysOrdersWithReturnsNotAllowed'
        }
      );
    });
  });

  describe('prepare product packs', () => {
    it('add standard pack to ticket', async () => {
      const pack = {
        ...Product.productA,
        ispack: true,
        productCategory: 'BE5D42E554644B6AA262CCB097753951'
      };
      const payload = {
        products: [
          { product: pack, qty: 1 },
          { product: Product.productA, qty: 2 }
        ]
      };
      const packContent = [
        {
          product: {
            id: '1',
            standardPrice: 23,
            listPrice: 29
          },
          qty: 1,
          belongsToPack: true,
          options: {},
          attrs: {}
        },
        {
          product: {
            id: '2',
            standardPrice: 23,
            listPrice: 11
          },
          qty: 2,
          belongsToPack: true,
          options: {},
          attrs: {}
        }
      ];

      OB.App.ProductPackProvider.getPack
        .mockReturnValueOnce({
          process: jest.fn().mockResolvedValue(packContent)
        }) // for the pack
        .mockReturnValueOnce(undefined); // for the regular product

      const newPayload = await prepareAction(payload);
      expect(newPayload.products).toMatchObject([
        ...packContent,
        { product: Product.productA, qty: 2 }
      ]);
    });

    it('pack processing fails', async () => {
      const pack = {
        ...Product.productA,
        ispack: true,
        productCategory: 'BE5D42E554644B6AA262CCB097753951'
      };
      const payload = {
        products: [{ product: pack, qty: 1 }]
      };

      OB.App.ProductPackProvider.getPack.mockReturnValueOnce({
        process: jest
          .fn()
          .mockRejectedValue(
            new OB.App.Class.TranslatableError('title', 'message', ['param'])
          )
      });
      await expectError(() => prepareAction(payload), {
        title: 'title',
        errorMsg: 'message',
        messageParams: ['param']
      });
    });

    it('more than one pack not allowed', async () => {
      const pack = {
        ...Product.productA,
        ispack: true,
        productCategory: 'BE5D42E554644B6AA262CCB097753951'
      };
      await expect(
        prepareAction({
          products: [{ product: pack }, { product: pack }]
        })
      ).rejects.toThrow('Cannot handle more than one pack');
    });

    it('more than one unit of a pack is not allowed', async () => {
      const pack = {
        ...Product.productA,
        ispack: true,
        productCategory: 'BE5D42E554644B6AA262CCB097753951'
      };
      await expect(
        prepareAction({
          products: [{ product: pack, qty: 2 }]
        })
      ).rejects.toThrow('Cannot handle more than unit of a pack');
    });
  });

  describe('prepare scale products', () => {
    const scaleProduct = { ...Product.productA, obposScale: true };
    it('more than one is not allowed', async () => {
      await expect(
        prepareAction({
          products: [{ product: scaleProduct }, { product: scaleProduct }]
        })
      ).rejects.toThrow('Cannot handle more than one scale product');
    });

    it('calls scale once', async () => {
      await prepareAction({
        products: [{ product: scaleProduct }]
      });

      expect(OB.POS.hwserver.getAsyncWeight).toHaveBeenCalledTimes(1);
    });

    it('scale value is used as qty', async () => {
      const newPayload = await prepareAction({
        products: [{ product: scaleProduct }]
      });

      expect(newPayload).toMatchObject({ products: [{ qty: 10 }] });
    });

    it('skip scale preparation on verified return', async () => {
      const newPayload = await prepareAction({
        products: [
          { product: scaleProduct, options: { isVerifiedReturn: true } }
        ]
      });

      expect(newPayload).toMatchObject({ products: [{ qty: 1 }] });
    });

    it('fails when scale returns 0', async () => {
      OB.POS.hwserver.getAsyncWeight.mockImplementation(() =>
        Promise.resolve({ result: 0 })
      );

      await expectError(
        () =>
          prepareAction({
            products: [{ product: scaleProduct }]
          }),
        {
          errorConfirmation: 'OBPOS_WeightZero'
        }
      );
    });

    it('handles with error offline scale', async () => {
      OB.POS.hwserver.getAsyncWeight.mockImplementation(() =>
        Promise.resolve({ exception: 1 })
      );

      await expectError(
        () =>
          prepareAction({
            products: [{ product: scaleProduct }]
          }),
        {
          errorConfirmation: 'OBPOS_MsgScaleServerNotAvailable'
        }
      );
    });
  });

  describe('stock check', () => {
    it('handles no stock', async () => {
      OB.App.StockChecker.hasStock.mockResolvedValueOnce(false);
      await expect(
        prepareAction({
          products: [{ product: Product.productA }]
        })
      ).rejects.toThrow(
        `Add product canceled: there is no stock of product ${Product.productA.id}`
      );
    });
  });

  describe('prepare BOM', () => {
    it('product has BOM', async () => {
      OB.App.MasterdataModels.ProductBOM.find.mockResolvedValueOnce([
        {
          active: true,
          bomprice: 15,
          bomproduct: 'F0659DF0BC634D38855D4D86082B7AA1',
          bomquantity: 2,
          bomtaxcategory: 'FF80818123B7FC160123B804AB8C0019',
          id: '32182CDA9D544392A092A913A68AFEC1',
          product: Product.productA.id
        }
      ]);
      const productWithBOM = {
        ...Product.productA,
        taxCategory: 'FF80818123B7FC160123B804AB8C0019'
      };
      const payload = {
        products: [{ product: productWithBOM, qty: 1 }]
      };
      const newPayload = await prepareAction(payload, Ticket.empty);
      expect(newPayload.products[0]).toMatchObject({
        product: {
          ...payload.products[0].product,
          productBOM: [
            {
              grossUnitAmount: 30,
              netUnitAmount: undefined,
              qty: 2,
              product: {
                id: 'F0659DF0BC634D38855D4D86082B7AA1',
                taxCategory: 'FF80818123B7FC160123B804AB8C0019'
              }
            }
          ]
        },
        qty: 1
      });
    });

    it('product has no BOM', async () => {
      OB.App.MasterdataModels.ProductBOM.find.mockResolvedValueOnce([]);
      const productWithBOM = {
        ...Product.productA,
        taxCategory: 'FF80818123B7FC160123B804AB8C0019'
      };
      const payload = {
        products: [{ product: productWithBOM, qty: 1 }]
      };
      const newPayload = await prepareAction(payload, Ticket.empty);
      expect(newPayload.products).toMatchObject(payload.products);
    });

    it('product BOM nas no price', async () => {
      OB.App.MasterdataModels.ProductBOM.find.mockResolvedValueOnce([
        {
          active: true,
          bomproduct: 'F0659DF0BC634D38855D4D86082B7AA1',
          bomquantity: 2,
          bomtaxcategory: 'FF80818123B7FC160123B804AB8C0019',
          id: '32182CDA9D544392A092A913A68AFEC1',
          product: Product.productA.id
        }
      ]);
      const productWithBOM = {
        ...Product.productA,
        taxCategory: 'FF80818123B7FC160123B804AB8C0019'
      };
      await expectError(
        () =>
          prepareAction({
            products: [{ product: productWithBOM }]
          }),
        {
          errorConfirmation: 'OBPOS_BOM_NoPrice'
        }
      );
    });
  });

  describe('prepare linked product service', () => {
    it('add ProductServiceLinked information (local)', async () => {
      const productServiceLinked = {
        ...Product.productA,
        modifyTax: true
      };
      const serviceLinked = [
        {
          id: '0BF2B3F4F7B843B6B777CA7A2AEFB1C3',
          product: Product.productA.id,
          productCategory: '5E0287DDDBB9432B9CA00CCCE9296BEC',
          taxCategory: 'FF80818123B7FC160123B804AB880008'
        }
      ];
      const payload = {
        products: [{ product: productServiceLinked, qty: 1 }]
      };
      OB.App.Security.hasPermission.mockReturnValue(false);
      OB.App.MasterdataModels.ProductServiceLinked.find.mockResolvedValueOnce(
        serviceLinked
      );
      const newPayload = await prepareAction(payload, Ticket.empty);
      expect(newPayload.products[0]).toMatchObject({
        product: {
          ...payload.products[0].product,
          productServiceLinked: serviceLinked
        },
        qty: 1
      });
    });

    it('add ProductServiceLinked information (remote)', async () => {
      const productServiceLinked = {
        ...Product.productA,
        modifyTax: true
      };
      const serviceLinked = [
        {
          id: '0BF2B3F4F7B843B6B777CA7A2AEFB1C3',
          product: Product.productA.id,
          productCategory: '5E0287DDDBB9432B9CA00CCCE9296BEC',
          taxCategory: 'FF80818123B7FC160123B804AB880008'
        }
      ];
      const payload = {
        products: [{ product: productServiceLinked, qty: 1 }]
      };
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.DAL.remoteSearch.mockResolvedValueOnce(serviceLinked);
      const newPayload = await prepareAction(payload, Ticket.empty);
      expect(newPayload.products[0]).toMatchObject({
        product: {
          ...payload.products[0].product,
          productServiceLinked: serviceLinked
        },
        qty: 1
      });
    });
  });

  describe('prepare product characteristics', () => {
    it('product has characteristics', async () => {
      const characteristics = [
        {
          active: true,
          characteristic: '015D6C6072AC4A13B7573A261B2011BC',
          characteristicValue: 'C6A9CF2813DC49C1BE5A9A6094DD967E',
          id: '1983C55A254D41A7AD2E17E84CA5F70A',
          obposFilteronwebpos: true,
          product: Product.productA.id,
          _identifier: 'Color'
        }
      ];
      OB.App.MasterdataModels.ProductCharacteristicValue.find.mockResolvedValueOnce(
        characteristics
      );
      const productWithCharacteritics = {
        ...Product.productA,
        _identifier: 'productWithCharacteristics',
        characteristicDescription: 'Color: Black/Silver'
      };
      const payload = {
        products: [{ product: productWithCharacteritics, qty: 1 }]
      };
      const newPayload = await prepareAction(payload, Ticket.empty);
      expect(newPayload.products[0]).toMatchObject({
        product: {
          ...payload.products[0].product,
          productCharacteristics: characteristics
        },
        qty: 1
      });
    });

    it('could not get characteristics', async () => {
      OB.App.MasterdataModels.ProductCharacteristicValue.find.mockRejectedValueOnce(
        new Error()
      );
      const productWithCharacteritics = {
        ...Product.productA,
        _identifier: 'productWithCharacteristics',
        characteristicDescription: 'Color: Black/Silver'
      };
      await expectError(
        () =>
          prepareAction({
            products: [{ product: productWithCharacteritics }]
          }),
        {
          errorConfirmation: 'OBPOS_CouldNotFindCharacteristics',
          messageParams: ['productWithCharacteristics']
        }
      );
    });
  });

  describe('prepare product attributes', () => {
    it('product with attribute', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_EnableSupportForProductAttributes'
      );
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('1234');
      const productWithAttributes = {
        ...Product.productA,
        hasAttributes: true
      };
      const payload = {
        products: [{ product: productWithAttributes, qty: 1 }]
      };
      const newPayload = await prepareAction(payload, Ticket.attributeLine);
      expect(newPayload.products[0]).toMatchObject({
        ...payload.products[0],
        options: { line: '1' },
        attrs: {
          attributeSearchAllowed: true,
          attributeValue: '1234',
          productHavingSameAttribute: true
        }
      });
    });

    it('multiple products with attributes', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_EnableSupportForProductAttributes'
      );
      OB.App.View.DialogUIHandler.inputData
        .mockResolvedValueOnce('1234')
        .mockResolvedValueOnce('abcd');
      const productWithAttributesA = {
        ...Product.productA,
        hasAttributes: true
      };
      const productWithAttributesB = {
        ...Product.productB,
        hasAttributes: true
      };
      const payload = {
        products: [
          { product: productWithAttributesA, qty: 1 },
          { product: productWithAttributesB, qty: 2 }
        ]
      };
      const newPayload = await prepareAction(payload, Ticket.attributeLine);
      expect(newPayload.products).toMatchObject([
        {
          ...payload.products[0],
          options: { line: '1' },
          attrs: {
            attributeSearchAllowed: true,
            attributeValue: '1234',
            productHavingSameAttribute: true
          }
        },
        {
          ...payload.products[1],
          attrs: {
            attributeSearchAllowed: true,
            attributeValue: 'abcd',
            productHavingSameAttribute: false
          }
        }
      ]);
    });

    it('product with attribute and option line', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_EnableSupportForProductAttributes'
      );
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('1234');
      const productWithAttributes = {
        ...Product.productA,
        hasAttributes: true
      };
      const payload = {
        products: [
          { product: productWithAttributes, qty: 1, options: { line: '1' } }
        ]
      };
      const newPayload = await prepareAction(payload, Ticket.attributeLine);
      expect(newPayload.products[0]).toMatchObject({
        ...payload.products[0],
        attrs: {
          attributeSearchAllowed: true,
          productHavingSameAttribute: true
        }
      });
    });

    it('product with attribute and different attribute', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_EnableSupportForProductAttributes'
      );
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('5678');
      const productWithAttributes = {
        ...Product.productA,
        hasAttributes: true
      };
      const payload = {
        products: [{ product: productWithAttributes, qty: 1 }]
      };
      const newPayload = await prepareAction(payload, Ticket.attributeLine);
      expect(newPayload.products[0]).toMatchObject({
        ...payload.products[0],
        attrs: {
          attributeSearchAllowed: true,
          attributeValue: '5678',
          productHavingSameAttribute: false
        }
      });
    });

    it('product with attribute and different attribute in line', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_EnableSupportForProductAttributes'
      );
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('1234');
      const productWithAttributes = {
        ...Product.productA,
        hasAttributes: true
      };
      const payload = {
        products: [{ product: productWithAttributes, qty: 1 }]
      };
      const newPayload = await prepareAction(payload, Ticket.singleLine);
      expect(newPayload.products[0]).toMatchObject({
        ...payload.products[0],
        attrs: {
          attributeSearchAllowed: true,
          attributeValue: '1234',
          productHavingSameAttribute: false
        }
      });
    });

    it('product with attribute in quotation (allowed)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p =>
          p === 'OBPOS_EnableSupportForProductAttributes' ||
          p === 'OBPOS_AskForAttributesWhenCreatingQuotation'
      );
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('1234');
      const productWithAttributes = {
        ...Product.productA,
        hasAttributes: true
      };
      const payload = {
        products: [{ product: productWithAttributes, qty: 1 }]
      };
      const quotation = { ...Ticket.attributeLine, isQuotation: true };
      const newPayload = await prepareAction(payload, quotation);
      expect(newPayload.products[0]).toMatchObject({
        ...payload.products[0],
        options: { line: '1' },
        attrs: {
          attributeSearchAllowed: true,
          attributeValue: '1234',
          productHavingSameAttribute: true
        }
      });
    });

    it('product with attribute in quotation (not allowed)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_EnableSupportForProductAttributes'
      );
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('1234');
      const productWithAttributes = {
        ...Product.productA,
        hasAttributes: true
      };
      const payload = {
        products: [{ product: productWithAttributes, qty: 1 }]
      };
      const quotation = { ...Ticket.attributeLine, isQuotation: true };
      const newPayload = await prepareAction(payload, quotation);
      expect(newPayload.products[0]).toMatchObject({
        ...payload.products[0],
        attrs: {
          attributeSearchAllowed: true,
          productHavingSameAttribute: false
        }
      });
    });

    it('no attribute value provided by user', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_EnableSupportForProductAttributes'
      );
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce(null);
      const productWithAttributes = {
        ...Product.productA,
        hasAttributes: true
      };
      await expect(
        prepareAction({
          products: [{ product: productWithAttributes, qty: 1 }]
        })
      ).rejects.toThrow(
        `No attribute provided for product ${productWithAttributes.id}`
      );
    });

    it('check serial attribute', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_EnableSupportForProductAttributes'
      );
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('1234');
      const productWithSerialAttributes = {
        ...Product.productA,
        hasAttributes: true,
        isSerialNo: true
      };
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: productWithSerialAttributes, qty: 1 }]
            },
            Ticket.attributeLine
          ),
        {
          errorConfirmation: 'OBPOS_ProductDefinedAsSerialNo'
        }
      );
    });
  });

  describe('prepare related services', () => {
    it('product with related services (local)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        property => property !== 'OBPOS_remote.product'
      );
      OB.App.MasterdataModels.Product.find.mockResolvedValueOnce([
        {
          id: 'dummy'
        }
      ]);
      const payload = {
        products: [{ product: Product.productA, qty: 1 }]
      };

      const newPayload = await prepareAction(payload);
      expect(newPayload.products).toMatchObject([
        {
          product: Product.productA,
          qty: 1,
          attrs: { hasRelatedServices: true, hasMandatoryServices: false }
        }
      ]);
    });

    it('product with mandatory services (local)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        property => property !== 'OBPOS_remote.product'
      );
      OB.App.MasterdataModels.Product.find.mockResolvedValueOnce([
        {
          id: 'dummy',
          proposalType: 'MP'
        }
      ]);
      const payload = {
        products: [{ product: Product.productA, qty: 1 }]
      };

      const newPayload = await prepareAction(payload);
      expect(newPayload.products).toMatchObject([
        {
          product: Product.productA,
          qty: 1,
          attrs: { hasRelatedServices: true, hasMandatoryServices: true }
        }
      ]);
    });

    it('related services retrieval fails (local)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        property => property !== 'OBPOS_remote.product'
      );
      OB.App.MasterdataModels.Product.find.mockRejectedValue(new Error());
      const payload = {
        products: [{ product: Product.productA, qty: 1 }]
      };

      const newPayload = await prepareAction(payload);
      expect(newPayload.products[0].hasRelatedServices).toBeFalsy();
      expect(newPayload.products[0].hasMandatoryServices).toBeFalsy();
    });

    it('product with related services (remote)', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.Request.mobileServiceRequest.mockResolvedValueOnce({
        response: { data: { hasservices: true, hasmandatoryservices: false } }
      });
      const payload = {
        products: [{ product: Product.productA, qty: 1 }]
      };

      const newPayload = await prepareAction(payload);
      expect(newPayload.products).toMatchObject([
        {
          product: Product.productA,
          qty: 1,
          attrs: { hasRelatedServices: true, hasMandatoryServices: false }
        }
      ]);
    });

    it('related services retrieval fails (remote)', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.Request.mobileServiceRequest.mockRejectedValue(new Error());
      const payload = {
        products: [{ product: Product.productA, qty: 1 }]
      };

      const newPayload = await prepareAction(payload);
      expect(newPayload.products[0].hasRelatedServices).toBeFalsy();
      expect(newPayload.products[0].hasMandatoryServices).toBeFalsy();
    });

    it('service does not have related services', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      const service = { ...Product.productA, productType: 'S' };
      const payload = {
        products: [{ product: service, qty: 1 }]
      };

      const newPayload = await prepareAction(payload);
      expect(newPayload.products[0].hasRelatedServices).toBeFalsy();
      expect(newPayload.products[0].hasMandatoryServices).toBeFalsy();
    });

    it('skip related services preparation (isSilentAddProduct)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        property => property !== 'OBPOS_remote.product'
      );
      OB.App.MasterdataModels.Product.find.mockResolvedValueOnce([
        {
          id: 'dummy'
        }
      ]);
      const payload = {
        products: [
          {
            product: Product.productA,
            qty: 1,
            options: { isSilentAddProduct: true }
          }
        ]
      };

      const newPayload = await prepareAction(payload);
      expect(newPayload.products[0].hasRelatedServices).toBeFalsy();
      expect(newPayload.products[0].hasMandatoryServices).toBeFalsy();
    });

    it('skip related services preparation (originalOrderLineId)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        property => property !== 'OBPOS_remote.product'
      );
      OB.App.MasterdataModels.Product.find.mockResolvedValueOnce([
        {
          id: 'dummy'
        }
      ]);
      const payload = {
        products: [
          {
            product: Product.productA,
            qty: 1,
            attrs: { originalOrderLineId: {} }
          }
        ]
      };

      const newPayload = await prepareAction(payload);
      expect(newPayload.products[0].hasRelatedServices).toBeFalsy();
      expect(newPayload.products[0].hasMandatoryServices).toBeFalsy();
    });
  });

  describe('prepare product price (multi pricelist)', () => {
    it('multi pricelist disabled', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        property => property !== 'EnableMultiPriceList'
      );
      const crossStoreProduct = {
        ...Product.productA,
        crossStore: true
      };
      const payload = {
        products: [
          { product: crossStoreProduct, qty: 1, options: {}, attrs: {} }
        ]
      };
      const newPayload = await prepareAction(payload);
      expect(newPayload).toMatchObject(payload);
    });

    it('product update price from pricelist disabled', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      const crossStoreProduct = {
        ...Product.productA,
        crossStore: true,
        updatePriceFromPricelist: false
      };
      const payload = {
        products: [
          { product: crossStoreProduct, qty: 1, options: {}, attrs: {} }
        ]
      };
      const newPayload = await prepareAction(payload);
      expect(newPayload).toMatchObject(payload);
    });

    it('packs are ignored', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      const crossStoreProduct = {
        ...Product.productA,
        ispack: true
      };
      const payload = {
        products: [
          { product: crossStoreProduct, qty: 1, options: {}, attrs: {} }
        ]
      };
      const newPayload = await prepareAction(payload);
      expect(newPayload).toMatchObject(payload);
    });

    it('cross store product without price', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      const crossStoreProduct = {
        ...Product.productA,
        crossStore: true,
        productPrices: []
      };
      await expectError(
        () =>
          prepareAction({
            products: [{ product: crossStoreProduct, qty: 1 }]
          }),
        {
          warningMsg: 'OBPOS_ProductNotFoundInPriceList'
        }
      );
    });

    it('change price of cross store product', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      const productPrice = {
        priceListId: '5D47B13F42A44352B09C97A72EE42ED8',
        price: 23
      };
      const crossStoreProduct = {
        ...Product.productA,
        crossStore: true,
        productPrices: [productPrice]
      };
      const payload = {
        products: [{ product: crossStoreProduct, qty: 1 }]
      };
      const newPayload = await prepareAction(payload);
      expect(newPayload).toMatchObject({
        products: [
          {
            product: {
              ...crossStoreProduct,
              standardPrice: productPrice.price,
              listPrice: productPrice.price,
              currentPrice: productPrice
            },
            qty: 1
          }
        ]
      });
    });

    it('no price for product in pricelist', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        property => property !== 'OBPOS_remote.product'
      );
      OB.App.MasterdataModels.ProductPrice.find.mockResolvedValueOnce([]);
      const otherPriceList = 'C7F693B202DE472EA7CF3AD23CCBAD89';
      const ticket = {
        ...Ticket.empty,
        priceList: otherPriceList
      };
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: Product.productA, qty: 1 }]
            },
            ticket
          ),
        {
          warningMsg: 'OBPOS_ProductNotFoundInPriceList'
        }
      );
    });

    it('no price for product in pricelist (remote)', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.DAL.remoteSearch.mockResolvedValue([]);
      const otherPriceList = 'C7F693B202DE472EA7CF3AD23CCBAD89';
      const ticket = {
        ...Ticket.empty,
        priceList: otherPriceList
      };
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: Product.productA, qty: 1 }]
            },
            ticket
          ),
        {
          warningMsg: 'OBPOS_ProductNotFoundInPriceList'
        }
      );
    });

    it('change price of product', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        property => property !== 'OBPOS_remote.product'
      );
      OB.App.MasterdataModels.ProductPrice.find.mockResolvedValueOnce([
        { pricestd: 23, pricelist: 29 }
      ]);
      const otherPriceList = 'C7F693B202DE472EA7CF3AD23CCBAD89';
      const ticket = {
        ...Ticket.empty,
        priceList: otherPriceList
      };
      const payload = {
        products: [{ product: Product.productA, qty: 1 }]
      };
      const newPayload = await prepareAction(payload, ticket);
      expect(newPayload).toMatchObject({
        products: [
          {
            product: {
              ...Product.productA,
              standardPrice: 23,
              listPrice: 29
            },
            qty: 1
          }
        ]
      });
    });

    it('change price of product (remote)', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.DAL.remoteSearch.mockResolvedValue([
        { pricestd: 23, pricelist: 29 }
      ]);
      const otherPriceList = 'C7F693B202DE472EA7CF3AD23CCBAD89';
      const ticket = {
        ...Ticket.empty,
        priceList: otherPriceList
      };
      const payload = {
        products: [{ product: Product.productA, qty: 1 }]
      };
      const newPayload = await prepareAction(payload, ticket);
      expect(newPayload).toMatchObject({
        products: [
          {
            product: {
              ...Product.productA,
              standardPrice: 23,
              listPrice: 29
            },
            qty: 1
          }
        ]
      });
    });
  });

  describe('approvals', () => {
    const service = { ...Product.productA, productType: 'S', returnable: true };

    it('delete line approval', async () => {
      const payloadWithApproval = {
        products: [{ product: service }],
        approvals: ['OBPOS_approval.deleteLine']
      };
      OB.App.Security.requestApprovalForAction.mockResolvedValue(
        payloadWithApproval
      );
      const newPayload = await prepareAction(
        {
          products: [
            { product: Product.productB, qty: -1, options: { line: '1' } }
          ]
        },
        Ticket.returned
      );
      expect(newPayload).toEqual(payloadWithApproval);
    });

    it('return service accepted approval', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_ReturnLine'
      );
      const payloadWithApproval = {
        products: [{ product: service }],
        approvals: ['OBPOS_approval.returnService']
      };
      OB.App.Security.requestApprovalForAction.mockResolvedValue(
        payloadWithApproval
      );
      const newPayload = await prepareAction(
        { products: [{ product: service, qty: 1 }] },
        Ticket.emptyReturn
      );
      expect(newPayload).toEqual(payloadWithApproval);
    });

    it('returnable service in negative line accepted approval', async () => {
      const payloadWithApproval = {
        products: [{ product: service }],
        approvals: ['OBPOS_approval.returnService']
      };
      OB.App.Security.requestApprovalForAction.mockResolvedValue(
        payloadWithApproval
      );
      const newPayload = await prepareAction(
        {
          products: [
            {
              product: service,
              qty: 1,
              attrs: { relatedLines: [{ orderlineId: '1' }] }
            }
          ]
        },
        Ticket.returnedLine
      );
      expect(newPayload).toEqual(payloadWithApproval);
    });

    it('return service rejected approval', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p === 'OBPOS_ReturnLine'
      );
      OB.App.Security.requestApprovalForAction.mockRejectedValue(
        new OB.App.Class.ActionCanceled('Approval required', {
          approvalRequired: 'OBPOS_approval.returnService'
        })
      );
      await expect(
        prepareAction(
          {
            products: [{ product: service, qty: 1 }]
          },
          Ticket.emptyReturn
        )
      ).rejects.toThrow(`Approval required`);
    });
  });
});
