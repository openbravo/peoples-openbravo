/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../global/SetupGlobal');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/actions/AddNewQuotation');
require('../../global/SetupGlobalUtils');
const deepfreeze = require('deepfreeze');

describe('Add new quotation action', () => {
  it('should create a new quotation ticket in state.Ticket and store the existing one in state.TicketList', () => {
    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'B' });
    const payload = deepfreeze({
      terminal: { terminalType: { documentTypeForQuotations: 'Q' } }
    });
    const state = deepfreeze({
      Ticket: { id: 'A' },
      TicketList: []
    });

    const newState = OB.App.StateAPI.Global.addNewQuotation(state, payload);

    expect(newState).toEqual({
      Ticket: {
        id: 'B',
        documentType: 'Q',
        isQuotation: true,
        orderType: 0
      },
      TicketList: [{ id: 'A' }]
    });
  });
});
