/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB = {
  App: {
    Class: {},
    MasterdataModels: {
      Product: { withId: jest.fn() }
    }
  },
  Discounts: {
    Pos: {
      ruleImpls: [
        {
          id: '0',
          _identifier: 'packA',
          products: [
            { product: { id: 'product1' }, obdiscQty: 1 },
            { product: { id: 'product2' }, obdiscQty: 2 }
          ]
        },
        {
          id: '1',
          _identifier: 'packB',
          products: [
            { product: { id: 'product1' }, obdiscQty: 3 },
            { product: { id: 'product2' }, obdiscQty: 3 }
          ],
          endingDate: '2013-07-06T23:01:14+02:00'
        }
      ]
    }
  }
};

require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/exception/TranslatableError');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-logic/pack/ProductPack');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-logic/pack/ProductPackProvider');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-logic/pack/Pack');

describe('product pack infrastructure', () => {
  OB.App.MasterdataModels.Product.withId.mockImplementation(id => {
    return { id };
  });

  describe('OB.App.ProductPackProvider', () => {
    test('find standard pack', async () => {
      const productPack = {
        id: '0',
        ispack: true,
        productCategory: 'BE5D42E554644B6AA262CCB097753951'
      };
      const pack = OB.App.ProductPackProvider.getPack(productPack);
      expect(pack).not.toBeUndefined();
    });

    test('pack not found', async () => {
      const productPack = {
        id: '2',
        ispack: true,
        productCategory: 'unexisting'
      };
      const pack = OB.App.ProductPackProvider.getPack(productPack);
      expect(pack).toBeUndefined();
    });

    test('no pack for regular product', async () => {
      const regularProduct = {
        id: '2'
      };
      const pack = OB.App.ProductPackProvider.getPack(regularProduct);
      expect(pack).toBeUndefined();
    });
  });

  describe('standard pack implementation', () => {
    test('process pack', async () => {
      const productPack = {
        id: '0',
        ispack: true,
        productCategory: 'BE5D42E554644B6AA262CCB097753951'
      };
      const pack = OB.App.ProductPackProvider.getPack(productPack);
      const packProducts = await pack.process();
      expect(packProducts).toEqual([
        { product: { id: 'product1' }, qty: 1, belongsToPack: true },
        { product: { id: 'product2' }, qty: 2, belongsToPack: true }
      ]);
    });

    test('expired pack', async () => {
      const productPack = {
        id: '1',
        ispack: true,
        productCategory: 'BE5D42E554644B6AA262CCB097753951'
      };
      const pack = OB.App.ProductPackProvider.getPack(productPack);
      let error;
      try {
        await pack.process();
      } catch (e) {
        error = e.data;
      }
      expect(error).toEqual({
        title: 'OBPOS_PackExpired_header',
        errorMsg: 'OBPOS_PackExpired_body',
        messageParams: [
          // eslint-disable-next-line no-underscore-dangle
          'packB',
          new Date('2013-07-06T23:01:14+02:00').toLocaleDateString()
        ]
      });
    });
  });
});
