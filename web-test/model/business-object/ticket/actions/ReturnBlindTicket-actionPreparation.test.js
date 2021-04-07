/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

const {
  executeActionPreparations
} = require('../../../../../../org.openbravo.mobile.core/web-test/base/state-utils');
require('../SetupTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/ReturnBlindTicket');
require('../SetupTicketUtils');
const deepfreeze = require('deepfreeze');

const expectActionPreparationError = async (ticket, payload, expectedError) => {
  let error;
  try {
    await executeActionPreparations(
      OB.App.StateAPI.Ticket.returnBlindTicket,
      ticket,
      payload
    );
  } catch (e) {
    error = e;
  }
  if (expectedError) {
    expect(error).toMatchObject(expectedError);
  } else {
    expect(error).toBeUndefined();
  }
};

describe('returnBlindTicket action preparations', () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  test.each`
    isEditable | error
    ${false}   | ${true}
    ${true}    | ${false}
  `(
    'should throw error if ticket is not editable',
    async ({ isEditable, error }) => {
      const ticket = deepfreeze({ isEditable, lines: [] });
      const payload = deepfreeze({});
      await expectActionPreparationError(
        ticket,
        payload,
        error
          ? {
              info: {
                title: 'OBPOS_modalNoEditableHeader',
                errorConfirmation: 'OBPOS_modalNoEditableBody'
              }
            }
          : null
      );
    }
  );

  test.each`
    lines                                                                                         | error
    ${[]}                                                                                         | ${false}
    ${[{ id: '1', product: { returnable: true } }]}                                               | ${false}
    ${[{ id: '1', product: { returnable: false } }]}                                              | ${true}
    ${[{ id: '1', product: { returnable: true } }, { id: '2', product: { returnable: true } }]}   | ${false}
    ${[{ id: '1', product: { returnable: true } }, { id: '2', product: { returnable: false } }]}  | ${true}
    ${[{ id: '1', product: { returnable: false } }, { id: '2', product: { returnable: true } }]}  | ${true}
    ${[{ id: '1', product: { returnable: false } }, { id: '2', product: { returnable: false } }]} | ${true}
  `(
    'should throw error if no returnable product',
    async ({ lines, lineIds, error }) => {
      const ticket = deepfreeze({ lines });
      const payload = deepfreeze({});
      await expectActionPreparationError(
        ticket,
        payload,
        error
          ? {
              info: {
                title: 'OBPOS_UnreturnableProduct',
                errorConfirmation: 'OBPOS_UnreturnableProductMessage'
              }
            }
          : null
      );
    }
  );
});
