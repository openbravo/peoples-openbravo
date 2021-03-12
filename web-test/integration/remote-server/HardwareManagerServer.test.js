/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at https://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */

global.OB = {
  info: jest.fn(),
  App: {
    Class: {},
    TerminalProperty: { get: jest.fn() }
  }
};

require('../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3.min.js');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/integration/remote-server/RemoteServerController');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/integration/remote-server/RemoteServer');
require('../../../web/org.openbravo.retail.posterminal/app/integration/remote-server/HardwareManagerServer');

describe('HardwareManagerServer', () => {
  const HardwareManagerServerClass = OB.App.RemoteServerController.getRemoteServer(
    'HardwareManagerServer'
  ).constructor;
  let hwManagerServer;

  beforeEach(() => {
    jest.clearAllMocks();
    hwManagerServer = new HardwareManagerServerClass();
    OB.App.TerminalProperty.get.mockImplementation(property => {
      switch (property) {
        case 'hardwareURL':
          return [
            {
              hardwareURL: 'https://127.0.1.1:8090',
              hasPDFPrinter: true,
              hasReceiptPrinter: true,
              barcode: null,
              id: '1',
              _identifier: 'Test Printer 1'
            },
            {
              hardwareURL: 'https://127.0.2.1:8090',
              hasPDFPrinter: false,
              hasReceiptPrinter: true,
              barcode: null,
              id: '2',
              _identifier: 'Test Printer 2 (Receipt only)'
            },
            {
              hardwareURL: 'https://127.0.3.1:8090',
              hasPDFPrinter: true,
              hasReceiptPrinter: false,
              barcode: null,
              id: '3',
              _identifier: 'Test Printer 3 (PDF only)'
            }
          ];
        case 'terminal':
          return {
            hardwareurl: 'https://127.0.0.1:8090/printer',
            scaleurl: 'https://127.0.0.1:8090/scale'
          };
        default:
          throw new Error(`not mocked ${property}`);
      }
    });
  });

  afterAll(() => {
    jest.restoreAllMocks();
  });

  it('get name', () => {
    expect(hwManagerServer.getName()).toBe('HardwareManagerServer');
  });

  it('get from RemoteServerController', () => {
    expect(
      OB.App.RemoteServerController.getRemoteServer('HardwareManagerServer')
    ).not.toBeNull();
  });

  test.each`
    url                                                          | isAttended
    ${'../../org.openbravo.mobile.core.service.jsonrest/'}       | ${false}
    ${'../../org.openbravo.mobile.core.loginutils'}              | ${false}
    ${'../../org.openbravo.mobile.core.checkServerAvailability'} | ${false}
    ${'https://127.0.1.1:8090/printer'}                          | ${true}
    ${'https://127.0.2.1:8090/printer'}                          | ${true}
    ${'https://127.0.1.1:8090/printerpdf'}                       | ${true}
    ${'https://127.0.3.1:8090/printerpdf'}                       | ${true}
    ${'https://127.0.0.1:8090/printer'}                          | ${true}
    ${'https://127.0.0.1:8090/scale'}                            | ${true}
    ${'https://127.0.0.1:8090/status.json'}                      | ${true}
  `("Backend server attends to '$url' ($isAttended)", ({ url, isAttended }) => {
    expect(hwManagerServer.isAttendedURL(url)).toBe(isAttended);
  });
});
