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
    Security: { hasPermission: jest.fn(), requestApprovalForAction: jest.fn() }
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
      taxCategoryBOM: []
    }
  }
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

const emptyTicket = {
  priceIncludesTax: true,
  lines: [],
  businessPartner: { id: 'dummy' }
};

const scaleProduct = {
  id: 'scaleProduct',
  obposScale: true,
  uOMstandardPrecision: 3,
  standardPrice: 10,
  listPrice: 11
};

const prepareAction = async (payload, ticket = emptyTicket) => {
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

  if (expectedError instanceof OB.App.Class.ActionSilentlyCanceled) {
    expect(error).toBeInstanceOf(OB.App.Class.ActionSilentlyCanceled);
    expect(error.message).toEqual(expectedError.message);
  } else {
    expect(error).toMatchObject({ info: expectedError });
  }
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

  describe('scale products', () => {
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

    it('handles no stock ', async () => {
      OB.App.StockChecker.hasStock.mockResolvedValueOnce(false);

      await expectError(
        () =>
          prepareAction({
            products: [{ product: scaleProduct }]
          }),
        new OB.App.Class.ActionSilentlyCanceled(
          `Add product canceled: there is no stock of product ${scaleProduct.id}`
        )
      );
    });
  });
});
