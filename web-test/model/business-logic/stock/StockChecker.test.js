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
    StateBackwardCompatibility: {
      getInstance: jest.fn(() => {
        return {
          toBackboneObject: jest.fn()
        };
      })
    }
  },
  MobileApp: {
    model: {}
  },
  Model: {
    Product: jest.fn(product => product)
  }
};

require('../../../../web/org.openbravo.retail.posterminal/app/model/business-logic/stock/StockChecker');

describe('OB.App.StockChecker', () => {
  const stockChecker = OB.App.StockChecker;
  const productA = '18C6723E5B53403D84D93D61F840FA30';
  const productB = '573FF496A9DC412FA3CBC0392BAADBD1';

  stockChecker.checkLineStock = jest.fn(product => product.id !== productB);

  describe('API', () => {
    test('has stock', async () => {
      const hasStock = await stockChecker.hasStock({ id: productA }, 2);
      expect(hasStock).toBeTruthy();
    });

    test('does not have stock', async () => {
      const hasStock = await stockChecker.hasStock({ id: productB }, 2);
      expect(hasStock).toBeFalsy();
    });
  });
});
