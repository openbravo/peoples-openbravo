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
const {
  executeActionPreparations
} = require('../../../../../org.openbravo.mobile.core/web-test/base/state-utils');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/exception/TranslatableError');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/ActionCanceled');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/TicketList');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket-list/actions/RemoveTickets');

describe('Remove Empty Tickets action', () => {
  const action = OB.App.StateAPI.TicketList.removeTickets;

  it('removes empty tickets from the ticket list', () => {
    const ticketList = [
      { id: 'B', lines: [] },
      { id: 'C', lines: [{ id: 'C1' }] }
    ];
    const payload = { removeFilter: ticket => ticket.lines.length === 0 };
    deepfreeze(ticketList);
    const newTicketList = action(ticketList, payload);
    expect(newTicketList).toEqual([{ id: 'C', lines: [{ id: 'C1' }] }]);
  });
});

describe('Remove Empty Tickets action preparations', () => {
  it('correct remove filter provided', async () => {
    const ticketList = [
      { id: 'B', lines: [] },
      { id: 'C', lines: [{ id: 'C1' }] }
    ];
    const payload = { removeFilter: ticket => ticket };
    const newPayload = await executeActionPreparations(
      OB.App.StateAPI.TicketList.removeTickets,
      deepfreeze(ticketList),
      deepfreeze(payload)
    );

    expect(newPayload).toEqual(payload);
  });

  it('throw error if no remove filter is provided', async () => {
    const ticketList = [
      { id: 'B', lines: [] },
      { id: 'C', lines: [{ id: 'C1' }] }
    ];
    const payload = {};
    let error;
    try {
      await executeActionPreparations(
        OB.App.StateAPI.TicketList.removeTickets,
        deepfreeze(ticketList),
        deepfreeze(payload)
      );
    } catch (e) {
      error = e;
    }
    expect(error.message).toEqual('Missing remove filter function');
  });

  it('throw error if wrong remove filter is provided', async () => {
    const ticketList = [
      { id: 'B', lines: [] },
      { id: 'C', lines: [{ id: 'C1' }] }
    ];
    const payload = { removeFilter: 'notAFunction' };
    let error;
    try {
      await executeActionPreparations(
        OB.App.StateAPI.TicketList.removeTickets,
        deepfreeze(ticketList),
        deepfreeze(payload)
      );
    } catch (e) {
      error = e;
    }
    expect(error.message).toEqual('Missing remove filter function');
  });
});
