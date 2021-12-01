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
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/ReturnLine');
require('../SetupTicketUtils');
const deepfreeze = require('deepfreeze');

const expectActionPreparationError = async (ticket, payload, expectedError) => {
  let error;
  try {
    await executeActionPreparations(
      OB.App.StateAPI.Ticket.returnLine,
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

describe('returnLine action preparation', () => {
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
      const payload = deepfreeze({ lineIds: [] });
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
    lines                                                    | lineIds  | notAllowSalesWithReturn | error
    ${[]}                                                    | ${[]}    | ${false}                | ${false}
    ${[]}                                                    | ${[]}    | ${true}                 | ${false}
    ${[{ id: '1', qty: 1, product: { returnable: true } }]}  | ${[]}    | ${false}                | ${false}
    ${[{ id: '1', qty: 1, product: { returnable: true } }]}  | ${[]}    | ${true}                 | ${true}
    ${[{ id: '1', qty: -1, product: { returnable: true } }]} | ${[]}    | ${false}                | ${false}
    ${[{ id: '1', qty: -1, product: { returnable: true } }]} | ${[]}    | ${true}                 | ${false}
    ${[{ id: '1', qty: 1, product: { returnable: true } }]}  | ${['1']} | ${false}                | ${false}
    ${[{ id: '1', qty: 1, product: { returnable: true } }]}  | ${['1']} | ${true}                 | ${false}
    ${[{ id: '1', qty: -1, product: { returnable: true } }]} | ${['1']} | ${false}                | ${false}
    ${[{ id: '1', qty: -1, product: { returnable: true } }]} | ${['1']} | ${true}                 | ${false}
  `(
    'should throw error if sale with return not allowed',
    async ({ lines, lineIds, notAllowSalesWithReturn, error }) => {
      OB.App.Security.hasPermission.mockReturnValue(notAllowSalesWithReturn);
      const ticket = deepfreeze({ isEditable: true, lines });
      const payload = deepfreeze({ lineIds });
      await expectActionPreparationError(
        ticket,
        payload,
        error ? { info: { errorMsg: 'OBPOS_MsgCannotAddNegative' } } : null
      );
    }
  );

  test.each`
    isLayaway | orderType | allowLayawaysNegativeLines | error
    ${false}  | ${0}      | ${false}                   | ${false}
    ${false}  | ${1}      | ${false}                   | ${false}
    ${false}  | ${2}      | ${false}                   | ${true}
    ${false}  | ${3}      | ${false}                   | ${true}
    ${false}  | ${4}      | ${false}                   | ${false}
    ${true}   | ${0}      | ${false}                   | ${true}
    ${true}   | ${1}      | ${false}                   | ${true}
    ${true}   | ${2}      | ${false}                   | ${true}
    ${true}   | ${3}      | ${false}                   | ${true}
    ${true}   | ${4}      | ${false}                   | ${true}
    ${false}  | ${0}      | ${true}                    | ${false}
    ${false}  | ${1}      | ${true}                    | ${false}
    ${false}  | ${2}      | ${true}                    | ${false}
    ${false}  | ${3}      | ${true}                    | ${false}
    ${false}  | ${4}      | ${true}                    | ${false}
    ${true}   | ${0}      | ${true}                    | ${false}
    ${true}   | ${1}      | ${true}                    | ${false}
    ${true}   | ${2}      | ${true}                    | ${false}
    ${true}   | ${3}      | ${true}                    | ${false}
    ${true}   | ${4}      | ${true}                    | ${false}
  `(
    'should throw error if return layaway not allowed',
    async ({ isLayaway, orderType, allowLayawaysNegativeLines, error }) => {
      OB.App.Security.hasPermission.mockReturnValue(allowLayawaysNegativeLines);
      const ticket = deepfreeze({
        isEditable: true,
        isLayaway,
        orderType,
        lines: []
      });
      const payload = deepfreeze({ lineIds: [] });
      await expectActionPreparationError(
        ticket,
        payload,
        error
          ? { info: { errorMsg: 'OBPOS_layawaysOrdersWithReturnsNotAllowed' } }
          : null
      );
    }
  );

  test.each`
    replacedorder | lines                                                                 | lineIds  | error
    ${false}      | ${[]}                                                                 | ${[]}    | ${false}
    ${true}       | ${[]}                                                                 | ${[]}    | ${false}
    ${false}      | ${[{ id: '1', remainingQuantity: 0, product: { returnable: true } }]} | ${[]}    | ${false}
    ${true}       | ${[{ id: '1', remainingQuantity: 0, product: { returnable: true } }]} | ${[]}    | ${false}
    ${false}      | ${[{ id: '1', remainingQuantity: 1, product: { returnable: true } }]} | ${[]}    | ${false}
    ${true}       | ${[{ id: '1', remainingQuantity: 1, product: { returnable: true } }]} | ${[]}    | ${false}
    ${false}      | ${[{ id: '1', remainingQuantity: 0, product: { returnable: true } }]} | ${['1']} | ${false}
    ${true}       | ${[{ id: '1', remainingQuantity: 0, product: { returnable: true } }]} | ${['1']} | ${false}
    ${false}      | ${[{ id: '1', remainingQuantity: 1, product: { returnable: true } }]} | ${['1']} | ${false}
    ${true}       | ${[{ id: '1', remainingQuantity: 1, product: { returnable: true } }]} | ${['1']} | ${true}
  `(
    'should throw error if return cancel and replace line',
    async ({ replacedorder, lines, lineIds, error }) => {
      const ticket = deepfreeze({ isEditable: true, replacedorder, lines });
      const payload = deepfreeze({ lineIds });
      await expectActionPreparationError(
        ticket,
        payload,
        error ? { info: { errorMsg: 'OBPOS_CancelReplaceReturnLines' } } : null
      );
    }
  );

  test.each`
    lines                                                     | lineIds  | error
    ${[]}                                                     | ${[]}    | ${false}
    ${[{ id: '1', qty: 1, product: { returnable: true } }]}   | ${[]}    | ${false}
    ${[{ id: '1', qty: -1, product: { returnable: true } }]}  | ${[]}    | ${false}
    ${[{ id: '1', qty: 1, product: { returnable: false } }]}  | ${[]}    | ${false}
    ${[{ id: '1', qty: -1, product: { returnable: false } }]} | ${[]}    | ${false}
    ${[{ id: '1', qty: 1, product: { returnable: true } }]}   | ${['1']} | ${false}
    ${[{ id: '1', qty: -1, product: { returnable: true } }]}  | ${['1']} | ${false}
    ${[{ id: '1', qty: 1, product: { returnable: false } }]}  | ${['1']} | ${true}
    ${[{ id: '1', qty: -1, product: { returnable: false } }]} | ${['1']} | ${false}
  `(
    'should throw error if no returnable product',
    async ({ lines, lineIds, error }) => {
      const ticket = deepfreeze({ isEditable: true, lines });
      const payload = deepfreeze({ lineIds });
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

  test.each`
    lines                                                                                                                                                               | lineIds       | error
    ${[{ id: '1', qty: 1, product: { productType: 'S', returnable: true } }]}                                                                                           | ${[]}         | ${false}
    ${[{ id: '1', qty: 1, product: { productType: 'S', returnable: true } }]}                                                                                           | ${['1']}      | ${false}
    ${[{ id: '1', qty: 1, product: { productType: 'S', returnable: true }, relatedLines: [{ orderlineId: '2' }] }]}                                                     | ${[]}         | ${false}
    ${[{ id: '1', qty: 1, product: { productType: 'S', returnable: true }, relatedLines: [{ orderlineId: '2' }] }]}                                                     | ${['1']}      | ${true}
    ${[{ id: '1', qty: 1, product: { productType: 'S', returnable: true }, relatedLines: [{ orderlineId: '2' }] }, { id: '2', qty: 1, product: { returnable: true } }]} | ${['1', '2']} | ${false}
  `(
    'should throw error if no returnable service',
    async ({ lines, lineIds, error }) => {
      const ticket = deepfreeze({ isEditable: true, lines });
      const payload = deepfreeze({ lineIds });
      await expectActionPreparationError(
        ticket,
        payload,
        error
          ? {
              info: {
                title: 'OBPOS_UnreturnableRelatedService',
                errorConfirmation: 'OBPOS_NotProductSelectedToReturn'
              }
            }
          : null
      );
    }
  );

  test.each`
    lines                                                                                                                                                                                   | lineIds       | error
    ${[{ id: '1', qty: 1, product: { productType: 'I', returnable: true } }]}                                                                                                               | ${[]}         | ${false}
    ${[{ id: '1', qty: 1, product: { productType: 'I', returnable: true } }]}                                                                                                               | ${['1']}      | ${false}
    ${[{ id: '1', qty: 1, product: { productType: 'I', returnable: true } }, { id: '2', qty: 1, product: { productType: 'S', returnable: false }, relatedLines: [{ orderlineId: '1' }] }]}  | ${['1']}      | ${true}
    ${[{ id: '1', qty: 1, product: { productType: 'I', returnable: true } }, { id: '2', qty: 1, product: { productType: 'S', returnable: true }, relatedLines: [{ orderlineId: '1' }] }]}   | ${['1']}      | ${false}
    ${[{ id: '1', qty: 1, product: { productType: 'I', returnable: true } }, { id: '2', qty: 1, product: { productType: 'S', returnable: false }, relatedLines: [{ orderlineId: '3' }] }]}  | ${['1']}      | ${false}
    ${[{ id: '1', qty: 1, product: { productType: 'I', returnable: true } }, { id: '2', qty: -1, product: { productType: 'S', returnable: false }, relatedLines: [{ orderlineId: '1' }] }]} | ${['1', '2']} | ${false}
  `(
    'should throw error if no returnable related service',
    async ({ lines, lineIds, error }) => {
      const ticket = deepfreeze({ isEditable: true, lines });
      const payload = deepfreeze({ lineIds });
      await expectActionPreparationError(
        ticket,
        payload,
        error
          ? {
              info: {
                title: 'OBPOS_UnreturnableRelatedService',
                errorConfirmation: 'OBPOS_UnreturnableRelatedServiceMessage'
              }
            }
          : null
      );
    }
  );

  test.each`
    lines                                                             | lineIds  | hasStock | error
    ${[]}                                                             | ${[]}    | ${false} | ${false}
    ${[]}                                                             | ${[]}    | ${true}  | ${false}
    ${[{ id: '1', qty: 1, product: { id: 'A', returnable: true } }]}  | ${[]}    | ${false} | ${false}
    ${[{ id: '1', qty: 1, product: { id: 'A', returnable: true } }]}  | ${[]}    | ${true}  | ${false}
    ${[{ id: '1', qty: -1, product: { id: 'A', returnable: true } }]} | ${[]}    | ${false} | ${false}
    ${[{ id: '1', qty: -1, product: { id: 'A', returnable: true } }]} | ${[]}    | ${true}  | ${false}
    ${[{ id: '1', qty: 1, product: { id: 'A', returnable: true } }]}  | ${['1']} | ${false} | ${false}
    ${[{ id: '1', qty: 1, product: { id: 'A', returnable: true } }]}  | ${['1']} | ${true}  | ${false}
    ${[{ id: '1', qty: -1, product: { id: 'A', returnable: true } }]} | ${['1']} | ${false} | ${true}
    ${[{ id: '1', qty: -1, product: { id: 'A', returnable: true } }]} | ${['1']} | ${true}  | ${false}
  `(
    'should throw error if no stock',
    async ({ lines, lineIds, hasStock, error }) => {
      OB.App.StockChecker.hasStock = jest.fn().mockResolvedValue(hasStock);
      const ticket = deepfreeze({ isEditable: true, lines });
      const payload = deepfreeze({ lineIds });
      await expectActionPreparationError(
        ticket,
        payload,
        error
          ? new OB.App.Class.ActionSilentlyCanceled(
              'Return line canceled: there is no stock of product A'
            )
          : null
      );
    }
  );
});
