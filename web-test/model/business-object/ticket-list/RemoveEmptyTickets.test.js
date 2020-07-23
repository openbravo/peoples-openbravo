/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */

OB = { App: { Class: {} } };
global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
const deepfreeze = require('deepfreeze');

require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionCanceled');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/TicketList');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/actions/RemoveEmptyTickets');

describe('Remove Empty Tickets action', () => {
  const action = OB.App.StateAPI.TicketList.removeEmptyTickets;

  it('removes empty tickets from the ticket list', () => {
    const ticketList = [
      { id: 'B', lines: [] },
      { id: 'C', lines: [{ id: 'C1' }] }
    ];
    const payload = {};
    deepfreeze(ticketList);
    const newTicketList = action(ticketList, payload);
    expect(newTicketList).toEqual([{ id: 'C', lines: [{ id: 'C1' }] }]);
  });
});
