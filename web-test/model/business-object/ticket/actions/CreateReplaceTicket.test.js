/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../SetupTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/CreateReplaceTicket');
require('../SetupTicketUtils');
const deepfreeze = require('deepfreeze');

describe('createReplaceTicket', () => {
  it('replaces ticket', () => {
    OB.App.UUID.generate = jest.fn().mockReturnValue('0');
    OB.App.State.DocumentSequence = {
      Utils: {
        calculateReplaceDocumentNumber: jest
          .fn()
          .mockImplementation(documentNo => `${documentNo}-1`)
      }
    };
    const ticket = deepfreeze({
      id: '1',
      documentNo: 'O1',
      lines: [{ id: '1', qty: 1 }]
    });
    const payload = deepfreeze({ terminal: { terminalType: {} }, context: {} });
    const newTicket = OB.App.StateAPI.Ticket.createReplaceTicket(
      ticket,
      payload
    );
    expect(newTicket).toMatchObject({
      canceledorder: {
        id: '1',
        documentNo: 'O1',
        lines: [{ id: '1', qty: 1 }]
      },
      creditAmount: 0,
      replacedorder: '1',
      documentNo: 'O1-1',
      replacedorder_documentNo: 'O1',
      negativeDocNo: 'O1*R*',
      documentType: undefined,
      id: '0',
      hasbeenpaid: 'N',
      isPaid: false,
      isEditable: true,
      doCancelAndReplace: true,
      isLayaway: false,
      orderType: undefined,
      lines: [
        {
          id: '0',
          replacedorderline: '1',
          qty: 1,
          invoicedQuantity: undefined,
          obposCanbedelivered: false,
          obposIspaid: false
        }
      ],
      paidOnCredit: false,
      paidPartiallyOnCredit: false,
      posTerminal: undefined
    });
  });

  test.each`
    ticket                                                                           | layawayorder | result
    ${{ orderType: 0, lines: [] }}                                                   | ${undefined} | ${{ orderType: 0 }}
    ${{ orderType: 0, isLayaway: false, lines: [] }}                                 | ${undefined} | ${{ orderType: 0 }}
    ${{ orderType: 0, isLayaway: true, lines: [] }}                                  | ${undefined} | ${{ orderType: 2 }}
    ${{ orderType: 0, lines: [{ deliveredQuantity: 0 }] }}                           | ${false}     | ${{ orderType: 0 }}
    ${{ orderType: 0, lines: [{ deliveredQuantity: 1 }] }}                           | ${false}     | ${{ orderType: 0 }}
    ${{ orderType: 0, lines: [{ deliveredQuantity: 0 }] }}                           | ${true}      | ${{ orderType: 2 }}
    ${{ orderType: 0, lines: [{ deliveredQuantity: 1 }] }}                           | ${true}      | ${{ orderType: 0 }}
    ${{ orderType: 0, lines: [{ deliveredQuantity: 0 }, { deliveredQuantity: 0 }] }} | ${true}      | ${{ orderType: 2 }}
    ${{ orderType: 0, lines: [{ deliveredQuantity: 0 }, { deliveredQuantity: 1 }] }} | ${true}      | ${{ orderType: 0 }}
  `(
    'should use layaway orderType if original ticket is a layaway',
    ({ ticket, layawayorder, result }) => {
      const payload = deepfreeze({
        terminal: { terminalType: { layawayorder } },
        context: {}
      });
      const newTicket = OB.App.StateAPI.Ticket.createReplaceTicket(
        deepfreeze(ticket),
        payload
      );
      expect(newTicket).toMatchObject(result);
    }
  );

  test.each`
    isCrossStore | result
    ${true}      | ${{ organization: 'O', documentType: 'O1', posTerminal: 'T' }}
    ${false}     | ${{ organization: 'O', documentType: 'O2', posTerminal: 'T' }}
  `(
    'should use original ticket documentType if isCrossStore is $isCrossStore',
    ({ isCrossStore, result }) => {
      OB.App.State.Ticket.Utils.isCrossStore = jest
        .fn()
        .mockReturnValue(isCrossStore);
      const ticket = deepfreeze({
        id: '1',
        organization: 'O',
        documentType: 'O1',
        documentNo: 'O1',
        grossAmount: 100,
        payment: 100,
        lines: []
      });
      const payload = deepfreeze({
        terminal: { id: 'T', terminalType: { documentType: 'O2' } },
        context: {}
      });
      const newTicket = OB.App.StateAPI.Ticket.createReplaceTicket(
        ticket,
        payload
      );
      expect(newTicket).toMatchObject(result);
    }
  );

  test.each`
    lines                                                                                                   | deferredLines    | result
    ${[{ obposCanbedelivered: true }]}                                                                      | ${undefined}     | ${{ lines: [{ obposCanbedelivered: true }] }}
    ${[{ obposCanbedelivered: false }]}                                                                     | ${undefined}     | ${{ lines: [{ obposCanbedelivered: true }] }}
    ${[{ obposCanbedelivered: true, qty: 1, deliveredQuantity: 1 }]}                                        | ${undefined}     | ${{ lines: [{ obposCanbedelivered: true }] }}
    ${[{ obposCanbedelivered: true, qty: 1, deliveredQuantity: 0 }]}                                        | ${undefined}     | ${{ lines: [{ obposCanbedelivered: true }] }}
    ${[{ obposCanbedelivered: false, qty: 1, deliveredQuantity: 1 }]}                                       | ${undefined}     | ${{ lines: [{ obposCanbedelivered: true }] }}
    ${[{ obposCanbedelivered: false, qty: 1, deliveredQuantity: 0 }]}                                       | ${undefined}     | ${{ lines: [{ obposCanbedelivered: false }] }}
    ${[{ isEditable: true, isDeletable: true }]}                                                            | ${undefined}     | ${{ lines: [{ isEditable: true, isDeletable: true }] }}
    ${[{ isEditable: false, isDeletable: false }]}                                                          | ${undefined}     | ${{ lines: [{ isEditable: false, isDeletable: false }] }}
    ${[{ id: '1', isEditable: true, isDeletable: true }, { id: '2', isEditable: true, isDeletable: true }]} | ${[{ id: '1' }]} | ${{ lines: [{ isEditable: false, isDeletable: false }, { isEditable: true, isDeletable: true }] }}
    ${[{ id: '1' }, { id: '2', relatedLines: [{ orderId: '1', orderlineId: '1' }] }]}                       | ${undefined}     | ${{ lines: [{ id: '0' }, { id: '0', relatedLines: [{ orderId: '0', orderlineId: '0' }] }] }}
    ${[{ id: '1', relatedLines: [{ orderId: '1', orderlineId: '2' }] }, { id: '2' }]}                       | ${undefined}     | ${{ lines: [{ id: '0', relatedLines: [{ orderId: '0', orderlineId: '0' }] }, { id: '0' }] }}
    ${[{ id: '1' }, { id: '2', relatedLines: [{ orderId: '1', orderlineId: '3' }] }]}                       | ${undefined}     | ${{ lines: [{ id: '0' }, { id: '0', relatedLines: [{ orderId: '0', orderlineId: '3' }] }] }}
    ${[{ id: '1', relatedLines: [{ orderId: '1', orderlineId: '3' }] }, { id: '2' }]}                       | ${undefined}     | ${{ lines: [{ id: '0', relatedLines: [{ orderId: '0', orderlineId: '3' }] }, { id: '0' }] }}
  `('should replace ticket lines', ({ lines, deferredLines, result }) => {
    const ticket = deepfreeze({
      id: '1',
      documentNo: 'O1',
      hasServices: true,
      lines: lines
    });
    const payload = deepfreeze({
      terminal: { terminalType: {} },
      context: {},
      deferredLines
    });
    const newTicket = OB.App.StateAPI.Ticket.createReplaceTicket(
      ticket,
      payload
    );
    expect(newTicket).toMatchObject(result);
  });
});
