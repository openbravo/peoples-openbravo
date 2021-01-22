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
  debug: jest.fn(),
  error: jest.fn(),
  App: {
    Class: {},
    MessageModelController: {
      add: jest.fn(),
      put: jest.fn(),
      findAll: jest.fn(),
      delete: jest.fn()
    },
    Security: {
      hasPermission: jest.fn()
    },
    State: {
      Ticket: {
        Utils: {
          isNegative: jest.fn()
        }
      }
    },
    TerminalProperty: {
      get: jest.fn()
    },
    View: {
      DialogUIHandler: {
        askConfirmation: jest.fn(),
        inputData: jest.fn()
      }
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
    jest.resetAllMocks();

    OB.App.TerminalProperty.get.mockImplementation(property => {
      if (property === 'hardwareURL') {
        return [];
      } else if (property === 'terminal') {
        return {
          terminalType: { printTwice: false, selectprinteralways: true }
        };
      }
      return {};
    });

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

  describe('message consumption implementation', () => {
    it('displayTotal', async () => {
      const printTemplate = new OB.App.Class.PrintTemplate();
      const ticket = { id: '1' };

      hwManagerEndpoint.templateStore.get = jest
        .fn()
        .mockResolvedValue(printTemplate);

      hwManagerEndpoint.controller.display = jest.fn();

      await hwManagerEndpoint.displayTotal({ data: { ticket } });

      expect(hwManagerEndpoint.templateStore.get.mock.calls[0][0]).toBe(
        'printDisplayTotalTemplate'
      );
      expect(hwManagerEndpoint.controller.display.mock.calls[0][0]).toBe(
        printTemplate
      );
      expect(
        hwManagerEndpoint.controller.display.mock.calls[0][1]
      ).toStrictEqual({
        ticket
      });
    });

    it('greetHardwareManager', async () => {
      hwManagerEndpoint.controller.getHardwareManagerStatus = jest
        .fn()
        .mockResolvedValue({
          version: '1.0.204901',
          revision: 'r1.0.204901',
          javaInfo: 'Java Info'
        });

      hwManagerEndpoint.printWelcome = jest.fn();

      OB.UTIL.localStorage.setItem.mockClear();
      await hwManagerEndpoint.greetHardwareManager();

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

    it('greetHardwareManager - no info retrieved', async () => {
      hwManagerEndpoint.controller.getHardwareManagerStatus = jest
        .fn()
        .mockResolvedValue({});

      hwManagerEndpoint.printWelcome = jest.fn();

      OB.UTIL.localStorage.setItem.mockClear();
      await hwManagerEndpoint.greetHardwareManager();

      expect(hwManagerEndpoint.printWelcome).not.toHaveBeenCalled();
      expect(OB.UTIL.localStorage.setItem).not.toHaveBeenCalled();
    });

    it('printTicket', async () => {
      const ticket = { id: '1', lines: [], payments: [] };

      hwManagerEndpoint.doPrintTicket = jest.fn();
      await hwManagerEndpoint.printTicket({ data: { ticket } });

      expect(hwManagerEndpoint.doPrintTicket).toHaveBeenCalledTimes(1);
      expect(hwManagerEndpoint.doPrintTicket.mock.calls[0][0]).toStrictEqual(
        ticket
      );
    });

    it('openDrawer', async () => {
      hwManagerEndpoint.controller.openDrawer = jest.fn();

      await hwManagerEndpoint.openDrawer();

      expect(hwManagerEndpoint.controller.openDrawer).toHaveBeenCalledTimes(1);
    });

    it('printTicket - with canceled ticket', async () => {
      const ticket = {
        id: '1',
        lines: [],
        payments: [],
        doCancelAndReplace: true,
        negativeDocNo: 'N1',
        canceledorder: { id: '2', lines: [], payments: [] }
      };

      hwManagerEndpoint.doPrintTicket = jest.fn();
      await hwManagerEndpoint.printTicket({ data: { ticket } });

      expect(hwManagerEndpoint.doPrintTicket).toHaveBeenCalledTimes(2);
      expect(hwManagerEndpoint.doPrintTicket.mock.calls[0][0]).toStrictEqual(
        ticket
      );
      expect(hwManagerEndpoint.doPrintTicket.mock.calls[1][0]).toStrictEqual({
        ...ticket.canceledorder,
        ordercanceled: true,
        negativeDocNo: 'N1'
      });
    });

    it('doPrintTicket - simplified invoice will print only the invoice', async () => {
      const ticket = {
        id: '1',
        calculatedInvoice: { fullInvoice: false }
      };

      hwManagerEndpoint.controller.print = jest.fn();
      await hwManagerEndpoint.doPrintTicket(ticket);

      expect(hwManagerEndpoint.controller.print).not.toHaveBeenCalled();
    });

    it('doPrintTicket - not printable ticket not printed', async () => {
      const ticket = {
        id: '1',
        lines: [],
        print: false
      };

      hwManagerEndpoint.controller.print = jest.fn();
      hwManagerEndpoint.controller.executeHooks = jest
        .fn()
        .mockResolvedValue({});
      OB.App.State.Ticket.Utils.isNegative.mockReturnValue(false);
      await hwManagerEndpoint.doPrintTicket(ticket, {});

      expect(hwManagerEndpoint.controller.print).not.toHaveBeenCalled();
    });

    it.each`
      description      | printTwice | times
      ${'print once'}  | ${false}   | ${1}
      ${'print twice'} | ${true}    | ${2}
    `(
      'doPrintTicket - standard flow: $description',
      async ({ printTwice, times }) => {
        const ticket = {
          id: '1',
          lines: [],
          payments: [],
          print: true
        };
        const printTemplate = new OB.App.Class.PrintTemplate();

        hwManagerEndpoint.templateStore.selectTicketPrintTemplate = jest
          .fn()
          .mockResolvedValue(printTemplate);
        OB.App.TerminalProperty.get.mockImplementation(property =>
          property === 'terminal' ? { terminalType: { printTwice } } : {}
        );
        hwManagerEndpoint.controller.print = jest.fn();
        hwManagerEndpoint.displayTicket = jest.fn();
        hwManagerEndpoint.retryPrintTicket = jest.fn();
        hwManagerEndpoint.controller.executeHooks = jest
          .fn()
          .mockResolvedValue({});
        OB.App.State.Ticket.Utils.isNegative.mockReturnValue(false);
        hwManagerEndpoint.selectPrinter = jest.fn();

        await hwManagerEndpoint.doPrintTicket(ticket, {});

        // select template
        expect(
          hwManagerEndpoint.templateStore.selectTicketPrintTemplate
        ).toHaveBeenCalledTimes(1);
        // printer selection
        expect(hwManagerEndpoint.selectPrinter).toHaveBeenCalledTimes(1);
        // print ticket
        expect(hwManagerEndpoint.controller.print).toHaveBeenCalledTimes(times);
        // display total
        expect(hwManagerEndpoint.displayTicket).toHaveBeenCalledTimes(1);
        // retry is not called
        expect(hwManagerEndpoint.retryPrintTicket).not.toHaveBeenCalled();
      }
    );

    it('doPrintTicket - retry on error', async () => {
      const ticket = {
        id: '1',
        lines: [],
        payments: [],
        print: true
      };
      const printTemplate = new OB.App.Class.PrintTemplate();

      hwManagerEndpoint.templateStore.selectTicketPrintTemplate = jest
        .fn()
        .mockResolvedValue(printTemplate);
      hwManagerEndpoint.controller.print = jest
        .fn()
        .mockRejectedValue(new Error('print failed'));
      hwManagerEndpoint.displayTicket = jest.fn();
      hwManagerEndpoint.retryPrintTicket = jest.fn();
      hwManagerEndpoint.controller.executeHooks = jest
        .fn()
        .mockResolvedValue({});
      OB.App.State.Ticket.Utils.isNegative.mockReturnValue(false);
      hwManagerEndpoint.selectPrinter = jest.fn();

      await hwManagerEndpoint.doPrintTicket(ticket, {});

      // select template
      expect(
        hwManagerEndpoint.templateStore.selectTicketPrintTemplate
      ).toHaveBeenCalledTimes(1);
      // printer selection
      expect(hwManagerEndpoint.selectPrinter).toHaveBeenCalledTimes(1);
      // print ticket (fails)
      expect(hwManagerEndpoint.controller.print).toHaveBeenCalledTimes(1);
      // not display total
      expect(hwManagerEndpoint.displayTicket).not.toHaveBeenCalled();
      // retry
      expect(hwManagerEndpoint.retryPrintTicket).toHaveBeenCalledTimes(1);
    });

    it.each`
      confirmation | times
      ${true}      | ${1}
      ${false}     | ${0}
    `(
      'retryPrintTicket - confirmation: $confirmation',
      async ({ confirmation, times }) => {
        const ticket = {
          id: '1',
          lines: [],
          payments: [],
          print: true
        };

        OB.App.View.DialogUIHandler.askConfirmation.mockResolvedValue(
          confirmation
        );
        hwManagerEndpoint.canSelectPrinter = jest.fn().mockReturnValue(true);
        hwManagerEndpoint.doPrintTicket = jest.fn();

        await hwManagerEndpoint.retryPrintTicket(ticket);

        expect(hwManagerEndpoint.doPrintTicket).toHaveBeenCalledTimes(times);
      }
    );

    it.each`
      options                          | terminalData                                        | canSelect | times
      ${{ skipSelectPrinters: false }} | ${{ terminalType: { selectprinteralways: false } }} | ${true}   | ${0}
      ${{}}                            | ${{ terminalType: { selectprinteralways: false } }} | ${false}  | ${0}
      ${{}}                            | ${{ terminalType: { selectprinteralways: true } }}  | ${false}  | ${0}
      ${{ forceSelect: true }}         | ${{ terminalType: { selectprinteralways: false } }} | ${true}   | ${1}
      ${{ forceSelect: true }}         | ${{ terminalType: { selectprinteralways: true } }}  | ${false}  | ${1}
      ${{}}                            | ${{ terminalType: { selectprinteralways: true } }}  | ${true}   | ${1}
    `(
      'selectPrinter - options: $options, terminalData: $terminalData, canSelect: $canSelect',
      async ({ options, terminalData, canSelect, times }) => {
        const printer = '1';

        OB.App.View.DialogUIHandler.inputData.mockResolvedValue({ printer });
        OB.App.TerminalProperty.get.mockImplementation(property =>
          property === 'terminal' ? terminalData : {}
        );
        hwManagerEndpoint.canSelectPrinter = jest
          .fn()
          .mockReturnValue(canSelect);
        hwManagerEndpoint.controller.setActiveURL = jest.fn();

        await hwManagerEndpoint.selectPrinter(options);

        expect(hwManagerEndpoint.controller.setActiveURL).toHaveBeenCalledTimes(
          times
        );
      }
    );

    it('selectPrinter - select ticket printer', async () => {
      const printer = '1';

      OB.App.View.DialogUIHandler.inputData.mockResolvedValue({ printer });
      hwManagerEndpoint.canSelectPrinter = jest.fn().mockReturnValue(true);
      hwManagerEndpoint.controller.setActiveURL = jest.fn();
      hwManagerEndpoint.controller.setActivePDFURL = jest.fn();

      const result = await hwManagerEndpoint.selectPrinter({});

      expect(result).toBe(printer);
      expect(hwManagerEndpoint.controller.setActiveURL).toHaveBeenCalledWith(
        printer
      );
      expect(
        hwManagerEndpoint.controller.setActivePDFURL
      ).not.toHaveBeenCalled();
    });

    it('selectPrinter - select PDF printer', async () => {
      const printer = '1';

      OB.App.View.DialogUIHandler.inputData.mockResolvedValue({ printer });
      hwManagerEndpoint.canSelectPrinter = jest.fn().mockReturnValue(true);
      hwManagerEndpoint.controller.setActiveURL = jest.fn();
      hwManagerEndpoint.controller.setActivePDFURL = jest.fn();

      const result = await hwManagerEndpoint.selectPrinter({ isPdf: true });

      expect(result).toBe(printer);
      expect(hwManagerEndpoint.controller.setActiveURL).not.toHaveBeenCalled();
      expect(hwManagerEndpoint.controller.setActivePDFURL).toHaveBeenCalledWith(
        printer
      );
    });

    it('selectPrinter - printer not selected', async () => {
      const printer = '1';

      OB.App.View.DialogUIHandler.inputData.mockResolvedValue({ printer });
      hwManagerEndpoint.canSelectPrinter = jest.fn().mockReturnValue(false);
      hwManagerEndpoint.controller.setActiveURL = jest.fn();
      hwManagerEndpoint.controller.setActivePDFURL = jest.fn();

      const result = await hwManagerEndpoint.selectPrinter({});

      expect(result).toBeNull();
    });

    it.each`
      hasPermission | hasAvailablePrinter | expected
      ${true}       | ${true}             | ${true}
      ${true}       | ${false}            | ${false}
      ${false}      | ${true}             | ${false}
      ${false}      | ${false}            | ${false}
    `(
      'hasPermission: $hasPermission, hasAvailablePrinter: $hasAvailablePrinter',
      async ({ hasPermission, hasAvailablePrinter, expected }) => {
        OB.App.Security.hasPermission.mockReturnValue(hasPermission);
        hwManagerEndpoint.controller.hasAvailablePrinter = jest
          .fn()
          .mockReturnValue(hasAvailablePrinter);

        expect(hwManagerEndpoint.canSelectPrinter()).toBe(expected);
      }
    );

    it('displayTicket', async () => {
      const printTemplate = new OB.App.Class.PrintTemplate();
      const ticket = { id: '1' };

      hwManagerEndpoint.templateStore.get = jest
        .fn()
        .mockResolvedValue(printTemplate);
      hwManagerEndpoint.controller.display = jest.fn();

      await hwManagerEndpoint.displayTicket(ticket);

      expect(hwManagerEndpoint.templateStore.get.mock.calls[0][0]).toBe(
        'displayReceiptTemplate'
      );
      expect(hwManagerEndpoint.controller.display.mock.calls[0][0]).toBe(
        printTemplate
      );
      expect(
        hwManagerEndpoint.controller.display.mock.calls[0][1]
      ).toStrictEqual({
        ticket
      });
    });

    it('printTicketLine', async () => {
      const printTemplate = new OB.App.Class.PrintTemplate();
      const line = { id: 'l1' };

      hwManagerEndpoint.templateStore.get = jest
        .fn()
        .mockResolvedValue(printTemplate);
      hwManagerEndpoint.controller.display = jest.fn();

      await hwManagerEndpoint.printTicketLine({ data: { line } });

      expect(hwManagerEndpoint.templateStore.get.mock.calls[0][0]).toBe(
        'printReceiptLineTemplate'
      );
      expect(hwManagerEndpoint.controller.display.mock.calls[0][0]).toBe(
        printTemplate
      );
      expect(
        hwManagerEndpoint.controller.display.mock.calls[0][1]
      ).toStrictEqual({
        ticketLine: line
      });
    });

    it('printWelcome', async () => {
      const printTemplate = new OB.App.Class.PrintTemplate();

      hwManagerEndpoint.templateStore.get = jest
        .fn()
        .mockResolvedValue(printTemplate);
      hwManagerEndpoint.controller.display = jest.fn();

      await hwManagerEndpoint.printWelcome();

      expect(hwManagerEndpoint.templateStore.get.mock.calls[0][0]).toBe(
        'printWelcomeTemplate'
      );
      expect(hwManagerEndpoint.controller.display.mock.calls[0][0]).toBe(
        printTemplate
      );
    });
  });
});
