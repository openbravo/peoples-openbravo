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

describe('Document Sequence Utils generateTicketDocumentSequence method', () => {
  it('should not generate new document number if ticket already has document number', () => {
    const ticket = deepfreeze({ documentNo: 'OS/00000' });
    const newTicketAndDocumentSequence = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
      ticket
    );
    expect(newTicketAndDocumentSequence).toEqual({ ticket: ticket });
  });

  it('should generate new document number if order has no document number', () => {
    const ticket = deepfreeze({});
    const documentSequence = deepfreeze({
      lastassignednum: { sequencePrefix: 'O', sequenceNumber: 0 }
    });
    const newTicketAndDocumentSequence = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
      ticket,
      documentSequence,
      null,
      null,
      null,
      null,
      '/',
      5,
      null
    );
    expect(newTicketAndDocumentSequence).toEqual({
      ticket: {
        obposSequencename: 'lastassignednum',
        obposSequencenumber: 1,
        documentNo: 'O/00001'
      },
      documentSequence: {
        lastassignednum: { sequencePrefix: 'O', sequenceNumber: 1 }
      }
    });
  });

  it('should generate new document number if invoice has no document number', () => {
    const ticket = deepfreeze({ isInvoice: true });
    const documentSequence = deepfreeze({
      simplifiedinvoiceslastassignednum: {
        sequencePrefix: 'I',
        sequenceNumber: 0
      }
    });
    const newTicketAndDocumentSequence = OB.App.State.DocumentSequence.Utils.generateTicketDocumentSequence(
      ticket,
      documentSequence,
      null,
      null,
      null,
      null,
      '/',
      5,
      null
    );
    expect(newTicketAndDocumentSequence).toEqual({
      ticket: {
        isInvoice: true,
        obposSequencename: 'simplifiedinvoiceslastassignednum',
        obposSequencenumber: 1,
        documentNo: 'I/00001'
      },
      documentSequence: {
        simplifiedinvoiceslastassignednum: {
          sequencePrefix: 'I',
          sequenceNumber: 1
        }
      }
    });
  });
});
