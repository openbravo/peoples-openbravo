/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupDocumentSequenceAndTicket');
const deepfreeze = require('deepfreeze');

describe('Document Sequence Utils getInvoiceSequenceName function', () => {
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
      {
        terminal: {
          fullReturnInvoiceDocNoPrefix: 'FRI',
          simplifiedReturnInvoiceDocNoPrefix: 'SRI'
        }
      }
    );
    expect(sequenceName).toEqual('fullinvoiceslastassignednum');
  });

  it('should generate full return invoice sequence name for full return invoice', () => {
    const ticket = deepfreeze({ fullInvoice: true, lines: [{ qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      {
        terminal: {
          fullReturnInvoiceDocNoPrefix: 'FRI',
          simplifiedReturnInvoiceDocNoPrefix: 'SRI'
        }
      }
    );
    expect(sequenceName).toEqual('fullreturninvoiceslastassignednum');
  });

  it('should generate simplified invoice sequence name for simplified invoice', () => {
    const ticket = deepfreeze({ fullInvoice: false, lines: [{ qty: 1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      {
        terminal: {
          fullReturnInvoiceDocNoPrefix: 'FRI',
          simplifiedReturnInvoiceDocNoPrefix: 'SRI'
        }
      }
    );
    expect(sequenceName).toEqual('simplifiedinvoiceslastassignednum');
  });

  it('should generate simplified return invoice sequence name for simplified return invoice', () => {
    const ticket = deepfreeze({ fullInvoice: false, lines: [{ qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      {
        terminal: {
          fullReturnInvoiceDocNoPrefix: 'FRI',
          simplifiedReturnInvoiceDocNoPrefix: 'SRI'
        }
      }
    );
    expect(sequenceName).toEqual('simplifiedreturninvoiceslastassignednum');
  });

  it('should generate full invoice sequence name if full return invoice sequence is not defined', () => {
    const ticket = deepfreeze({ fullInvoice: true, lines: [{ qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      {
        terminal: {
          simplifiedReturnInvoiceDocNoPrefix: 'SRI'
        }
      }
    );
    expect(sequenceName).toEqual('fullinvoiceslastassignednum');
  });

  it('should generate simplified invoice sequence name if simplified return invoice sequence is not defined', () => {
    const ticket = deepfreeze({ fullInvoice: false, lines: [{ qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket,
      {
        terminal: {
          fullReturnInvoiceDocNoPrefix: 'FRI'
        }
      }
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
      {
        terminal: {
          fullReturnInvoiceDocNoPrefix: 'FRI',
          simplifiedReturnInvoiceDocNoPrefix: 'SRI'
        },
        preferences: {
          salesWithOneLineNegativeAsReturns: false
        }
      }
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
      {
        terminal: {
          fullReturnInvoiceDocNoPrefix: 'FRI',
          simplifiedReturnInvoiceDocNoPrefix: 'SRI'
        },
        preferences: {
          salesWithOneLineNegativeAsReturns: true
        }
      }
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
      {
        terminal: {
          fullReturnInvoiceDocNoPrefix: 'FRI',
          simplifiedReturnInvoiceDocNoPrefix: 'SRI'
        },
        preferences: {
          salesWithOneLineNegativeAsReturns: false
        }
      }
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
      {
        terminal: {
          fullReturnInvoiceDocNoPrefix: 'FRI',
          simplifiedReturnInvoiceDocNoPrefix: 'SRI'
        },
        preferences: {
          salesWithOneLineNegativeAsReturns: true
        }
      }
    );
    expect(sequenceName).toEqual('simplifiedreturninvoiceslastassignednum');
  });

  it('should return custom sequence name if defined', () => {
    const ticket = deepfreeze({ obposSequencename: 'custom' });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getInvoiceSequenceName(
      ticket
    );
    expect(sequenceName).toEqual('custom');
  });
});
