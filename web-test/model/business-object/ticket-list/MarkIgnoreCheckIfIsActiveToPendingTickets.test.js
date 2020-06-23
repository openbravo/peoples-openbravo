/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB = { App: { Class: {} } };
const deepfreeze = require('deepfreeze');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/actions/MarkIgnoreCheckIfIsActiveToPendingTickets');

describe('Mark ignore check if is active to pending tickets', () => {
  let action;
  beforeEach(() => {
    action = OB.App.StateAPI.Global.markIgnoreCheckIfIsActiveToPendingTickets;
  });

  it('changes ignore check flag to pending current ticket', () => {
    const state = {
      Ticket: { id: 'A', hasbeenpaid: 'N', ignoreCheckIfIsActiveOrder: false },
      TicketList: []
    };

    deepfreeze(state);

    const newState = action(state);
    expect(newState.Ticket.ignoreCheckIfIsActiveOrder).toBeTruthy();
  });

  it('changes ignore check flag to enqueued tickets', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [
        { id: 'B', hasbeenpaid: 'N', ignoreCheckIfIsActiveOrder: false },
        {
          id: 'C',
          hasbeenpaid: 'Y',
          ignoreCheckIfIsActiveOrder: false
        }
      ]
    };

    deepfreeze(state);

    const newState = action(state);
    expect(
      newState.TicketList.map(ticket => ticket.ignoreCheckIfIsActiveOrder)
    ).toEqual([true, false]);
  });
});
