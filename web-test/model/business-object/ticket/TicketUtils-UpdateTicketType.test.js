/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */
global.OB = {
  App: {
    Class: {},
    TerminalProperty: { get: jest.fn() },
    UUID: { generate: jest.fn() }
  }
};

global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');

require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
OB.App.StateAPI.registerModel('Ticket');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');

// set Ticket model utility functions
OB.App.State = { Ticket: { Utils: {} } };
OB.App.StateAPI.Ticket.utilities.forEach(
  util => (OB.App.State.Ticket.Utils[util.functionName] = util.implementation)
);

describe('Ticket Utils updateTicketType method', () => {
  it('should set sale type for positive ticket', () => {
    const ticket = deepfreeze({ organization: 'A', lines: [{ qty: 1 }] });
    const newTicket = OB.App.State.Ticket.Utils.updateTicketType(ticket, {
      terminal: {
        organization: 'A',
        terminalType: { documentType: 'Sale', documentTypeForReturns: 'Return' }
      }
    });
    expect(newTicket).toEqual({
      organization: 'A',
      orderType: 0,
      documentType: 'Sale',
      lines: [{ qty: 1 }]
    });
  });

  it('should set return type for negative ticket', () => {
    const ticket = deepfreeze({ organization: 'A', lines: [{ qty: -1 }] });
    const newTicket = OB.App.State.Ticket.Utils.updateTicketType(ticket, {
      terminal: {
        organization: 'A',
        terminalType: { documentType: 'Sale', documentTypeForReturns: 'Return' }
      }
    });
    expect(newTicket).toEqual({
      organization: 'A',
      orderType: 1,
      documentType: 'Return',
      lines: [{ qty: -1 }]
    });
  });

  it('should not change type for cross store ticket', () => {
    const ticket = deepfreeze({ organization: 'A' });
    const newTicket = OB.App.State.Ticket.Utils.updateTicketType(ticket, {
      terminal: {
        organization: 'B',
        terminalType: { documentType: 'Sale', documentTypeForReturns: 'Return' }
      }
    });
    expect(newTicket).toEqual({
      organization: 'A'
    });
  });
});
