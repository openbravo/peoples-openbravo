/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */

global.OB = {
  error: jest.fn(),
  App: {
    Class: {},
    MessageModelController: {
      add: jest.fn(),
      put: jest.fn(),
      findAll: jest.fn(),
      delete: jest.fn()
    },
    TerminalProperty: {
      get: jest.fn().mockImplementation(property => {
        if (property === 'hardwareURL') {
          return [];
        } else if (property === 'terminal') {
          return {
            terminalType: { printTwice: false, selectprinteralways: true }
          };
        }
        return {};
      })
    }
  },
  I18N: { getLabel: jest.fn() },
  UTIL: {
    localStorage: {
      getItem: jest.fn(),
      setItem: jest.fn(),
      removeItem: jest.fn()
    }
  }
};

require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/synchronization-buffer/SynchronizationBuffer');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/synchronization-buffer/SynchronizationEndpoint');
require('../../../web/org.openbravo.retail.posterminal/app/model/synchronization-buffer/HardwareManagerEndpoint');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/ExternalDeviceController');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/printing/PrintTemplate');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/printing/PrintTemplateStore');

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
    jest.clearAllMocks();

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

  describe('initHardwareManager message synchronization', () => {
    const initHardwareManagerMsg = {
      type: 'initHardwareManager',
      consumeOffline: true
    };
    test.each`
      messages                    | numberOfCalls | status
      ${[initHardwareManagerMsg]} | ${1}          | ${'online'}
      ${[initHardwareManagerMsg]} | ${1}          | ${'offline'}
    `(
      'initHardwareManager message consumed with status $status',
      async ({ messages, status, numberOfCalls }) => {
        hwManagerEndpoint.online = status === 'online' ? true : false;
        hwManagerEndpoint.initHardwareManager = jest.fn();
        OB.App.MessageModelController.findAll
          .mockResolvedValueOnce(messages)
          .mockResolvedValue([]);

        await syncBuffer.internalFlush();

        expect(hwManagerEndpoint.initHardwareManager).toHaveBeenCalledTimes(
          numberOfCalls
        );
      }
    );
  });

  describe('openDrawer message synchronization', () => {
    const openDrawerMsg = {
      type: 'openDrawer',
      consumeOffline: true
    };
    test.each`
      messages           | numberOfCalls | status
      ${[openDrawerMsg]} | ${1}          | ${'online'}
      ${[openDrawerMsg]} | ${1}          | ${'offline'}
    `(
      'openDrawer message consumed with status $status',
      async ({ messages, status, numberOfCalls }) => {
        hwManagerEndpoint.online = status === 'online' ? true : false;
        hwManagerEndpoint.openDrawer = jest.fn();
        OB.App.MessageModelController.findAll
          .mockResolvedValueOnce(messages)
          .mockResolvedValue([]);

        await syncBuffer.internalFlush();

        expect(hwManagerEndpoint.openDrawer).toHaveBeenCalledTimes(
          numberOfCalls
        );
      }
    );
  });

  describe('printWelcome message synchronization', () => {
    const printWelcomeMsg = {
      type: 'printWelcome',
      consumeOffline: true
    };
    test.each`
      messages             | numberOfCalls | status
      ${[printWelcomeMsg]} | ${1}          | ${'online'}
      ${[printWelcomeMsg]} | ${1}          | ${'offline'}
    `(
      'printWelcome message consumed with status $status',
      async ({ messages, status, numberOfCalls }) => {
        hwManagerEndpoint.online = status === 'online' ? true : false;
        hwManagerEndpoint.printWelcome = jest.fn();
        OB.App.MessageModelController.findAll
          .mockResolvedValueOnce(messages)
          .mockResolvedValue([]);

        await syncBuffer.internalFlush();

        expect(hwManagerEndpoint.printWelcome).toHaveBeenCalledTimes(
          numberOfCalls
        );
      }
    );
  });

  describe('new type of message synchronization', () => {
    const printTicketMsg = { type: 'newType' };
    const synchronization = jest.fn();

    test.each`
      messages            | numberOfCalls | status
      ${[printTicketMsg]} | ${1}          | ${'online'}
      ${[printTicketMsg]} | ${0}          | ${'offline'}
    `(
      'printTicket calls with status $status: $numberOfCalls',
      async ({ messages, status, numberOfCalls }) => {
        syncBuffer.addMessageSynchronization(
          'HardwareManager',
          'newType',
          async message => {
            await synchronization(message);
          }
        );
        hwManagerEndpoint.online = status === 'online' ? true : false;
        OB.App.MessageModelController.findAll
          .mockResolvedValueOnce(messages)
          .mockResolvedValue([]);

        await syncBuffer.internalFlush();

        expect(synchronization).toHaveBeenCalledTimes(numberOfCalls);
      }
    );
  });

  it('printWelcome', async () => {
    const printTemplate = new OB.App.Class.PrintTemplate();

    hwManagerEndpoint.controller.display = jest.fn();
    OB.App.PrintTemplateStore.get = jest.fn().mockResolvedValue(printTemplate);

    await hwManagerEndpoint.printWelcome();

    expect(OB.App.PrintTemplateStore.get.mock.calls[0][0]).toBe(
      'printWelcomeTemplate'
    );
    expect(hwManagerEndpoint.controller.display.mock.calls[0][0]).toBe(
      printTemplate
    );
  });

  it('initHardwareManager', async () => {
    hwManagerEndpoint.controller.getHardwareManagerStatus = jest
      .fn()
      .mockResolvedValue({
        version: '1.0.204901',
        revision: 'r1.0.204901',
        javaInfo: 'Java Info'
      });

    hwManagerEndpoint.printWelcome = jest.fn();

    OB.UTIL.localStorage.setItem.mockClear();
    await hwManagerEndpoint.initHardwareManager();

    expect(hwManagerEndpoint.printWelcome).toHaveBeenCalledTimes(1);
    expect(OB.UTIL.localStorage.setItem.mock.calls[0][0]).toBe(
      'hardwareManagerVersion'
    );
    expect(OB.UTIL.localStorage.setItem.mock.calls[0][1]).toBe('1.0.204901');
    expect(OB.UTIL.localStorage.setItem.mock.calls[1][0]).toBe(
      'hardwareManagerRevision'
    );
    expect(OB.UTIL.localStorage.setItem.mock.calls[1][1]).toBe('r1.0.204901');

    expect(OB.UTIL.localStorage.setItem.mock.calls[2][0]).toBe(
      'hardwareManagerJavaInfo'
    );
    expect(OB.UTIL.localStorage.setItem.mock.calls[2][1]).toBe('Java Info');
  });

  it('initHardwareManager - no info retrieved', async () => {
    hwManagerEndpoint.controller.getHardwareManagerStatus = jest
      .fn()
      .mockResolvedValue({});

    hwManagerEndpoint.printWelcome = jest.fn();

    OB.UTIL.localStorage.setItem.mockClear();
    await hwManagerEndpoint.initHardwareManager();

    expect(hwManagerEndpoint.printWelcome).not.toHaveBeenCalled();
    expect(OB.UTIL.localStorage.setItem).not.toHaveBeenCalled();
  });

  it('openDrawer', async () => {
    hwManagerEndpoint.controller.openDrawer = jest.fn();

    await hwManagerEndpoint.openDrawer();

    expect(hwManagerEndpoint.controller.openDrawer).toHaveBeenCalledTimes(1);
  });
});
