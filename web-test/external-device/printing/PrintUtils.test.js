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
  App: {},
  Format: {
    defaultDecimalSymbol: '.',
    defaultGroupingSymbol: ',',
    defaultGroupingSize: '3',
    formats: { priceInform: '#0.00', qtyEdition: '#0.###' }
  },
  I18N: { getLabel: jest.fn() }
};

global.lodash = require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.21');
require('../../../../org.openbravo.client.application/web/org.openbravo.client.application/js/utilities/ob-utilities-number.js');
require('../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3.min.js');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.21');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-i18n');
require('../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');
require('../../../web/org.openbravo.retail.posterminal/app/external-device/printing/PrintUtils');

describe('PrintUtils', () => {
  it('printAmount', () => {
    expect(OB.App.PrintUtils.printAmount(23)).toBe('23.00');
  });

  it('printQty', () => {
    expect(OB.App.PrintUtils.printQty(23)).toBe('23');
  });

  it('printTicketLineAmount - price includes tax', () => {
    const ticketLine = { priceIncludesTax: true, baseGrossUnitAmount: 150.5 };
    expect(OB.App.PrintUtils.printTicketLineAmount(ticketLine)).toBe('150.50');
  });

  it('printTicketLineAmount - price not includes tax', () => {
    const ticketLine = { priceIncludesTax: false, baseNetUnitAmount: 150.5 };
    expect(OB.App.PrintUtils.printTicketLineAmount(ticketLine)).toBe('150.50');
  });

  it('printTicketLinePrice - price includes tax', () => {
    const ticketLine = { priceIncludesTax: true, baseGrossUnitPrice: 150.5 };
    expect(OB.App.PrintUtils.printTicketLinePrice(ticketLine)).toBe('150.50');
  });

  it('printTicketLinePrice - price not includes tax', () => {
    const ticketLine = { priceIncludesTax: false, baseNetUnitPrice: 150.5 };
    expect(OB.App.PrintUtils.printTicketLinePrice(ticketLine)).toBe('150.50');
  });

  it('getChangeLabelFromTicket with changePayments', () => {
    const ticket = {
      payments: [
        {
          kind: 'OBPOS_payment.cash',
          paymentData: {
            key: 'OBPOS_payment.cash',
            label: '30.60€'
          }
        }
      ]
    };
    expect(OB.App.PrintUtils.getChangeLabelFromTicket(ticket)).toBe('30.60€');
  });

  it('getChangeLabelFromTicket (with changePayments)', () => {
    const ticket = {
      changePayments: [
        {
          label: '30.60€'
        },
        {
          label: '10.23$'
        }
      ]
    };
    expect(OB.App.PrintUtils.getChangeLabelFromTicket(ticket)).toBe(
      '30.60€ + 10.23$'
    );
  });
});
