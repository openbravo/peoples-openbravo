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

/**
 * @fileoverview performs tests on setQuantity action preparation
 * @see SetQuantity.test for unit tests on setQuantity action
 **/

OB = {
  App: {
    StateBackwardCompatibility: { setProperties: jest.fn() },
    Class: {},
    Security: { hasPermission: jest.fn(), requestApprovalForAction: jest.fn() }
  },
  UTIL: {
    HookManager: { registerHook: jest.fn() }
  }
};

global.lodash = require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
const {
  executeActionPreparations
} = require('../../../../../../org.openbravo.mobile.core/web-test/base/state-utils');

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionCanceled');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionSilentlyCanceled');

require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/SetLinePrice');

const basicTicket = {
  lines: [
    {
      id: '1',
      qty: 1,
      price: 10,
      priceList: 10,
      isEditable: true,
      product: { listPrice: 10, obposEditablePrice: true, productType: 'I' }
    },
    {
      id: '2',
      qty: 1,
      price: 20,
      priceList: 20,
      isEditable: true,
      product: { listPrice: 20, obposEditablePrice: true, productType: 'I' }
    },
    {
      id: '3',
      qty: 1,
      price: 30,
      priceList: 30,
      isEditable: true,
      product: { listPrice: 30, obposEditablePrice: true, productType: 'I' }
    }
  ]
};

const basicReturn = {
  isPaid: false,
  lines: [
    {
      id: '1',
      qty: 1,
      price: 10,
      priceList: 10,
      isEditable: false,
      originalDocumentNo: '0001',
      product: { listPrice: 10, obposEditablePrice: true }
    }
  ]
};

const prepareAction = async (payload, ticket = basicTicket) => {
  const newPayload = await executeActionPreparations(
    OB.App.StateAPI.Ticket.setLinePrice,
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

describe('Ticket.setQuantity action preparation', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    OB.App.Security.hasPermission.mockReturnValue(true);
  });

  describe('parameter validation', () => {
    it('checks line ids parameter is present', async () => {
      await expect(prepareAction({ price: 5 })).rejects.toThrow();
    });

    it('checks line ids is an array', async () => {
      await expect(prepareAction({ lineIds: '1', price: 5 })).rejects.toThrow();

      await expect(
        prepareAction({ lineIds: ['1'], price: 5 })
      ).resolves.not.toThrow();
    });

    it('line ids exists', async () => {
      await expect(
        prepareAction({ lineIds: ['1', 'dummy'], price: 5 })
      ).rejects.toThrow();
    });

    it('checks price parameter is present', async () => {
      await expect(prepareAction({ lineIds: ['1'] })).rejects.toThrow();
    });

    it('checks price is a numeric value', async () => {
      await expect(
        prepareAction({ lineIds: ['1'], price: 'dummy' })
      ).rejects.toThrow();
    });

    it('checks price is >= 0', async () => {
      await expect(
        prepareAction({ lineIds: ['1'], price: -1 })
      ).rejects.toThrow();
    });

    it('can set price=0', async () => {
      await expect(
        prepareAction({ lineIds: ['1'], price: 0 })
      ).resolves.not.toThrow();
    });
  });

  describe('restrictions', () => {
    it('cannot set price to replaced return lines', async () => {
      await expectError(
        () =>
          prepareAction(
            { lineIds: ['1'], price: 5 },
            {
              lines: [
                {
                  id: '1',
                  qty: -1,
                  replacedorderline: true,
                  price: 10,
                  priceList: 10,
                  product: { listPrice: 10, obposEditablePrice: true }
                }
              ]
            }
          ),
        {
          errorConfirmation: 'OBPOS_CancelReplaceReturnPriceChange'
        }
      );
    });

    it('cannot set price to not editable ticket', async () => {
      await expectError(
        () =>
          prepareAction(
            { lineIds: ['1'], price: 5 },
            { ...basicTicket, isEditable: false }
          ),
        {
          errorConfirmation: 'OBPOS_modalNoEditableBody'
        }
      );
    });

    it('cannot set price if product price is not editable 1', async () => {
      await expectError(
        () =>
          prepareAction(
            { lineIds: ['1'], price: 5 },
            {
              lines: [
                {
                  id: '1',
                  qty: 1,
                  price: 10,
                  priceList: 10,
                  isEditable: true,
                  product: { listPrice: 10, obposEditablePrice: false }
                }
              ]
            }
          ),
        {
          errorConfirmation: 'OBPOS_modalNoEditableLineBody'
        }
      );
    });

    it('cannot set price if product price is not editable 2', async () => {
      await expectError(
        () =>
          prepareAction(
            { lineIds: ['1'], price: 5 },
            {
              lines: [
                {
                  id: '1',
                  qty: 1,
                  price: 10,
                  priceList: 10,
                  isEditable: true,
                  product: {
                    listPrice: 10,
                    obposEditablePrice: true,
                    isEditablePrice: false
                  }
                }
              ]
            }
          ),
        {
          errorConfirmation: 'OBPOS_modalNoEditableLineBody'
        }
      );
    });

    it('approval is requested if ChangeServicePriceNeedApproval is not granted', async () => {
      OB.App.Security.hasPermission = jest.fn(
        p => p !== 'OBPOS_ChangeServicePriceNeedApproval'
      );

      await prepareAction({ lineIds: ['1'], price: 15 });
      expect(OB.App.Security.requestApprovalForAction).toHaveBeenCalled();
    });

    describe('verified return', () => {
      it('cannot increase price without permission', async () => {
        await expectError(
          () => prepareAction({ lineIds: ['1'], price: 15 }, basicReturn),
          { errorMsg: 'OBPOS_CannotChangePrice' }
        );
      });

      it('cannot decrease price without permission', async () => {
        OB.App.Security.hasPermission = jest.fn(
          p => p !== 'OBPOS_ModifyPriceVerifiedReturns'
        );

        await expectError(
          () => prepareAction({ lineIds: ['1'], price: 5 }, basicReturn),
          {
            errorMsg: 'OBPOS_CannotChangePrice'
          }
        );
      });

      it('can decrease price with permission', async () => {
        OB.App.Security.hasPermission.mockReturnValue(true);

        await expect(
          prepareAction({ lineIds: ['1'], price: 5 }, basicReturn)
        ).resolves.not.toThrow();
      });

      it('cannot increse price even with permission', async () => {
        OB.App.Security.hasPermission.mockReturnValue(true);

        await expectError(
          () => prepareAction({ lineIds: ['1'], price: 15 }, basicReturn),
          { errorMsg: 'OBPOS_CannotChangePrice' }
        );
      });
    });
  });
});
