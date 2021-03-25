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
    PrintUtils: {
      printAmount: jest.fn(),
      printQty: jest.fn(),
      printTicketLinePrice: jest.fn(),
      printTicketLineAmount: jest.fn(),
      getChangeLabelFromTicket: jest.fn()
    },
    Request: { get: jest.fn() },
    Security: { hasPermission: jest.fn() },
    State: { Ticket: { Utils: { getPendingAmount: jest.fn() } } },
    TerminalProperty: {
      get: jest.fn()
    }
  },
  DEC: { abs: jest.fn(), add: jest.fn(), sub: jest.fn() },
  I18N: {
    getLabel: jest.fn(),
    formatCurrency: jest.fn(),
    formatDate: jest.fn(),
    formatHour: jest.fn(),
    formatRate: jest.fn()
  },
  UTIL: {
    encodeXMLComponent: jest.fn()
  }
};

global.lodash = require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/printing/PrintTemplate');
const getPrintTemplateMock = require('./PrintTemplateMock');
const ticket = require('./printableTicket.json');

describe('Print Template Generation', () => {
  beforeAll(() => {
    OB.App.TerminalProperty.get.mockImplementation(property => {
      switch (property) {
        case 'context':
          return { user: { _identifier: 'U1' } };
        case 'terminal':
          return { organization: { _identifier: 'O1' } };
        default:
          throw new Error(`Unkwnown property ${property}`);
      }
    });
  });
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test.each`
    template
    ${'checkdrawerstatus'}
    ${'cleandisplay'}
    ${'displayreceipt'}
    ${'displaytotal'}
    ${'goodbye'}
    ${'opendrawer'}
    ${'printcanceledlayaway'}
    ${'printcanceledreceipt'}
    ${'printclosedinvoice'}
    ${'printclosedreceipt'}
    ${'printinvoice'}
    ${'printlayaway'}
    ${'printline'}
    ${'printquotation'}
    ${'printreceipt32'}
    ${'printreceipt'}
    ${'printreturninvoice'}
    ${'printreturn'}
    ${'printsimplifiedclosedinvoice'}
    ${'printsimplifiedinvoice'}
    ${'printsimplifiedreturninvoice'}
    ${'welcome'}
  `('generate $template template', async ({ template }) => {
    const path = `../../../web/org.openbravo.retail.posterminal/res/${template}.xml`;
    const printTemplate = getPrintTemplateMock(path);

    await expect(
      printTemplate.generate({ ticket, ticketLine: ticket.lines[0] })
    ).resolves.not.toBeNull();
  });

  test.each`
    template
    ${'printcashmgmt'}
  `('generate $template template', async ({ template }) => {
    const cashmgmt = [];
    const path = `../../../web/org.openbravo.retail.posterminal/res/${template}.xml`;
    const printTemplate = getPrintTemplateMock(path);

    await expect(printTemplate.generate({ cashmgmt })).resolves.not.toBeNull();
  });
});
