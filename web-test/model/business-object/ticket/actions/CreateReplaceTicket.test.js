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
    const globalState = deepfreeze({ Cashup: { id: 'C' } });
    const ticket = deepfreeze({
      id: '1',
      documentNo: 'O1',
      grossAmount: 100,
      payment: 100,
      lines: [{ id: '1', qty: 1, promotions: [], product: {} }]
    });
    const payload = deepfreeze({ terminal: { terminalType: {} } });
    const newTicket = OB.App.StateAPI.Ticket.createReplaceTicket(
      ticket,
      payload,
      { globalState }
    );
    expect(newTicket).toMatchObject({
      cancelLayaway: true,
      canceledorder: {
        id: '1',
        documentNo: 'O1',
        grossAmount: 100,
        lines: [{ id: '1', qty: 1, promotions: [], product: {} }],
        payment: 100
      },
      creditAmount: 0,
      documentNo: 'O1*R*',
      documentType: undefined,
      fromLayaway: undefined,
      grossAmount: 100,
      id: undefined,
      isEditable: false,
      isLayaway: false,
      isPaid: false,
      lines: [
        {
          id: undefined,
          canceledLine: '1',
          qty: -1,
          deliveredQuantity: undefined,
          invoicedQuantity: undefined,
          obposCanbedelivered: true,
          obposIspaid: false,
          promotions: [],
          product: {}
        }
      ],
      nettingPayment: 0,
      obposAppCashup: 'C',
      orderType: 3,
      paidOnCredit: false,
      paidPartiallyOnCredit: false,
      payment: 100,
      payments: [],
      posTerminal: undefined
    });
  });

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
      const globalState = deepfreeze({ Cashup: {} });
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
        terminal: { id: 'T', terminalType: { documentType: 'O2' } }
      });
      const newTicket = OB.App.StateAPI.Ticket.createReplaceTicket(
        ticket,
        payload,
        { globalState }
      );
      expect(newTicket).toMatchObject(result);
    }
  );

  test.each`
    lines                                                                                                    | result
    ${[{ id: '1', qty: -1, promotions: [], product: {} }]}                                                   | ${{ lines: [] }}
    ${[{ id: '1', qty: 1, promotions: [], product: {} }]}                                                    | ${{ lines: [{ qty: -1 }] }}
    ${[{ id: '1', qty: 5, promotions: [], product: {} }, { id: '2', qty: -2, promotions: [], product: {} }]} | ${{ lines: [{ qty: -5 }] }}
    ${[{ id: '1', qty: 5, deliveredQuantity: 0, promotions: [], product: {} }]}                              | ${{ lines: [{ qty: -5 }] }}
    ${[{ id: '1', qty: 5, deliveredQuantity: 4, promotions: [], product: {} }]}                              | ${{ lines: [{ qty: -1 }] }}
    ${[{ id: '1', qty: 5, deliveredQuantity: 5, promotions: [], product: {} }]}                              | ${{ lines: [] }}
  `('should replace ticket lines', ({ lines, result }) => {
    const globalState = deepfreeze({ Cashup: {} });
    const ticket = deepfreeze({
      id: '1',
      documentNo: 'O1',
      grossAmount: 100,
      payment: 100,
      lines: lines
    });
    const payload = deepfreeze({
      terminal: { id: 'T', terminalType: { documentType: 'O2' } }
    });
    const newTicket = OB.App.StateAPI.Ticket.createReplaceTicket(
      ticket,
      payload,
      { globalState }
    );
    expect(newTicket).toMatchObject(result);
  });
});
