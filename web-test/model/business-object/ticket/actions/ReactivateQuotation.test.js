/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../SetupTicket');
require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/actions/ReactivateQuotation');
require('../SetupTicketUtils');
const deepfreeze = require('deepfreeze');

const basicQuotation = deepfreeze({
  lines: [
    {
      id: '1',
      product: { id: 'p1' }
    },
    {
      id: '2',
      product: { id: 'p2' }
    }
  ],
  isEditable: false
});

const servicesQuotation = deepfreeze({
  lines: [
    {
      id: '1',
      product: { id: 'p1' }
    },
    {
      id: '2',
      product: { id: 'p2' },
      relatedLines: [{ id: '1', product: { id: 'p1' } }]
    }
  ],
  isEditable: false
});
describe('Ticket.reactivateQuotation action', () => {
  it('reactivateQuotation basic quotation', () => {
    const reactivatedQuotation = OB.App.StateAPI.Ticket.reactivateQuotation(
      basicQuotation,
      {}
    );
    expect(reactivatedQuotation.isEditable).toBeTruthy();
    expect(reactivatedQuotation.lines[0].id).not.toBe('1');
  });
  it('reactivateQuotation services quotation', () => {
    const reactivatedQuotation = OB.App.StateAPI.Ticket.reactivateQuotation(
      servicesQuotation,
      {}
    );
    expect(reactivatedQuotation.isEditable).toBeTruthy();
    expect(reactivatedQuotation.lines[1].relatedLines[0].orderlineId).toBe(
      reactivatedQuotation.lines[0].id
    );
  });
});