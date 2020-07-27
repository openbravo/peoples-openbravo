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
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/actions/LoadTicket');

describe('Load Ticket actions', () => {
  describe('Load Ticket using a Ticket object', () => {
    let action;
    beforeEach(() => {
      action = OB.App.StateAPI.Global.loadTicket;
    });

    it('does not modify the state when ticket to load is the current ticket', () => {
      const state = {
        Ticket: { id: 'A' },
        TicketList: { tickets: [], addedIds: [] }
      };
      const payload = {
        ticket: { id: 'A' }
      };
      deepfreeze(state);

      const newState = action(state, payload);
      expect(newState).toEqual(state);
    });

    it('loads the ticket passed as payload and enqueues the current ticket', () => {
      const state = {
        Ticket: { id: 'A' },
        TicketList: { tickets: [{ id: 'B' }], addedIds: ['B'] }
      };
      const payload = {
        ticket: { id: 'B' }
      };
      deepfreeze(state);
      const newState = action(state, payload);
      expect(newState).toEqual({
        Ticket: { id: 'B' },
        TicketList: { tickets: [{ id: 'A' }], addedIds: ['B', 'A'] }
      });
    });

    it('throws an error if provided ticket is not valid', () => {
      const state = {
        Ticket: { id: 'A' },
        TicketList: { tickets: [{ id: 'B' }], addedIds: ['B'] }
      };
      const payload = {
        ticket: { id: 'C' }
      };
      deepfreeze(state);

      expect(() => {
        action(state, payload);
      }).toThrow();
    });
  });

  describe('Load Ticket by Id action', () => {
    let action;
    beforeEach(() => {
      action = OB.App.StateAPI.Global.loadTicketById;
    });

    it('does not modify the state when ticket to load is the current ticket', () => {
      const state = {
        Ticket: { id: 'A' },
        TicketList: { tickets: [], addedIds: [] }
      };
      const payload = {
        id: 'A'
      };
      deepfreeze(state);

      const newState = action(state, payload);
      expect(newState).toEqual(state);
    });

    it('loads the ticket passed as payload and enqueues the current ticket', () => {
      const state = {
        Ticket: { id: 'A' },
        TicketList: { tickets: [{ id: 'B' }], addedIds: ['B'] }
      };
      const payload = { id: 'B' };

      deepfreeze(state);
      const newState = action(state, payload);
      expect(newState).toEqual({
        Ticket: { id: 'B' },
        TicketList: { tickets: [{ id: 'A' }], addedIds: ['B', 'A'] }
      });
    });

    it('throws an error if provided ticket is not valid', () => {
      const state = {
        Ticket: { id: 'A' },
        TicketList: { tickets: [{ id: 'B' }], addedIds: ['B'] }
      };
      const payload = { id: 'C' };
      deepfreeze(state);

      expect(() => {
        action(state, payload);
      }).toThrow();
    });
  });
});
