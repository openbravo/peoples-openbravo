/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */

global.OB = { debug: jest.fn() };
OB.App = {
  Class: {},
  MessageModelController: {
    add: jest.fn(),
    put: jest.fn(),
    findAll: jest.fn(),
    delete: jest.fn()
  }
};
OB.POS = {
  hwserver: {}
};

require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/synchronization-buffer/SynchronizationBuffer');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/synchronization-buffer/SynchronizationEndpoint');
require('../../../web/org.openbravo.retail.posterminal/app/model/synchronization-buffer/HardwareManagerEndpoint');

describe('Harware Manager Synchronization Endpoint', () => {
  let syncBuffer;
  let hwManagerEndpoint;

  // initializes the sync buffer with just the hw manager synchronization endpoint
  const initSingleEndpointSyncBuffer = () => {
    hwManagerEndpoint = new OB.App.Class.HardwareManagerEndpoint();
    syncBuffer = new OB.App.Class.SynchronizationBuffer();
    syncBuffer.registerEndpoint(hwManagerEndpoint);
  };

  beforeEach(() => {
    jest.resetAllMocks();
    // by default execute the tests with a unique synchronization endpoint
    initSingleEndpointSyncBuffer();
  });

  describe('online/offline endpoint status', () => {
    it('Correct initial endpoint status', async () => {
      expect(await hwManagerEndpoint.isOnline()).toBeFalsy();
    });

    it('SynchronizationBuffer can switch the endpoint status', async () => {
      expect(await hwManagerEndpoint.isOnline()).toBeFalsy();
      await syncBuffer.goOnline('HardwareManager');
      expect(await hwManagerEndpoint.isOnline()).toBeTruthy();
    });
  });

  describe('printTicket message synchronization', () => {
    const printTicketMsg = { type: 'printTicket', consumeOffline: true };
    test.each`
      messages            | numberOfCalls | status
      ${[printTicketMsg]} | ${1}          | ${'online'}
      ${[printTicketMsg]} | ${1}          | ${'offline'}
    `(
      'printTicket message consumed with status $status',
      async ({ messages, status, numberOfCalls }) => {
        hwManagerEndpoint.online = status === 'online' ? true : false;
        hwManagerEndpoint.printTicket = jest.fn();
        OB.App.MessageModelController.findAll
          .mockResolvedValueOnce(messages)
          .mockResolvedValue([]);

        await syncBuffer.internalFlush();

        expect(hwManagerEndpoint.printTicket).toHaveBeenCalledTimes(
          numberOfCalls
        );
      }
    );
  });

  describe('displayTotal message synchronization', () => {
    const displayTotalMsg = { type: 'displayTotal', consumeOffline: true };
    test.each`
      messages             | numberOfCalls | status
      ${[displayTotalMsg]} | ${1}          | ${'online'}
      ${[displayTotalMsg]} | ${1}          | ${'offline'}
    `(
      'displayTotal message consumed with status $status',
      async ({ messages, status, numberOfCalls }) => {
        hwManagerEndpoint.online = status === 'online' ? true : false;
        hwManagerEndpoint.displayTotal = jest.fn();
        OB.App.MessageModelController.findAll
          .mockResolvedValueOnce(messages)
          .mockResolvedValue([]);

        await syncBuffer.internalFlush();

        expect(hwManagerEndpoint.displayTotal).toHaveBeenCalledTimes(
          numberOfCalls
        );
      }
    );
  });

  describe('printTicketLine message synchronization', () => {
    const printTicketLineMsg = {
      type: 'printTicketLine',
      consumeOffline: true
    };
    test.each`
      messages                | numberOfCalls | status
      ${[printTicketLineMsg]} | ${1}          | ${'online'}
      ${[printTicketLineMsg]} | ${1}          | ${'offline'}
    `(
      'displayTotal message consumed with status $status',
      async ({ messages, status, numberOfCalls }) => {
        hwManagerEndpoint.online = status === 'online' ? true : false;
        hwManagerEndpoint.printTicketLine = jest.fn();
        OB.App.MessageModelController.findAll
          .mockResolvedValueOnce(messages)
          .mockResolvedValue([]);

        await syncBuffer.internalFlush();

        expect(hwManagerEndpoint.printTicketLine).toHaveBeenCalledTimes(
          numberOfCalls
        );
      }
    );
  });
});
