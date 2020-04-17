/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */

/**
 * @fileoverview performs tests on setQuantity action preparation
 * @see SetQuantity.test for unit tests on setQuantity action
 **/

OB = {
  App: { StateBackwardCompatibility: { setProperties: jest.fn() }, Class: {} },
  MobileApp: { model: { hasPermission: jest.fn().mockReturnValue(false) } },
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
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/SetPrice');

describe('Ticket.setQuantity action preparation', () => {
  const basicTicket = {
    Ticket: {
      lines: [
        {
          id: '1',
          qty: 1,
          price: 10,
          priceList: 10,
          product: { listPrice: 10 }
        },
        {
          id: '2',
          qty: 1,
          price: 20,
          priceList: 20,
          product: { listPrice: 20 }
        },
        {
          id: '3',
          qty: 1,
          price: 30,
          priceList: 30,
          product: { listPrice: 30 }
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
    persistence.getState = jest.fn(() => basicTicket);
    jest.clearAllMocks();
  });

  describe('parameter validation', () => {
    it('checks line ids parameter is present', async () => {
      await expect(state.Ticket.setPrice({ price: 5 })).rejects.toThrow();
    });

    it('checks line ids is an array', async () => {
      await expect(
        state.Ticket.setPrice({ lineIds: '1', price: 5 })
      ).rejects.toThrow();

      await expect(
        state.Ticket.setPrice({ lineIds: ['1'], price: 5 })
      ).resolves.not.toThrow();
    });

    it('line ids exists', async () => {
      await expect(
        state.Ticket.setPrice({ lineIds: ['1', 'dummy'], price: 5 })
      ).rejects.toThrow();
    });

    it('checks price parameter is present', async () => {
      await expect(state.Ticket.setPrice({ lineIds: ['1'] })).rejects.toThrow();
    });

    it('checks price is a numeric value', async () => {
      await expect(
        state.Ticket.setPrice({ lineIds: ['1'], price: 'dummy' })
      ).rejects.toThrow();
    });

    it('checks price is >= 0', async () => {
      await expect(
        state.Ticket.setPrice({ lineIds: ['1'], price: -1 })
      ).rejects.toThrow();

      await expect(
        state.Ticket.setPrice({ lineIds: ['1'], price: 0 })
      ).resolves.not.toThrow();
    });
  });
});
