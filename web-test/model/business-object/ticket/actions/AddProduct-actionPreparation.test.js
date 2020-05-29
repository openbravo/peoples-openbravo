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

OB = {
  App: {
    StateBackwardCompatibility: { setProperties: jest.fn() },
    Class: {},
    MasterdataModels: {
      ProductBOM: { find: jest.fn() },
      ProductCharacteristicValue: { find: jest.fn() }
    },
    Security: { hasPermission: jest.fn(), requestApprovalForAction: jest.fn() },
    View: { DialogUIHandler: { inputData: jest.fn() } }
  },

  UTIL: {
    HookManager: { registerHook: jest.fn() }
  },

  POS: {
    hwserver: {
      getAsyncWeight: jest.fn()
    }
  },

  Taxes: {
    Pos: {
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

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionCanceled');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionSilentlyCanceled');

require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-logic/stock/StockChecker');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/AddProduct');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/util/UUID');

OB.App.Class.Criteria = class MockedCriteria {
  criterion() {
    return { build: jest.fn() };
  }
};

const Product = {
  base: {
    id: '0',
    _identifier: 'test product',
    uOMstandardPrecision: 3,
    standardPrice: 10,
    listPrice: 11
  }
};

const Ticket = {
  empty: {
    priceIncludesTax: true,
    lines: [],
    businessPartner: { id: '1' },
    orderType: 0
  },
  emptyReturn: {
    priceIncludesTax: true,
    lines: [],
    businessPartner: { id: '1' },
    orderType: 1
  },
  singleLine: {
    priceIncludesTax: true,
    lines: [{ id: '1', product: Product.base, qty: 1 }],
    businessPartner: { id: '1' },
    orderType: 0
  },
  returnedLine: {
    priceIncludesTax: true,
    lines: [{ id: '1', product: Product.base, qty: -2 }],
    businessPartner: { id: '1' },
    orderType: 0
  },
  attributeLine: {
    priceIncludesTax: true,
    lines: [{ id: '1', product: Product.base, qty: 1, attributeValue: '1234' }],
    businessPartner: { id: '1' },
    orderType: 0
  },
  cancelAndReplace: {
    priceIncludesTax: true,
    lines: [
      { id: '1', product: Product.base, qty: -1, replacedorderline: true }
    ],
    businessPartner: { id: '1' },
    orderType: 0
  }
};

const prepareAction = async (payload, ticket = Ticket.empty) => {
  const newPayload = await executeActionPreparations(
    OB.App.StateAPI.Ticket.addProduct,
    deepfreeze({ Ticket: ticket }),
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
  beforeEach(() => {
    jest.resetAllMocks();
    // default mocks
    OB.POS.hwserver.getAsyncWeight.mockImplementation(() =>
      Promise.resolve({ result: 10 })
    );
    OB.App.StockChecker.hasStock = jest.fn().mockResolvedValue(true);
  });

  describe('check restrictions', () => {
    it('product without price check', async () => {
      await expectError(
        () =>
          prepareAction({
            products: [{ product: { ...Product.base, listPrice: undefined } }]
          }),
        {
          warningMsg: 'OBPOS_productWithoutPriceInPriceList',
          messageParams: ['test product']
        }
      );
    });

    it('generic product check', async () => {
      await expectError(
        () =>
          prepareAction({
            products: [{ product: { ...Product.base, isGeneric: true } }]
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
              products: [{ product: Product.base }],
              options: { line: '1' }
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
                    ...Product.base,
                    oBPOSAllowAnonymousSale: false
                  }
                }
              ],
              options: { businessPartner: '1' }
            },
            Ticket.empty
          ),
        {
          errorConfirmation: 'OBPOS_AnonymousSaleNotAllowed'
        }
      );
    });

    it('not returnable check (1)', async () => {
      const unReturnableProduct = { ...Product.base, returnable: false };
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: unReturnableProduct, qty: 1 }]
            },
            Ticket.emptyReturn
          ),
        {
          errorConfirmation: 'OBPOS_UnreturnableProductMessage'
        }
      );
    });

    it('not returnable check (2)', async () => {
      const unReturnableProduct = { ...Product.base, returnable: false };
      await expectError(
        () =>
          prepareAction(
            {
              products: [{ product: unReturnableProduct, qty: 1 }],
              options: { line: '1' }
            },
            Ticket.returnedLine
          ),
        {
          errorConfirmation: 'OBPOS_UnreturnableProductMessage'
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
              products: [{ product: Product.base, qty: 1 }]
            },
            closedQuotation
          ),
        {
          errorMsg: 'OBPOS_QuotationClosed'
        }
      );
    });

    it('product locked', async () => {
      // TODO once finished to implemented
    });
  });

  describe('scale products', () => {
    const scaleProduct = { ...Product.base, obposScale: true };
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
          products: [{ product: Product.base }]
        })
      ).rejects.toThrow(
        `Add product canceled: there is no stock of product ${Product.base.id}`
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
          product: Product.base.id
        }
      ]);
      const productWithBOM = {
        ...Product.base,
        taxCategory: 'FF80818123B7FC160123B804AB8C0019'
      };
      const payload = {
        products: [{ product: productWithBOM, qty: 1 }]
      };
      const newPayload = await prepareAction(payload, Ticket.empty);
      expect(newPayload.products[0]).toEqual({
        product: {
          ...payload.products[0].product,
          productBOM: [
            {
              amount: 30,
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
        ...Product.base,
        taxCategory: 'FF80818123B7FC160123B804AB8C0019'
      };
      const payload = {
        products: [{ product: productWithBOM, qty: 1 }]
      };
      const newPayload = await prepareAction(payload, Ticket.empty);
      expect(newPayload.products).toEqual(payload.products);
    });

    it('product BOM nas no price', async () => {
      OB.App.MasterdataModels.ProductBOM.find.mockResolvedValueOnce([
        {
          active: true,
          bomproduct: 'F0659DF0BC634D38855D4D86082B7AA1',
          bomquantity: 2,
          bomtaxcategory: 'FF80818123B7FC160123B804AB8C0019',
          id: '32182CDA9D544392A092A913A68AFEC1',
          product: Product.base.id
        }
      ]);
      const productWithBOM = {
        ...Product.base,
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

  describe('prepare product characteristics', () => {
    it('product has characteristics', async () => {
      const characteristics = [
        {
          active: true,
          characteristic: '015D6C6072AC4A13B7573A261B2011BC',
          characteristicValue: 'C6A9CF2813DC49C1BE5A9A6094DD967E',
          id: '1983C55A254D41A7AD2E17E84CA5F70A',
          obposFilteronwebpos: true,
          product: Product.base.id,
          _identifier: 'Color'
        }
      ];
      OB.App.MasterdataModels.ProductCharacteristicValue.find.mockResolvedValueOnce(
        characteristics
      );
      const productWithCharacteritics = {
        ...Product.base,
        _identifier: 'productWithCharacteristics',
        characteristicDescription: 'Color: Black/Silver'
      };
      const payload = {
        products: [{ product: productWithCharacteritics, qty: 1 }]
      };
      const newPayload = await prepareAction(payload, Ticket.empty);
      expect(newPayload.products[0]).toEqual({
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
        ...Product.base,
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
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('1234');
      const productWithAttributes = {
        ...Product.base,
        hasAttributes: true
      };
      const payload = {
        products: [{ product: productWithAttributes, qty: 1 }],
        options: {}
      };
      const newPayload = await prepareAction(payload, Ticket.attributeLine);
      expect(newPayload).toEqual({
        ...payload,
        line: '1',
        attrs: {
          attributeSearchAllowed: true,
          attributeValue: '1234',
          productHavingSameAttribute: true
        }
      });
    });

    it('product with attribute and option line', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('1234');
      const productWithAttributes = {
        ...Product.base,
        hasAttributes: true
      };
      const payload = {
        products: [{ product: productWithAttributes, qty: 1 }],
        options: { line: '1' }
      };
      const newPayload = await prepareAction(payload, Ticket.attributeLine);
      expect(newPayload).toEqual({
        ...payload,
        attrs: {
          attributeSearchAllowed: true,
          productHavingSameAttribute: true
        }
      });
    });

    it('product with attribute and different attribute', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('5678');
      const productWithAttributes = {
        ...Product.base,
        hasAttributes: true
      };
      const payload = {
        products: [{ product: productWithAttributes, qty: 1 }],
        options: {}
      };
      const newPayload = await prepareAction(payload, Ticket.attributeLine);
      expect(newPayload).toEqual({
        ...payload,
        attrs: {
          attributeSearchAllowed: true,
          attributeValue: '5678',
          productHavingSameAttribute: false
        }
      });
    });

    it('product with attribute and different attribute in line', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('1234');
      const productWithAttributes = {
        ...Product.base,
        hasAttributes: true
      };
      const payload = {
        products: [{ product: productWithAttributes, qty: 1 }],
        options: {}
      };
      const newPayload = await prepareAction(payload, Ticket.singleLine);
      expect(newPayload).toEqual({
        ...payload,
        attrs: {
          attributeSearchAllowed: true,
          attributeValue: '1234',
          productHavingSameAttribute: false
        }
      });
    });

    it('product with attribute in quotation (allowed)', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('1234');
      const productWithAttributes = {
        ...Product.base,
        hasAttributes: true
      };
      const payload = {
        products: [{ product: productWithAttributes, qty: 1 }],
        options: {}
      };
      const quotation = { ...Ticket.attributeLine, isQuotation: true };
      const newPayload = await prepareAction(payload, quotation);
      expect(newPayload).toEqual({
        ...payload,
        line: '1',
        attrs: {
          attributeSearchAllowed: true,
          attributeValue: '1234',
          productHavingSameAttribute: true
        }
      });
    });

    it('product with attribute in quotation (not allowed)', async () => {
      OB.App.Security.hasPermission.mockImplementation(
        p => p !== 'OBPOS_AskForAttributesWhenCreatingQuotation'
      );
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('1234');
      const productWithAttributes = {
        ...Product.base,
        hasAttributes: true
      };
      const payload = {
        products: [{ product: productWithAttributes, qty: 1 }],
        options: {}
      };
      const quotation = { ...Ticket.attributeLine, isQuotation: true };
      const newPayload = await prepareAction(payload, quotation);
      expect(newPayload).toEqual({
        ...payload,
        attrs: {
          attributeSearchAllowed: true,
          productHavingSameAttribute: false
        }
      });
    });

    it('no attribute value provided by user', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce(null);
      const productWithAttributes = {
        ...Product.base,
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
      OB.App.Security.hasPermission.mockReturnValue(true);
      OB.App.View.DialogUIHandler.inputData.mockResolvedValueOnce('5678');
      const productWithSerialAttributes = {
        ...Product.base,
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

    it('more than one is not allowed', async () => {
      OB.App.Security.hasPermission.mockReturnValue(true);
      const productWithAttributes = {
        ...Product.base,
        hasAttributes: true
      };
      await expect(
        prepareAction({
          products: [
            { product: productWithAttributes },
            { product: productWithAttributes }
          ]
        })
      ).rejects.toThrow('Cannot handle attributes for more than one product');
    });
  });

  describe('approvals', () => {
    const service = { ...Product.base, productType: 'S', returnable: true };
    it('return service accepted approval', async () => {
      const payloadWithApproval = {
        products: [{ product: service }],
        approvals: ['OBPOS_approval.returnService']
      };
      OB.App.Security.requestApprovalForAction.mockResolvedValue(
        payloadWithApproval
      );
      const newPayload = await prepareAction(
        { products: [{ product: service }] },
        Ticket.emptyReturn
      );
      expect(newPayload).toEqual(payloadWithApproval);
    });

    it('return service rejected approval', async () => {
      OB.App.Security.requestApprovalForAction.mockRejectedValue(
        new OB.App.Class.ActionCanceled('Approval required', {
          approvalRequired: 'OBPOS_approval.returnService'
        })
      );
      await expect(
        prepareAction(
          {
            products: [{ product: service }]
          },
          Ticket.emptyReturn
        )
      ).rejects.toThrow(`Approval required`);
    });
  });
});
