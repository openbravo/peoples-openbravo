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

require('../../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/exception/TranslatableError');
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

  simple: {
    ...Ticket.empty,
    lines: [
      { id: 'l1', product: { id: 'p1', _identifier: 'P1' }, isDeletable: true }
    ]
  },

  cancelAndReplace: {
    ...Ticket.empty,
    replacedorder: true,
    lines: [
      {
        id: 'l1',
        product: { id: 'p1', _identifier: 'P1' },
        qty: 10,
        deliveredQuantity: 5,
        isDeletable: true
      }
    ]
  },
  services: {
    ...Ticket.empty,
    hasServices: true,
    lines: [
      {
        id: 'l1',
        product: {
          id: 'p1',
          _identifier: 'P1',
          productType: 'S',
          quantityRule: 'PP'
        },
        isDeletable: true,
        isEditable: true,
        groupService: false,
        relatedLines: [{ orderlineId: 'l2' }]
      },
      { id: 'l2', qty: 2 }
    ]
  },
  nonDeletable: {
    ...Ticket.empty,
    lines: [
      { id: 'l1', product: { id: 'p1', _identifier: 'P1' }, isDeletable: false }
    ]
  }
};

const prepareAction = async (payload, ticket = Ticket.simple) => {
  const newPayload = await executeActionPreparations(
    OB.App.StateAPI.Ticket.deleteLine,
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

describe('deleteLine preparation', () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  describe('config preparation', () => {
    it.each`
      preference               | property
      ${'OBPOS_remove_ticket'} | ${'saveRemoval'}
    `('prepares config for $property', async ({ preference, property }) => {
      const assertConfig = async set => {
        OB.App.Security.hasPermission.mockImplementation(p =>
          p === preference ? set : undefined
        );
        OB.App.Security.requestApprovalForAction.mockImplementation(
          (property, payload) => payload
        );

        const payload = await prepareAction({
          lineIds: ['l1']
        });

        expect(payload.config[property]).toBe(set);
      };

      await assertConfig(true);
      await assertConfig(false);
    });
  });

  describe('restrictions', () => {
    it('prevents non editable ticket', async () => {
      await expectError(
        () =>
          prepareAction(
            { lineIds: ['l1'] },
            { ...Ticket.simple, isEditable: false }
          ),
        {
          title: 'OBPOS_modalNoEditableHeader',
          errorConfirmation: 'OBPOS_modalNoEditableBody'
        }
      );
    });

    it('prevents cancel&replace with delivered quantity', async () => {
      await expectError(
        () => prepareAction({ lineIds: ['l1'] }, Ticket.cancelAndReplace),
        {
          errorConfirmation: 'OBPOS_CancelReplaceDeleteLine'
        }
      );
    });

    it('validate services', async () => {
      await expectError(
        () => prepareAction({ lineIds: ['l1'] }, Ticket.services),
        {
          errorConfirmation: 'OBPOS_AllServiceLineMustSelectToDelete'
        }
      );
    });

    it('prevents non deleteable line', async () => {
      await expectError(
        () => prepareAction({ lineIds: ['l1'] }, { ...Ticket.nonDeletable }),
        {
          warningMsg: 'OBPOS_NotDeletableLine'
        }
      );
    });
  });
  // eslint-disable-next-line jest/no-disabled-tests
  describe.skip('approvals', () => {
    it('check approvals', async () => {
      const payloadWithApproval = {
        ticket: [{ ticket: Ticket.simple }],
        approvals: ['OBPOS_approval.deleteLine']
      };
      OB.App.Security.requestApprovalForAction.mockResolvedValue(
        payloadWithApproval
      );
      const newPayload = await prepareAction(
        { lineIds: ['l1'] },
        Ticket.simple
      );
      expect(newPayload).toEqual(payloadWithApproval);
    });
  });
});
