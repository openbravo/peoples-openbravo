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
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionCanceled');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/actions/RemoveTicket');

describe('Remove Ticket actions', () => {
  let action;

  beforeEach(() => {
    action = OB.App.StateAPI.Global.removeTicket;
  });

  it('removes a ticket from the ticket list using the ID', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [{ id: 'B' }]
    };
    const payload = {
      id: 'B'
    };
    deepfreeze(state);

    const newState = action(state, payload);
    expect(newState.TicketList).toHaveLength(0);
  });

  it('if removing active ticket, replace with the first ticket in the list', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [{ id: 'B' }]
    };
    const payload = {
      id: 'A'
    };
    deepfreeze(state);

    const newState = action(state, payload);
    expect(newState.TicketList).toHaveLength(0);
    expect(newState.Ticket.id).toEqual('B');
  });

  it('replace with the first ticket in the list if no id provided', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [{ id: 'B' }]
    };
    const payload = {};
    deepfreeze(state);

    const newState = action(state, payload);
    expect(newState.TicketList).toHaveLength(0);
    expect(newState.Ticket.id).toEqual('B');
  });

  it('replaces current ticket with a new one', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [{ id: 'B' }]
    };
    const payload = {
      forceNewTicket: true,
      businessPartner: { id: 'BP1' }
    };
    deepfreeze(state);

    OB.App.State = { Ticket: { Utils: {} } };
    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'C' });

    const newState = action(state, payload);
    expect(newState.TicketList).toHaveLength(1);
    expect(newState.Ticket.id).toEqual('C');

    delete OB.App.State;
  });

  it('creates new ticket when list is empty', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: []
    };
    const payload = { businessPartner: { id: 'BP1' } };
    deepfreeze(state);

    OB.App.State = { Ticket: { Utils: {} } };
    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'C' });

    const newState = action(state, payload);
    expect(newState.Ticket.id).toEqual('C');

    delete OB.App.State;
  });
});
