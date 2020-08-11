/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/*global global*/

OB = { App: { Class: {} } };

global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/TicketList');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/TicketListUtils');

OB.App.State = { Ticket: { Utils: {} } };

describe('Remove Ticket utilities', () => {
  let removeCurrentTicket;

  beforeAll(() => {
    removeCurrentTicket = OB.App.StateAPI.TicketList.utilities.find(
      util => util.functionName === 'removeCurrentTicket'
    ).implementation;
  });

  it('replaces current ticket with the first ticket in the list', () => {
    const state = {
      Ticket: { id: 'A', session: 'ABCD' },
      TicketList: [{ id: 'B', session: 'ABCD' }]
    };
    const payload = {
      session: 'ABCD'
    };
    deepfreeze(state);

    const result = removeCurrentTicket(state.TicketList, state.Ticket, payload);
    expect(result.ticketList).toHaveLength(0);
    expect(result.ticket.id).toEqual('B');
  });

  it('replaces current ticket with a new one when forced via parameters', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [{ id: 'B' }]
    };
    const payload = {
      preferences: { alwaysCreateNewReceiptAfterPayReceipt: true },
      businessPartner: { id: 'BP1' }
    };
    deepfreeze(state);

    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'C' });

    const result = removeCurrentTicket(state.TicketList, state.Ticket, payload);
    expect(result.ticketList).toHaveLength(1);
    expect(result.ticket.id).toEqual('C');

    delete OB.App.State.Ticket.Utils.newTicket;
  });

  it('creates new ticket when list is empty', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: []
    };
    const payload = { businessPartner: { id: 'BP1' } };
    deepfreeze(state);

    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'C' });

    const result = removeCurrentTicket(state.TicketList, state.Ticket, payload);
    expect(result.ticket.id).toEqual('C');

    delete OB.App.State.Ticket.Utils.newTicket;
  });
});
