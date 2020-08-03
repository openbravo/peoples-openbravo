/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */

OB = { App: { Class: {}, TerminalProperty: { get: jest.fn() } } };
const deepfreeze = require('deepfreeze');
global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const {
  executeActionPreparations
} = require('../../../../../org.openbravo.mobile.core/web-test/base/state-utils');

require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/TicketList');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/actions/BringTicket');

describe('Bring Ticket', () => {
  describe('Bring Ticket action preparation', () => {
    OB.App.TerminalProperty.get.mockImplementation(property =>
      property === 'usermodel' ? { id: 'user1' } : undefined
    );
    const prepareAction = async (ticketList, payload) => {
      const newPayload = await executeActionPreparations(
        OB.App.StateAPI.TicketList.bringTicket,
        deepfreeze(ticketList),
        deepfreeze(payload)
      );
      return newPayload;
    };

    it('prepare payload as expected', async () => {
      const ticketList = [
        { id: 'A', session: '1', createdBy: 'user0', updatedBy: 'user0' }
      ];
      const newPayload = await prepareAction(ticketList, {
        ticketIds: ['A'],
        session: '2'
      });
      expect(newPayload).toEqual({
        ticketIds: ['A'],
        session: '2',
        userId: 'user1'
      });
    });

    it('prepare payload with single ticket id', async () => {
      const ticketList = [
        { id: 'A', session: '1', createdBy: 'user0', updatedBy: 'user0' }
      ];
      const newPayload = await prepareAction(ticketList, {
        ticketIds: 'A',
        session: '2'
      });
      expect(newPayload).toEqual({
        ticketIds: ['A'],
        session: '2',
        userId: 'user1'
      });
    });

    it('ticketIds mandatory parameter', async () => {
      const ticketList = [
        { id: 'A', session: '1', createdBy: 'user0', updatedBy: 'user0' }
      ];
      let error;
      try {
        await prepareAction(ticketList, {
          session: '2'
        });
      } catch (e) {
        error = e;
      }
      expect(error.message).toEqual('ticketIds parameter is mandatory');
    });

    it('session mandatory parameter', async () => {
      const ticketList = [
        { id: 'A', session: '1', createdBy: 'user0', updatedBy: 'user0' }
      ];
      let error;
      try {
        await prepareAction(ticketList, {
          ticketIds: ['A']
        });
      } catch (e) {
        error = e;
      }
      expect(error.message).toEqual('session parameter is mandatory');
    });
  });

  describe('Bring Ticket action', () => {
    it('brings a single ticket to the current session', () => {
      const ticketList = [
        { id: 'A', session: '1', createdBy: 'user0', updatedBy: 'user0' },
        { id: 'B', session: '1', createdBy: 'user0', updatedBy: 'user0' }
      ];
      const currentSession = '2';
      const payload = {
        ticketIds: ['A'],
        session: currentSession,
        userId: 'user1'
      };
      deepfreeze(ticketList);
      const newState = OB.App.StateAPI.TicketList.bringTicket(
        ticketList,
        payload
      );
      expect(newState).toEqual([
        { id: 'A', session: '2', createdBy: 'user1', updatedBy: 'user1' },
        { id: 'B', session: '1', createdBy: 'user0', updatedBy: 'user0' }
      ]);
    });

    it('brings multiple tickets to the current session', () => {
      const ticketList = [
        { id: 'A', session: '1', createdBy: 'user0', updatedBy: 'user0' },
        { id: 'B', session: '1', createdBy: 'user0', updatedBy: 'user0' },
        { id: 'C', session: '1', createdBy: 'user0', updatedBy: 'user0' }
      ];
      const currentSession = '2';
      const payload = {
        ticketIds: ['A', 'C'],
        session: currentSession,
        userId: 'user1'
      };
      deepfreeze(ticketList);
      const newState = OB.App.StateAPI.TicketList.bringTicket(
        ticketList,
        payload
      );
      expect(newState).toEqual([
        { id: 'A', session: '2', createdBy: 'user1', updatedBy: 'user1' },
        { id: 'B', session: '1', createdBy: 'user0', updatedBy: 'user0' },
        { id: 'C', session: '2', createdBy: 'user1', updatedBy: 'user1' }
      ]);
    });
  });
});
