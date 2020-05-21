/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2020 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
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
