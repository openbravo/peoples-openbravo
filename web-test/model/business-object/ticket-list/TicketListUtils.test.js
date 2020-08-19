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
  let removeTicket;

  beforeAll(() => {
    removeTicket = OB.App.StateAPI.TicketList.utilities.find(
      util => util.functionName === 'removeTicket'
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

    const result = removeTicket(state.TicketList, state.Ticket, payload);
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

    const result = removeTicket(state.TicketList, state.Ticket, payload);
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

    const result = removeTicket(state.TicketList, state.Ticket, payload);
    expect(result.ticket.id).toEqual('C');

    delete OB.App.State.Ticket.Utils.newTicket;
  });

  it('creates new ticket when list is empty and ticket is present in the list to delete', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: []
    };
    const payload = {
      businessPartner: { id: 'BP1' },
      multiTicketList: [{ id: 'A' }]
    };
    deepfreeze(state);

    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'B' });

    const result = removeTicket(state.TicketList, state.Ticket, payload);
    expect(result.ticket.id).toEqual('B');
    expect(result.ticketList).toEqual([]);

    delete OB.App.State.Ticket.Utils.newTicket;
  });

  it('keeps current ticket when list is empty and ticket is not present in the list to delete', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [{ id: 'B' }]
    };
    const payload = {
      businessPartner: { id: 'BP1' },
      multiTicketList: [{ id: 'B' }]
    };
    deepfreeze(state);

    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'C' });

    const result = removeTicket(state.TicketList, state.Ticket, payload);
    expect(result.ticket.id).toEqual('A');
    expect(result.ticketList).toEqual([]);

    delete OB.App.State.Ticket.Utils.newTicket;
  });

  it('replaces current ticket when list is not empty and ticket is present in the list to delete', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [{ id: 'B' }, { id: 'C' }]
    };
    const payload = {
      businessPartner: { id: 'BP1' },
      multiTicketList: [{ id: 'A' }]
    };
    deepfreeze(state);

    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'D' });

    const result = removeTicket(state.TicketList, state.Ticket, payload);
    expect(result.ticket.id).toEqual('B');
    expect(result.ticketList).toEqual([{ id: 'C' }]);

    delete OB.App.State.Ticket.Utils.newTicket;
  });

  it('keeps current ticket when list is not empty and ticket is not present in the list to delete', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [{ id: 'B' }, { id: 'C' }]
    };
    const payload = {
      businessPartner: { id: 'BP1' },
      multiTicketList: [{ id: 'B' }]
    };
    deepfreeze(state);

    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'D' });

    const result = removeTicket(state.TicketList, state.Ticket, payload);
    expect(result.ticket.id).toEqual('A');
    expect(result.ticketList).toEqual([{ id: 'C' }]);

    delete OB.App.State.Ticket.Utils.newTicket;
  });

  it('replaces current ticket when list is not empty and ticket and another ticket from list is present in the list to delete', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [{ id: 'B' }, { id: 'C' }]
    };
    const payload = {
      businessPartner: { id: 'BP1' },
      multiTicketList: [{ id: 'A' }, { id: 'B' }]
    };
    deepfreeze(state);

    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'D' });

    const result = removeTicket(state.TicketList, state.Ticket, payload);
    expect(result.ticket.id).toEqual('C');
    expect(result.ticketList).toEqual([]);

    delete OB.App.State.Ticket.Utils.newTicket;
  });

  it('keeps current ticket when list is not empty and ticket is not present in the list to delete but another ticket from the list is', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [{ id: 'B' }, { id: 'C' }]
    };
    const payload = {
      businessPartner: { id: 'BP1' },
      multiTicketList: [{ id: 'B' }, { id: 'C' }]
    };
    deepfreeze(state);

    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'D' });

    const result = removeTicket(state.TicketList, state.Ticket, payload);
    expect(result.ticket.id).toEqual('A');
    expect(result.ticketList).toEqual([]);

    delete OB.App.State.Ticket.Utils.newTicket;
  });

  it('creates new ticket when list is not empty and ticket and every ticket from the list are present in the list to delete', () => {
    const state = {
      Ticket: { id: 'A' },
      TicketList: [{ id: 'B' }, { id: 'C' }]
    };
    const payload = {
      businessPartner: { id: 'BP1' },
      multiTicketList: [{ id: 'A' }, { id: 'B' }, { id: 'C' }]
    };
    deepfreeze(state);

    OB.App.State.Ticket.Utils.newTicket = jest
      .fn()
      .mockReturnValue({ id: 'D' });

    const result = removeTicket(state.TicketList, state.Ticket, payload);
    expect(result.ticket.id).toEqual('D');
    expect(result.ticketList).toEqual([]);

    delete OB.App.State.Ticket.Utils.newTicket;
  });
});
