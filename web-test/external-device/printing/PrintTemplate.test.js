/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/* global global  */

global.OB = {
  App: {
    Class: {},
    Request: { get: jest.fn() },
    TerminalProperty: {
      get: jest.fn().mockReturnValue({})
    },
    OrgVariables: {
      getAll: jest.fn().mockReturnValue([
        {
          id: '4C5E56D05A144C5BA0B22AC1F77C72F6',
          variable: 'test_variable',
          value: 'Test value',
          translatable: false,
          language: null,
          langName: null,
          langCode: null,
          active: true
        },
        {
          id: '4C5E56D05A144C5BA0B22AC1F77C72F6',
          variable: 'test_variable2',
          value: 'Test value in english',
          translatable: true,
          language: '2C5E56D05A144C5BA0B22AC1F77C72F6',
          langName: 'English (USA)',
          langCode: 'en_US',
          active: true
        }
      ]),
      getOrgVariable: jest.fn()
    }
  },
  I18N: { getLabel: jest.fn() },
  UTIL: {
    localStorage: { getItem: jest.fn() },
    encodeXMLComponent: jest.fn(),
    TicketUtils: { toOrder: jest.fn(), toOrderLine: jest.fn() }
  }
};

global.lodash = require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.21');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/printing/PrintTemplate');
const getPrintTemplateMock = require('./PrintTemplateMock');

describe('PrintTemplate', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should getOrgVariable being called twice', async () => {
    const ticket = { grossAmount: 23 };
    const printTemplate = getPrintTemplateMock('template.xml');

    OB.UTIL.encodeXMLComponent.mockImplementation(value => value);
    OB.I18N.getLabel.mockReturnValue('Total');

    expect(OB.App.OrgVariables.getOrgVariable).toHaveBeenCalledTimes(2);
  });

  it('get template data', async () => {
    const resource = '../org.openbravo.retail.posterminal/res/template.xml';
    const resourcedata = '<output></output>';
    const printTemplate = new OB.App.Class.PrintTemplate(
      'testTemplate',
      resource
    );

    OB.App.Request.get.mockResolvedValue(resourcedata);

    await printTemplate.generate();
    expect(printTemplate.resource).toBe(resource);
    expect(printTemplate.resourcedata).toBe(resourcedata);
    expect(OB.App.Request.get).toHaveBeenCalledTimes(1);

    // call generate() again, resourcedata is now cached
    await printTemplate.generate();
    expect(printTemplate.resource).toBe(resource);
    expect(printTemplate.resourcedata).toBe(resourcedata);
    // check that now no request to the backend have been performed
    expect(OB.App.Request.get).toHaveBeenCalledTimes(1);
  });

  it('caches xml template', async () => {
    const resource = '../org.openbravo.retail.posterminal/res/template.xml';
    const resourcedata = '<output></output>';
    const printTemplate = new OB.App.Class.PrintTemplate(
      'testTemplate',
      resource
    );

    OB.App.Request.get.mockResolvedValue(resourcedata);

    await printTemplate.generate();
    expect(printTemplate.resource).toBe(resource);
    expect(printTemplate.resourcedata).toBe(resourcedata);
    expect(OB.App.Request.get).toHaveBeenCalledTimes(1);

    // call generate() again, resourcedata is now cached
    await printTemplate.generate();
    expect(printTemplate.resource).toBe(resource);
    expect(printTemplate.resourcedata).toBe(resourcedata);

    // No new request has been done as it is already cached
    expect(OB.App.Request.get).toHaveBeenCalledTimes(1);
  });

  it('intializes after reset', async () => {
    const resource = '../org.openbravo.retail.posterminal/res/template.xml';
    const printTemplate = new OB.App.Class.PrintTemplate(
      'testTemplate',
      resource
    );

    await printTemplate.generate();
    expect(OB.App.Request.get).toHaveBeenCalledTimes(1);

    printTemplate.reset();

    // call generate() again, resourcedata is now cached
    await printTemplate.generate();

    // No new request has been done as it is already cached
    expect(OB.App.Request.get).toHaveBeenCalledTimes(2);
  });

  it('prepare params in standard template', async () => {
    const params = { ticket: { id: 't1' }, ticketLine: { id: 'l1' } };
    const printTemplate = new OB.App.Class.PrintTemplate(
      'testTemplate',
      '../org.openbravo.retail.posterminal/res/template.xml'
    );

    OB.UTIL.TicketUtils.toOrder.mockReturnValue({});
    OB.UTIL.TicketUtils.toOrderLine.mockReturnValue({});

    const result = await printTemplate.prepareParams(params);
    expect(result).toStrictEqual({
      getOrgVariable: expect.any(Function),
      ticket: { id: 't1' },
      ticketLine: { id: 'l1' }
    });
  });

  it('prepare params in legacy template', async () => {
    const params = { ticket: { id: 't1' }, ticketLine: { id: 'l1' } };
    const printTemplate = new OB.App.Class.PrintTemplate(
      'testTemplate',
      '../org.openbravo.retail.posterminal/res/template.xml',
      {
        isLegacy: true
      }
    );

    OB.UTIL.TicketUtils.toOrder.mockReturnValue({ id: 'o1' });
    OB.UTIL.TicketUtils.toOrderLine.mockReturnValue({ id: 'ol1' });

    const result = await printTemplate.prepareParams(params);
    expect(result).toStrictEqual({
      ticket: { id: 't1' },
      ticketLine: { id: 'l1' },
      order: { id: 'o1' },
      line: { id: 'ol1' }
    });
  });

  describe('initializer', () => {
    let printTemplate;
    beforeEach(() => {
      printTemplate = new OB.App.Class.PrintTemplate(
        'testTemplate',
        '../org.openbravo.retail.posterminal/res/template.xml'
      );
    });

    it('executes default if none defined', async () => {
      await printTemplate.generate();

      expect(OB.App.Request.get).toHaveBeenCalledTimes(1);
    });

    it('is executed when registered', async () => {
      const initializer = jest.fn();
      OB.App.PrintTemplate.registerInitializer(initializer);

      await printTemplate.generate();

      expect(initializer).toHaveBeenCalledTimes(1);
      expect(OB.App.Request.get).not.toHaveBeenCalled();
    });

    it('last overwrites first', async () => {
      const initializer1 = jest.fn();
      const initializer2 = jest.fn();

      OB.App.PrintTemplate.registerInitializer(initializer1);
      OB.App.PrintTemplate.registerInitializer(initializer2);

      await printTemplate.generate();

      expect(initializer2).toHaveBeenCalledTimes(1);
      expect(initializer1).not.toHaveBeenCalled();
    });
  });
});
