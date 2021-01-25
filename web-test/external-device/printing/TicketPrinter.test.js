/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
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

require('../../../web/org.openbravo.retail.posterminal/app/external-device/ExternalDeviceController');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/printing/PrintTemplate');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/printing/PrintTemplateStore');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/printing/TicketPrinter');

describe('TicketPrinter', () => {
  let ticketPrinter;

  beforeEach(() => {
    jest.clearAllMocks();

    ticketPrinter = new OB.App.Class.TicketPrinter();
  });

  it('displayTotal', async () => {
    const printTemplate = new OB.App.Class.PrintTemplate();
    const ticket = { id: '1' };

    ticketPrinter.templateStore.get = jest
      .fn()
      .mockResolvedValue(printTemplate);

    ticketPrinter.controller.display = jest.fn();

    await ticketPrinter.displayTotal({ messageObj: { data: { ticket } } });

    expect(ticketPrinter.templateStore.get.mock.calls[0][0]).toBe(
      'printDisplayTotalTemplate'
    );
    expect(ticketPrinter.controller.display.mock.calls[0][0]).toBe(
      printTemplate
    );
    expect(ticketPrinter.controller.display.mock.calls[0][1]).toStrictEqual({
      ticket
    });
  });

  it('printTicket', async () => {
    const ticket = { id: '1', lines: [], payments: [] };

    ticketPrinter.doPrintTicket = jest.fn();
    await ticketPrinter.printTicket({ messageObj: { data: { ticket } } });

    expect(ticketPrinter.doPrintTicket).toHaveBeenCalledTimes(1);
    expect(ticketPrinter.doPrintTicket.mock.calls[0][0]).toStrictEqual(ticket);
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

    ticketPrinter.doPrintTicket = jest.fn();
    await ticketPrinter.printTicket({ messageObj: { data: { ticket } } });

    expect(ticketPrinter.doPrintTicket).toHaveBeenCalledTimes(2);
    expect(ticketPrinter.doPrintTicket.mock.calls[0][0]).toStrictEqual(ticket);
    expect(ticketPrinter.doPrintTicket.mock.calls[1][0]).toStrictEqual({
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

    ticketPrinter.controller.print = jest.fn();
    await ticketPrinter.doPrintTicket(ticket);

    expect(ticketPrinter.controller.print).not.toHaveBeenCalled();
  });

  it('doPrintTicket - not printable ticket not printed', async () => {
    const ticket = {
      id: '1',
      lines: [],
      print: false
    };

    ticketPrinter.controller.print = jest.fn();
    ticketPrinter.controller.executeHooks = jest.fn().mockResolvedValue({});
    OB.App.State.Ticket.Utils.isNegative.mockReturnValue(false);
    await ticketPrinter.doPrintTicket(ticket, {});

    expect(ticketPrinter.controller.print).not.toHaveBeenCalled();
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

      ticketPrinter.templateStore.selectTicketPrintTemplate = jest
        .fn()
        .mockResolvedValue(printTemplate);
      OB.App.TerminalProperty.get.mockImplementation(property =>
        property === 'terminal' ? { terminalType: { printTwice } } : {}
      );
      ticketPrinter.controller.print = jest.fn();
      ticketPrinter.displayTicket = jest.fn();
      ticketPrinter.retryPrintTicket = jest.fn();
      ticketPrinter.controller.executeHooks = jest
        .fn()
        .mockResolvedValue({ ticket });
      OB.App.State.Ticket.Utils.isNegative.mockReturnValue(false);
      ticketPrinter.selectPrinter = jest.fn();

      await ticketPrinter.doPrintTicket(ticket, {});

      // select template
      expect(
        ticketPrinter.templateStore.selectTicketPrintTemplate
      ).toHaveBeenCalledTimes(1);
      // printer selection
      expect(ticketPrinter.selectPrinter).toHaveBeenCalledTimes(1);
      // print ticket
      expect(ticketPrinter.controller.print).toHaveBeenCalledTimes(times);
      // display total
      expect(ticketPrinter.displayTicket).toHaveBeenCalledTimes(1);
      // retry is not called
      expect(ticketPrinter.retryPrintTicket).not.toHaveBeenCalled();
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

    ticketPrinter.templateStore.selectTicketPrintTemplate = jest
      .fn()
      .mockResolvedValue(printTemplate);
    ticketPrinter.controller.print = jest
      .fn()
      .mockRejectedValue(new Error('print failed'));
    ticketPrinter.displayTicket = jest.fn();
    ticketPrinter.retryPrintTicket = jest.fn();
    ticketPrinter.controller.executeHooks = jest
      .fn()
      .mockResolvedValue({ ticket });
    OB.App.State.Ticket.Utils.isNegative.mockReturnValue(false);
    ticketPrinter.selectPrinter = jest.fn();

    await ticketPrinter.doPrintTicket(ticket, {});

    // select template
    expect(
      ticketPrinter.templateStore.selectTicketPrintTemplate
    ).toHaveBeenCalledTimes(1);
    // printer selection
    expect(ticketPrinter.selectPrinter).toHaveBeenCalledTimes(1);
    // print ticket (fails)
    expect(ticketPrinter.controller.print).toHaveBeenCalledTimes(1);
    // not display total
    expect(ticketPrinter.displayTicket).not.toHaveBeenCalled();
    // retry
    expect(ticketPrinter.retryPrintTicket).toHaveBeenCalledTimes(1);
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
      ticketPrinter.canSelectPrinter = jest.fn().mockReturnValue(true);
      ticketPrinter.doPrintTicket = jest.fn();

      await ticketPrinter.retryPrintTicket(ticket);

      expect(ticketPrinter.doPrintTicket).toHaveBeenCalledTimes(times);
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
      ticketPrinter.canSelectPrinter = jest.fn().mockReturnValue(canSelect);
      ticketPrinter.controller.setActiveURL = jest.fn();

      await ticketPrinter.selectPrinter(options);

      expect(ticketPrinter.controller.setActiveURL).toHaveBeenCalledTimes(
        times
      );
    }
  );

  it('selectPrinter - select ticket printer', async () => {
    const printer = '1';

    OB.App.View.DialogUIHandler.inputData.mockResolvedValue({ printer });
    ticketPrinter.canSelectPrinter = jest.fn().mockReturnValue(true);
    ticketPrinter.controller.setActiveURL = jest.fn();
    ticketPrinter.controller.setActivePDFURL = jest.fn();

    const result = await ticketPrinter.selectPrinter({});

    expect(result).toBe(printer);
    expect(ticketPrinter.controller.setActiveURL).toHaveBeenCalledWith(printer);
    expect(ticketPrinter.controller.setActivePDFURL).not.toHaveBeenCalled();
  });

  it('selectPrinter - select PDF printer', async () => {
    const printer = '1';

    OB.App.View.DialogUIHandler.inputData.mockResolvedValue({ printer });
    ticketPrinter.canSelectPrinter = jest.fn().mockReturnValue(true);
    ticketPrinter.controller.setActiveURL = jest.fn();
    ticketPrinter.controller.setActivePDFURL = jest.fn();

    const result = await ticketPrinter.selectPrinter({ isPdf: true });

    expect(result).toBe(printer);
    expect(ticketPrinter.controller.setActiveURL).not.toHaveBeenCalled();
    expect(ticketPrinter.controller.setActivePDFURL).toHaveBeenCalledWith(
      printer
    );
  });

  it('selectPrinter - printer not selected', async () => {
    const printer = '1';

    OB.App.View.DialogUIHandler.inputData.mockResolvedValue({ printer });
    ticketPrinter.canSelectPrinter = jest.fn().mockReturnValue(false);
    ticketPrinter.controller.setActiveURL = jest.fn();
    ticketPrinter.controller.setActivePDFURL = jest.fn();

    const result = await ticketPrinter.selectPrinter({});

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
      ticketPrinter.controller.hasAvailablePrinter = jest
        .fn()
        .mockReturnValue(hasAvailablePrinter);

      expect(ticketPrinter.canSelectPrinter()).toBe(expected);
    }
  );

  it('displayTicket', async () => {
    const printTemplate = new OB.App.Class.PrintTemplate();
    const ticket = { id: '1' };

    ticketPrinter.templateStore.get = jest
      .fn()
      .mockResolvedValue(printTemplate);
    ticketPrinter.controller.display = jest.fn();

    await ticketPrinter.displayTicket(ticket);

    expect(ticketPrinter.templateStore.get.mock.calls[0][0]).toBe(
      'displayReceiptTemplate'
    );
    expect(ticketPrinter.controller.display.mock.calls[0][0]).toBe(
      printTemplate
    );
    expect(ticketPrinter.controller.display.mock.calls[0][1]).toStrictEqual({
      ticket
    });
  });

  it('printTicketLine', async () => {
    const printTemplate = new OB.App.Class.PrintTemplate();
    const line = { id: 'l1' };

    ticketPrinter.templateStore.get = jest
      .fn()
      .mockResolvedValue(printTemplate);
    ticketPrinter.controller.display = jest.fn();

    await ticketPrinter.printTicketLine({ messageObj: { data: { line } } });

    expect(ticketPrinter.templateStore.get.mock.calls[0][0]).toBe(
      'printReceiptLineTemplate'
    );
    expect(ticketPrinter.controller.display.mock.calls[0][0]).toBe(
      printTemplate
    );
    expect(ticketPrinter.controller.display.mock.calls[0][1]).toStrictEqual({
      ticketLine: line
    });
  });
});
