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
    TerminalProperty: { get: jest.fn() }
  }
};

global.lodash = require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.21');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/printing/PrintTemplate');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/printing/PrintTemplateStore');

describe('PrintTemplateStore', () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  it('template registry, get resource from terminal', async () => {
    const templateName = 'testPrintTemplate';
    const resource = '../org.openbravo.retail.posterminal/res/template.xml';
    const defaultResource =
      '../org.openbravo.retail.posterminal/res/default.xml';

    OB.App.TerminalProperty.get.mockReturnValue({
      testPrintTemplate: resource
    });

    OB.App.PrintTemplateStore.register(templateName, defaultResource);
    const template = OB.App.PrintTemplateStore.get(templateName);

    expect(template.resource).toBe(resource);
  });

  it('can not register a template with an already registered name', async () => {
    const templateName = 'testPrintTemplate';
    const resource = '../org.openbravo.retail.posterminal/res/template.xml';

    OB.App.TerminalProperty.get.mockReturnValue({
      testPrintTemplate: resource
    });

    expect(() =>
      OB.App.PrintTemplateStore.register(templateName, resource)
    ).toThrow('A template with name testPrintTemplate already exists');
  });

  it('template registry, get default resource', async () => {
    const templateName = 'anotherTestPrintTemplate';
    const defaultResource =
      '../org.openbravo.retail.posterminal/res/default.xml';

    OB.App.TerminalProperty.get.mockReturnValue({});

    OB.App.PrintTemplateStore.register(templateName, defaultResource);
    const template = OB.App.PrintTemplateStore.get(templateName);

    expect(template.resource).toBe(defaultResource);
  });

  it('overwrite registered template', async () => {
    const templateName = 'overwrittenTestPrintTemplate';
    const defaultResource =
      '../org.openbravo.retail.posterminal/res/default.xml';
    const customResource = '../org.openbravo.retail.posterminal/res/custom.xml';

    OB.App.TerminalProperty.get.mockReturnValue({});

    OB.App.PrintTemplateStore.register(templateName, defaultResource);
    OB.App.PrintTemplateStore.overwrite(templateName, customResource);
    const template = OB.App.PrintTemplateStore.get(templateName);

    expect(template.resource).toBe(customResource);
  });

  it('select ticket print template', async () => {
    const ticket = { orderType: 0, lines: [{ qty: 1 }] };
    const resource = '../org.openbravo.retail.posterminal/res/printreceipt.xml';

    OB.App.TerminalProperty.get.mockReturnValue({});

    const template = OB.App.PrintTemplateStore.selectTicketPrintTemplate(
      ticket
    );

    expect(template.resource).toBe(resource);
  });

  test.each`
    ticket                                                                                      | expected
    ${{ ordercanceled: true, lines: [{ qty: 1 }] }}                                             | ${'printCanceledReceiptTemplate'}
    ${{ cancelLayaway: true, lines: [{ qty: 1 }] }}                                             | ${'printCanceledLayawayTemplate'}
    ${{ isInvoice: true, orderType: 1, fullInvoice: true, lines: [{ qty: 1 }] }}                | ${'printReturnInvoiceTemplate'}
    ${{ isInvoice: true, fullInvoice: true, lines: [{ qty: -1 }] }}                             | ${'printReturnInvoiceTemplate'}
    ${{ isInvoice: true, orderType: 1, fullInvoice: false, lines: [{ qty: 1 }] }}               | ${'printSimplifiedReturnInvoiceTemplate'}
    ${{ isInvoice: true, fullInvoice: false, lines: [{ qty: -1 }] }}                            | ${'printSimplifiedReturnInvoiceTemplate'}
    ${{ isInvoice: true, orderType: 0, isQuotation: true, lines: [{ qty: 1 }] }}                | ${'printQuotationTemplate'}
    ${{ isInvoice: true, orderType: 0, fullInvoice: true, isPaid: true, lines: [{ qty: 1 }] }}  | ${'printClosedInvoiceTemplate'}
    ${{ isInvoice: true, orderType: 0, fullInvoice: false, isPaid: true, lines: [{ qty: 1 }] }} | ${'printSimplifiedClosedInvoiceTemplate'}
    ${{ isInvoice: true, orderType: 0, fullInvoice: true, lines: [{ qty: 1 }] }}                | ${'printInvoiceTemplate'}
    ${{ isInvoice: true, orderType: 0, fullInvoice: false, lines: [{ qty: 1 }] }}               | ${'printSimplifiedInvoiceTemplate'}
    ${{ orderType: 1, isPaid: true, lines: [{ qty: 1 }] }}                                      | ${'printReturnTemplate'}
    ${{ orderType: 0, isQuotation: true, isPaid: true, lines: [{ qty: 1 }] }}                   | ${'printQuotationTemplate'}
    ${{ orderType: 0, isPaid: true, lines: [{ qty: 1 }] }}                                      | ${'printClosedReceiptTemplate'}
    ${{ orderType: 0, lines: [{ qty: 1 }] }}                                                    | ${'printTicketTemplate'}
    ${{ orderType: 1, lines: [{ qty: 1 }] }}                                                    | ${'printReturnTemplate'}
    ${{ orderType: 2, lines: [{ qty: 1 }] }}                                                    | ${'printLayawayTemplate'}
    ${{ orderType: 3, lines: [{ qty: 1 }] }}                                                    | ${'printLayawayTemplate'}
    ${{ lines: [{ qty: -1 }] }}                                                                 | ${'printReturnTemplate'}
  `("Print template for '$ticket' is '$expected' ", ({ ticket, expected }) => {
    expect(
      OB.App.PrintTemplateStore.selectTicketPrintTemplateName(ticket)
    ).toBe(expected);
  });

  it('forced template selection', async () => {
    const ticket = { lines: [] };
    const forcedTemplate = 'forcedPrintTemplate';
    expect(
      OB.App.PrintTemplateStore.selectTicketPrintTemplateName(ticket, {
        forcedtemplate: forcedTemplate
      })
    ).toBe(forcedTemplate);
  });
});
