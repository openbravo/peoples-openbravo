/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../global/SetupGlobal');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/actions/LoadRemoteTicket');
require('../../global/SetupGlobalUtils');
const deepfreeze = require('deepfreeze');

describe('Load Remote Ticket action', () => {
  it('should not load ticket if it is the current ticket', () => {
    const state = deepfreeze({
      Ticket: {
        id: 'A'
      }
    });
    const payload = deepfreeze({
      ticketInSession: {
        id: 'A'
      }
    });

    const newState = OB.App.StateAPI.Global.loadRemoteTicket(state, payload);

    expect(newState).toMatchObject({
      Ticket: {
        id: 'A'
      }
    });
  });

  it('should switch ticket if it is in ticket list', () => {
    const state = deepfreeze({
      TicketList: [
        {
          id: 'A'
        }
      ],
      Ticket: {
        id: 'B'
      }
    });
    const payload = deepfreeze({
      ticketInSession: {
        id: 'A'
      }
    });

    const newState = OB.App.StateAPI.Global.loadRemoteTicket(state, payload);

    expect(newState).toMatchObject({
      TicketList: [
        {
          id: 'B'
        }
      ],
      Ticket: {
        id: 'A'
      }
    });
  });

  it('should load ticket if it is not in state', () => {
    const state = deepfreeze({
      TicketList: [
        {
          id: 'A'
        }
      ],
      Ticket: {
        id: 'B'
      }
    });
    const payload = deepfreeze({
      ticket: {
        orderid: 'C',
        orderDate: new Date(),
        creationDate: new Date(),
        totalamount: OB.DEC.Zero,
        receiptTaxes: [],
        receiptPayments: [],
        receiptLines: [],
        businessPartner: { locations: [{}] }
      },
      terminal: { terminalType: {} }
    });

    const newState = OB.App.StateAPI.Global.loadRemoteTicket(state, payload);

    expect(newState).toMatchObject({
      TicketList: [
        {
          id: 'B'
        },
        {
          id: 'A'
        }
      ],
      Ticket: {
        id: 'C'
      }
    });
  });
});
