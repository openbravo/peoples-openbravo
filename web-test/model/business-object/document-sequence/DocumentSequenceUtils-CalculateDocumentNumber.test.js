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

describe('Document Sequence Utils calculateDocumentNumber method', () => {
  it('should generate correct documentNo for 0 sequenceNumber', () => {
    const documentNumber = OB.App.State.DocumentSequence.Utils.calculateDocumentNumber(
      {
        sequencePrefix: 'PREFIX',
        documentNumberSeparator: '/',
        documentNumberPadding: 5,
        sequenceNumber: 0
      }
    );
    expect(documentNumber).toEqual('PREFIX/00000');
  });

  it('should generate correct documentNo for 1 sequenceNumber', () => {
    const documentNumber = OB.App.State.DocumentSequence.Utils.calculateDocumentNumber(
      {
        sequencePrefix: 'PREFIX',
        documentNumberSeparator: '/',
        documentNumberPadding: 5,
        sequenceNumber: 1
      }
    );
    expect(documentNumber).toEqual('PREFIX/00001');
  });

  it('should generate correct documentNo for 10 sequenceNumber', () => {
    const documentNumber = OB.App.State.DocumentSequence.Utils.calculateDocumentNumber(
      {
        sequencePrefix: 'PREFIX',
        documentNumberSeparator: '/',
        documentNumberPadding: 5,
        sequenceNumber: 10
      }
    );
    expect(documentNumber).toEqual('PREFIX/00010');
  });

  it('should generate correct documentNo for 100 sequenceNumber', () => {
    const documentNumber = OB.App.State.DocumentSequence.Utils.calculateDocumentNumber(
      {
        sequencePrefix: 'PREFIX',
        documentNumberSeparator: '/',
        documentNumberPadding: 5,
        sequenceNumber: 100
      }
    );
    expect(documentNumber).toEqual('PREFIX/00100');
  });

  it('should generate correct documentNo for 1000 sequenceNumber', () => {
    const documentNumber = OB.App.State.DocumentSequence.Utils.calculateDocumentNumber(
      {
        sequencePrefix: 'PREFIX',
        documentNumberSeparator: '/',
        documentNumberPadding: 5,
        sequenceNumber: 1000
      }
    );
    expect(documentNumber).toEqual('PREFIX/01000');
  });

  it('should generate correct documentNo for 10000 sequenceNumber', () => {
    const documentNumber = OB.App.State.DocumentSequence.Utils.calculateDocumentNumber(
      {
        sequencePrefix: 'PREFIX',
        documentNumberSeparator: '/',
        documentNumberPadding: 5,
        sequenceNumber: 10000
      }
    );
    expect(documentNumber).toEqual('PREFIX/10000');
  });

  it('should generate correct documentNo for 100000 sequenceNumber', () => {
    const documentNumber = OB.App.State.DocumentSequence.Utils.calculateDocumentNumber(
      {
        sequencePrefix: 'PREFIX',
        documentNumberSeparator: '/',
        documentNumberPadding: 5,
        sequenceNumber: 100000
      }
    );
    expect(documentNumber).toEqual('PREFIX/100000');
  });

  it('should generate correct documentNo when not using separator', () => {
    const documentNumber = OB.App.State.DocumentSequence.Utils.calculateDocumentNumber(
      {
        sequencePrefix: 'PREFIX',
        documentNumberSeparator: '',
        documentNumberPadding: 5,
        sequenceNumber: 10000
      }
    );
    expect(documentNumber).toEqual('PREFIX10000');
  });
});
