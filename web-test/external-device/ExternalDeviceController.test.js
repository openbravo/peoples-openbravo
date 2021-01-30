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
  App: {
    Class: {},
    PrintTemplateStore: { get: jest.fn() },
    Request: { get: jest.fn(), post: jest.fn() },
    SynchronizationBuffer: { goOnline: jest.fn(), goOffline: jest.fn() },
    TerminalProperty: { get: jest.fn() },
    UserNotifier: { notifyError: jest.fn() },
    View: { DialogUIHandler: { askConfirmation: jest.fn() } }
  },
  I18N: { getLabel: jest.fn() },
  UTIL: {
    localStorage: { setItem: jest.fn(), getItem: jest.fn() }
  }
};

global.lodash = require('../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
require('../../web/org.openbravo.retail.posterminal/app/external-device/ExternalDeviceController');

describe('ExternalDeviceController', () => {
  const printTemplate = {
    generate: () => {
      return {
        data: '<output></output>'
      };
    }
  };
  const printPDFTemplate = {
    ispdf: true,
    generate: params => {
      return {
        param: params.ticket,
        mainReport: {},
        subReports: []
      };
    }
  };

  beforeEach(() => {
    jest.resetAllMocks();
    OB.App.TerminalProperty.get.mockImplementation(property => {
      if (property === 'terminal') {
        return {
          id: '12FA7864FBDE4FCC8D6E3891A936D61F',
          searchKey: 'VBS1001',
          hardwareurl: 'http://127.0.0.1:8090/printer'
        };
      }
      if (property === 'hardwareURL') {
        return [
          {
            id: '1',
            _identifier: 'Test Printer 1',
            hardwareURL: 'http://127.0.1.1:8090',
            hasReceiptPrinter: true,
            hasPDFPrinter: true
          },
          {
            id: '2',
            _identifier: 'Test Printer 2 (Receipt only)',
            hardwareURL: 'http://127.0.2.1:8090',
            hasReceiptPrinter: true,
            hasPDFPrinter: false
          },
          {
            id: '3',
            _identifier: 'Test Printer 3 (PDF only)',
            hardwareURL: 'http://127.0.3.1:8090',
            hasReceiptPrinter: false,
            hasPDFPrinter: true
          }
        ];
      }
      return {};
    });
  });

  it('get identifier of active URLs', async () => {
    OB.I18N.getLabel.mockReturnValue('Main Terminal Printer');

    const controller = new OB.App.Class.ExternalDeviceController();
    expect(controller.getActiveURLIdentifier(false)).toBe(
      'Main Terminal Printer'
    );
    expect(controller.getActiveURLIdentifier(true)).toBe(
      'Main Terminal Printer'
    );
  });

  it('change active URLs', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();
    controller.setActiveURL('2');
    controller.setActivePDFURL('3');

    expect(controller.getActiveURLIdentifier(false)).toBe(
      'Test Printer 2 (Receipt only)'
    );
    expect(controller.getActiveURLIdentifier(true)).toBe(
      'Test Printer 3 (PDF only)'
    );
  });

  it('main URL is active', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();
    expect(controller.isMainURLActive()).toBe(true);
  });

  it('has ticket printer available', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();
    expect(controller.hasAvailablePrinter(false)).toBe(true);
  });

  it('has PDF printer available', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();
    expect(controller.hasAvailablePrinter(true)).toBe(true);
  });

  it('getHardwareManagerStatus', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();
    OB.App.Request.get.mockResolvedValue({
      version: '1.0.204901',
      revision: '1.0.204901',
      javaInfo: 'Java Info'
    });

    await controller.getHardwareManagerStatus();

    expect(OB.App.Request.get.mock.calls[0][0]).toBe(
      'http://127.0.0.1:8090/status.json'
    );
    expect(OB.App.SynchronizationBuffer.goOnline).toHaveBeenCalled();
  });

  it('getHardwareManagerStatus request failure', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();
    OB.App.Request.get.mockRejectedValue(new Error('Request error'));

    try {
      await controller.getHardwareManagerStatus();
    } catch (error) {
      // expected error
    }

    expect(OB.App.SynchronizationBuffer.goOffline).toHaveBeenCalled();
    expect(OB.App.UserNotifier.notifyError).toHaveBeenCalledWith({
      message: 'OBPOS_MsgHardwareServerNotAvailable'
    });
  });

  it('display', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();
    controller.requestPrint = jest.fn();
    controller.storeData = jest.fn();

    await controller.display(printTemplate);

    expect(controller.requestPrint.mock.calls[0][0]).toBe('<output></output>');
    expect(controller.requestPrint.mock.calls[0][1]).toBe(
      controller.devices.DISPLAY
    );
    expect(controller.storeData.mock.calls[0][0]).toBe('<output></output>');
    expect(controller.storeData.mock.calls[0][1]).toBe(
      controller.devices.DISPLAY
    );
  });

  it('openDrawer', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();
    const templateContent = '<output><opendrawer/></output>';
    OB.App.PrintTemplateStore.get.mockReturnValue({
      generate: async () => {
        return { data: templateContent };
      }
    });
    controller.requestPrint = jest.fn();
    controller.storeData = jest.fn();

    await controller.openDrawer();

    expect(OB.App.PrintTemplateStore.get.mock.calls[0][0]).toBe(
      'openDrawerTemplate'
    );
    expect(controller.requestPrint.mock.calls[0][0]).toBe(templateContent);
    expect(controller.requestPrint.mock.calls[0][1]).toBe(
      controller.devices.DRAWER
    );
    expect(controller.storeData.mock.calls[0][0]).toBe(templateContent);
    expect(controller.storeData.mock.calls[0][1]).toBe(
      controller.devices.DRAWER
    );
  });

  it('print template', async () => {
    const ticket = { id: '1' };
    const controller = new OB.App.Class.ExternalDeviceController();
    controller.requestPrint = jest.fn();
    controller.storeData = jest.fn();

    await controller.print(printTemplate, { ticket });

    expect(controller.requestPrint.mock.calls[0][0]).toBe('<output></output>');
    expect(controller.requestPrint.mock.calls[0][1]).toBe(
      controller.devices.PRINTER
    );
    expect(controller.storeData.mock.calls[0][0]).toBe('<output></output>');
    expect(controller.storeData.mock.calls[0][1]).toBe(
      controller.devices.PRINTER
    );
  });

  it('print PDF template', async () => {
    const ticket = { id: '1' };
    const controller = new OB.App.Class.ExternalDeviceController();
    controller.requestPDFPrint = jest.fn();
    controller.storeData = jest.fn();

    await controller.print(printPDFTemplate, { ticket });

    expect(controller.requestPDFPrint.mock.calls[0][0]).toBe(
      JSON.stringify({
        param: ticket,
        mainReport: {},
        subReports: []
      })
    );
    expect(controller.storeData.mock.calls[0][0]).toBe(
      JSON.stringify({
        param: ticket,
        mainReport: {},
        subReports: []
      })
    );
    expect(controller.storeData.mock.calls[0][1]).toBe(
      controller.devices.PRINTER
    );
  });

  it('print with web printer', async () => {
    const ticket = { id: '1' };
    const controller = new OB.App.Class.ExternalDeviceController();
    // simulate controller has webPrinter configured and connected
    controller.webPrinter = { connected: () => true, print: jest.fn() };
    controller.requestWebPrinter = jest.fn();
    controller.storeData = jest.fn();

    await controller.print(printTemplate, { ticket });

    expect(controller.requestWebPrinter.mock.calls[0][0]).toBe(
      '<output></output>'
    );
    expect(controller.storeData.mock.calls[0][0]).toBe('<output></output>');
    expect(controller.storeData.mock.calls[0][1]).toBe(
      controller.devices.PRINTER
    );
  });

  it('print request', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();

    const requestData = '<output></output>';
    await controller.requestPrint(requestData, controller.devices.PRINTER);

    expect(OB.App.Request.post.mock.calls[0][0]).toBe(
      'http://127.0.0.1:8090/printer'
    );
    expect(OB.App.Request.post.mock.calls[0][1]).toBe(requestData);
    expect(OB.App.SynchronizationBuffer.goOnline).toHaveBeenCalled();
  });

  it('print request failure', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();
    OB.App.Request.post.mockRejectedValue(new Error('Request error'));

    try {
      await controller.requestPrint(
        '<output></output>',
        controller.devices.PRINTER
      );
    } catch (error) {
      // expected error
    }
    expect(OB.App.Request.post.mock.calls[0][0]).toBe(
      'http://127.0.0.1:8090/printer'
    );
    expect(OB.App.SynchronizationBuffer.goOffline).toHaveBeenCalled();
    expect(OB.App.UserNotifier.notifyError).toHaveBeenCalledWith({
      message: 'OBPOS_MsgHardwareServerNotAvailable'
    });
  });

  it('print PDF request', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();

    const requestData = '{}';
    await controller.requestPDFPrint(requestData);

    expect(OB.App.Request.post.mock.calls[0][0]).toBe(
      'http://127.0.0.1:8090/printerpdf'
    );
    expect(OB.App.Request.post.mock.calls[0][1]).toBe(requestData);
  });

  it('print PDF request failure', async () => {
    const controller = new OB.App.Class.ExternalDeviceController();
    OB.App.Request.post.mockRejectedValue(new Error('Request error'));

    try {
      await controller.requestPDFPrint('{}');
    } catch (error) {
      // expected error
    }
    expect(OB.App.Request.post.mock.calls[0][0]).toBe(
      'http://127.0.0.1:8090/printerpdf'
    );
    expect(OB.App.UserNotifier.notifyError).toHaveBeenCalledWith({
      message: 'OBPOS_MsgHardwareServerNotAvailable'
    });
  });

  it('connect + request to web printer', async () => {
    const ticket = { id: '1' };
    const controller = new OB.App.Class.ExternalDeviceController();
    // simulate controller has webPrinter configured and not yet connected
    controller.webPrinter = {
      connected: () => false,
      print: jest.fn(),
      request: jest.fn()
    };

    OB.App.View.DialogUIHandler.askConfirmation.mockResolvedValue(true);
    await controller.print(printTemplate, { ticket });

    expect(controller.webPrinter.request).toHaveBeenCalled();
    expect(controller.webPrinter.print.mock.calls[0][0]).toBe(
      '<output></output>'
    );
  });

  it('not print if can not connect to web printer', async () => {
    const ticket = { id: '1' };
    const controller = new OB.App.Class.ExternalDeviceController();
    controller.webPrinter = {
      connected: () => false,
      print: jest.fn(),
      request: jest.fn()
    };

    OB.App.View.DialogUIHandler.askConfirmation.mockResolvedValue(false);
    await controller.print(printTemplate, { ticket });

    expect(controller.webPrinter.request).not.toHaveBeenCalled();
    expect(controller.webPrinter.print).not.toHaveBeenCalled();
  });

  it('request to connected web printer', async () => {
    const ticket = { id: '1' };
    const controller = new OB.App.Class.ExternalDeviceController();
    controller.webPrinter = { connected: () => true, print: jest.fn() };

    await controller.print(printTemplate, { ticket });

    expect(controller.webPrinter.print.mock.calls[0][0]).toBe(
      '<output></output>'
    );
  });
});
