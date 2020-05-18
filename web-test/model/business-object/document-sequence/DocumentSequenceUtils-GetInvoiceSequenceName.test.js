/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupDocumentSequence');
require('./SetupDocumentSequenceUtils');
const deepfreeze = require('deepfreeze');

describe('Document Sequence Utils getInvoiceSequenceName method', () => {
  it('should generate simplified invoice sequence name for empty invoice', () => {
    const ticket = deepfreeze({});
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket
    );
    expect(sequenceName).toEqual('simplifiedinvoiceslastassignednum');
  });

  it('should generate full invoice sequence name for full invoice', () => {
    const ticket = deepfreeze({ fullInvoice: true, lines: [{ qty: 1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      'FRI',
      'SRI',
      false
    );
    expect(sequenceName).toEqual('fullinvoiceslastassignednum');
  });

  it('should generate full return invoice sequence name for full return invoice', () => {
    const ticket = deepfreeze({ fullInvoice: true, lines: [{ qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      'FRI',
      'SRI',
      false
    );
    expect(sequenceName).toEqual('fullreturninvoiceslastassignednum');
  });

  it('should generate simplified invoice sequence name for simplified invoice', () => {
    const ticket = deepfreeze({ fullInvoice: false, lines: [{ qty: 1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      'FRI',
      'SRI',
      false
    );
    expect(sequenceName).toEqual('simplifiedinvoiceslastassignednum');
  });

  it('should generate simplified return invoice sequence name for simplified return invoice', () => {
    const ticket = deepfreeze({ fullInvoice: false, lines: [{ qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      'FRI',
      'SRI',
      false
    );
    expect(sequenceName).toEqual('simplifiedreturninvoiceslastassignednum');
  });

  it('should generate full invoice sequence name if full return invoice sequence is not defined', () => {
    const ticket = deepfreeze({ fullInvoice: true, lines: [{ qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      null,
      'SRI',
      false
    );
    expect(sequenceName).toEqual('fullinvoiceslastassignednum');
  });

  it('should generate simplified invoice sequence name if simplified return invoice sequence is not defined', () => {
    const ticket = deepfreeze({ fullInvoice: false, lines: [{ qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      'FRI',
      null,
      false
    );
    expect(sequenceName).toEqual('simplifiedinvoiceslastassignednum');
  });

  it('should generate full invoice sequence name if one negative line and no salesWithOneLineNegativeAsReturns', () => {
    const ticket = deepfreeze({
      fullInvoice: true,
      lines: [{ qty: 1 }, { qty: -1 }]
    });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      'FRI',
      'SRI',
      false
    );
    expect(sequenceName).toEqual('fullinvoiceslastassignednum');
  });

  it('should generate full return invoice sequence name if one negative line and salesWithOneLineNegativeAsReturns', () => {
    const ticket = deepfreeze({
      fullInvoice: true,
      lines: [{ qty: 1 }, { qty: -1 }]
    });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      'FRI',
      'SRI',
      true
    );
    expect(sequenceName).toEqual('fullreturninvoiceslastassignednum');
  });

  it('should generate simplified invoice sequence name if one negative line and no salesWithOneLineNegativeAsReturns', () => {
    const ticket = deepfreeze({
      fullInvoice: false,
      lines: [{ qty: 1 }, { qty: -1 }]
    });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      'FRI',
      'SRI',
      false
    );
    expect(sequenceName).toEqual('simplifiedinvoiceslastassignednum');
  });

  it('should generate simplified return invoice sequence name if one negative line and salesWithOneLineNegativeAsReturns', () => {
    const ticket = deepfreeze({
      fullInvoice: false,
      lines: [{ qty: 1 }, { qty: -1 }]
    });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      'FRI',
      'SRI',
      true
    );
    expect(sequenceName).toEqual('simplifiedreturninvoiceslastassignednum');
  });
});
