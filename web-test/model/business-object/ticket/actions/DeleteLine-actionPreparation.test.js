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
    Class: {},
    DAL: { find: jest.fn() },
    MasterdataModels: {
      ProductBOM: { find: jest.fn() },
      ProductCharacteristicValue: { find: jest.fn() },
      ProductServiceLinked: { find: jest.fn() }
    },
    Security: { hasPermission: jest.fn(), requestApprovalForAction: jest.fn() },
    StateBackwardCompatibility: { setProperties: jest.fn() },
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

require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/Ticket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/DeleteLine');

let Ticket = {
  empty: {
    lines: [],
    businessPartner: { id: '1' },
    orderType: 0
  }
};

Ticket = {
  ...Ticket,

  services: {
    ...Ticket.empty,
    hasServices: true,
    lines: [{ id: 'l1' }, { id: 'l2', relatedLines: [{ orderlineId: 'l1' }] }]
  }
};

const prepareAction = async (payload, ticket = Ticket.empty) => {
  const newPayload = await executeActionPreparations(
    OB.App.StateAPI.Ticket.deleteLine,
    deepfreeze({ Ticket: ticket }),
    deepfreeze(payload)
  );
  return newPayload;
};

// eslint-disable-next-line no-unused-vars
const expectError = async (action, expectedError) => {
  let error;
  try {
    await action();
  } catch (e) {
    error = e;
  }
  expect(error).toMatchObject({ info: expectedError });
};

describe('deleteLine preparation', () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  describe('related services lines', () => {
    it('deletes 1 level', async () => {
      const payload = await prepareAction(
        {
          lineIds: ['l1']
        },
        Ticket.services
      );

      expect(payload.lineIds).toEqual(expect.arrayContaining(['l1', 'l2']));
    });
  });
});
