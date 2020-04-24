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

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/State');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionCanceled');
require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionSilentlyCanceled');

require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/SetLinePrice');

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
  const basicTicket = {
    Ticket: {
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
    }
  };

  const basicReturn = {
    Ticket: {
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
    }
  };

  const persistence = {
    initialize: jest.fn(),
    getState: jest.fn(() => basicTicket),
    dispatch: jest.fn()
  };
  const state = new OB.App.Class.State(persistence);

  beforeEach(() => {
    jest.clearAllMocks();
    persistence.getState = jest.fn(() => basicTicket);
    OB.App.Security.hasPermission.mockReturnValue(true);
  });

  describe('parameter validation', () => {
    it('checks line ids parameter is present', async () => {
      await expect(state.Ticket.setLinePrice({ price: 5 })).rejects.toThrow();
    });

    it('checks line ids is an array', async () => {
      await expect(
        state.Ticket.setLinePrice({ lineIds: '1', price: 5 })
      ).rejects.toThrow();

      await expect(
        state.Ticket.setLinePrice({ lineIds: ['1'], price: 5 })
      ).resolves.not.toThrow();
    });

    it('line ids exists', async () => {
      await expect(
        state.Ticket.setLinePrice({ lineIds: ['1', 'dummy'], price: 5 })
      ).rejects.toThrow();
    });

    it('checks price parameter is present', async () => {
      await expect(
        state.Ticket.setLinePrice({ lineIds: ['1'] })
      ).rejects.toThrow();
    });

    it('checks price is a numeric value', async () => {
      await expect(
        state.Ticket.setLinePrice({ lineIds: ['1'], price: 'dummy' })
      ).rejects.toThrow();
    });

    it('checks price is >= 0', async () => {
      await expect(
        state.Ticket.setLinePrice({ lineIds: ['1'], price: -1 })
      ).rejects.toThrow();
    });

    it('can set price=0', async () => {
      await expect(
        state.Ticket.setLinePrice({ lineIds: ['1'], price: 0 })
      ).resolves.not.toThrow();
    });
  });

  describe('restrictions', () => {
    it('cannot set price to replaced return lines', async () => {
      persistence.getState.mockReturnValue({
        Ticket: {
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
      });

      await expectError(
        () => state.Ticket.setLinePrice({ lineIds: ['1'], price: 5 }),
        { errorConfirmation: 'OBPOS_CancelReplaceReturnPriceChange' }
      );
    });

    it('cannot set price to not editable ticket', async () => {
      persistence.getState.mockReturnValue({
        Ticket: { ...basicTicket.Ticket, isEditable: false }
      });

      await expectError(
        () => state.Ticket.setLinePrice({ lineIds: ['1'], price: 5 }),
        { errorConfirmation: 'OBPOS_modalNoEditableBody' }
      );
    });

    it('cannot set price if product price is not editable 1', async () => {
      persistence.getState.mockReturnValue({
        Ticket: {
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
      });

      await expectError(
        () => state.Ticket.setLinePrice({ lineIds: ['1'], price: 5 }),
        { errorConfirmation: 'OBPOS_modalNoEditableLineBody' }
      );
    });

    it('cannot set price if product price is not editable 2', async () => {
      persistence.getState.mockReturnValue({
        Ticket: {
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
      });

      await expectError(
        () => state.Ticket.setLinePrice({ lineIds: ['1'], price: 5 }),
        { errorConfirmation: 'OBPOS_modalNoEditableLineBody' }
      );
    });

    it('approval is requested if ChangeServicePriceNeedApproval is not granted', async () => {
      OB.App.Security.hasPermission = jest.fn(
        p => p !== 'OBPOS_ChangeServicePriceNeedApproval'
      );

      await state.Ticket.setLinePrice({ lineIds: ['1'], price: 15 });
      expect(OB.App.Security.requestApprovalForAction).toHaveBeenCalled();
    });

    describe('verified return', () => {
      it('cannot increase price without permission', async () => {
        persistence.getState.mockReturnValue(basicReturn);

        await expectError(
          () => state.Ticket.setLinePrice({ lineIds: ['1'], price: 15 }),
          { errorMsg: 'OBPOS_CannotChangePrice' }
        );
      });

      it('cannot decrease price without permission', async () => {
        persistence.getState.mockReturnValue(basicReturn);
        OB.App.Security.hasPermission = jest.fn(
          p => p !== 'OBPOS_ModifyPriceVerifiedReturns'
        );

        await expectError(
          () => state.Ticket.setLinePrice({ lineIds: ['1'], price: 5 }),
          { errorMsg: 'OBPOS_CannotChangePrice' }
        );
      });

      it('can decrease price with permission', async () => {
        persistence.getState.mockReturnValue(basicReturn);
        OB.App.Security.hasPermission.mockReturnValue(true);

        await expect(
          state.Ticket.setLinePrice({ lineIds: ['1'], price: 5 })
        ).resolves.not.toThrow();
      });

      it('cannot increse price even with permission', async () => {
        persistence.getState.mockReturnValue(basicReturn);
        OB.App.Security.hasPermission.mockReturnValue(true);

        await expectError(
          () => state.Ticket.setLinePrice({ lineIds: ['1'], price: 15 }),
          { errorMsg: 'OBPOS_CannotChangePrice' }
        );
      });
    });
  });
});
