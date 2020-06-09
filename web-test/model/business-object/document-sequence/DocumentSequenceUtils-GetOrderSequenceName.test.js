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

describe('Document Sequence Utils getOrderSequenceName method', () => {
  it('should generate order sequence name for empty order', () => {
    const ticket = deepfreeze({});
    const sequenceName = OB.App.State.DocumentSequence.Utils.getOrderSequenceName(
      ticket
    );
    expect(sequenceName).toEqual('lastassignednum');
  });

  it('should generate order sequence name for order', () => {
    const ticket = deepfreeze({ isQuotation: false, lines: [{ qty: 1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getOrderSequenceName(
      ticket,
      {
        terminal: {
          returnSequencePrefix: 'R',
          quotationSequencePrefix: 'Q'
        }
      }
    );
    expect(sequenceName).toEqual('lastassignednum');
  });

  it('should generate return sequence name for return', () => {
    const ticket = deepfreeze({ isQuotation: false, lines: [{ qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getOrderSequenceName(
      ticket,
      {
        terminal: {
          returnSequencePrefix: 'R',
          quotationSequencePrefix: 'Q'
        }
      }
    );
    expect(sequenceName).toEqual('returnslastassignednum');
  });

  it('should generate quotation sequence name for quotation', () => {
    const ticket = deepfreeze({ isQuotation: true, lines: [{ qty: 1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getOrderSequenceName(
      ticket,
      {
        terminal: {
          returnSequencePrefix: 'R',
          quotationSequencePrefix: 'Q'
        }
      }
    );
    expect(sequenceName).toEqual('quotationslastassignednum');
  });

  it('should generate order sequence name if return sequence is not defined', () => {
    const ticket = deepfreeze({ isQuotation: false, lines: [{ qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getOrderSequenceName(
      ticket,
      {
        terminal: {
          quotationSequencePrefix: 'Q'
        }
      }
    );
    expect(sequenceName).toEqual('lastassignednum');
  });

  it('should generate order sequence name if quotation sequence is not defined', () => {
    const ticket = deepfreeze({ isQuotation: true, lines: [{ qty: 1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getOrderSequenceName(
      ticket,
      {
        terminal: {
          returnSequencePrefix: 'R'
        }
      }
    );
    expect(sequenceName).toEqual('lastassignednum');
  });

  it('should generate order sequence name if one negative line and no salesWithOneLineNegativeAsReturns', () => {
    const ticket = deepfreeze({ lines: [{ qty: 1 }, { qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getOrderSequenceName(
      ticket,
      {
        terminal: {
          returnSequencePrefix: 'R',
          quotationSequencePrefix: 'Q'
        },
        preferences: {
          salesWithOneLineNegativeAsReturns: false
        }
      }
    );
    expect(sequenceName).toEqual('lastassignednum');
  });

  it('should generate return sequence name if one negative line and salesWithOneLineNegativeAsReturns', () => {
    const ticket = deepfreeze({ lines: [{ qty: 1 }, { qty: -1 }] });
    const sequenceName = OB.App.State.DocumentSequence.Utils.getOrderSequenceName(
      ticket,
      {
        terminal: {
          returnSequencePrefix: 'R',
          quotationSequencePrefix: 'Q'
        },
        preferences: {
          salesWithOneLineNegativeAsReturns: true
        }
      }
    );
    expect(sequenceName).toEqual('returnslastassignednum');
  });
});
