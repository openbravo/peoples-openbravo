/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupDocumentSequence');
require('./SetupDocumentSequenceUtils');

describe('Document Sequence Utils calculateDocumentNumber function', () => {
  test.each`
    documentNo         | result
    ${'VBS100001'}     | ${'VBS100001-1'}
    ${'VBS100001-1'}   | ${'VBS100001-2'}
    ${'VBS1/00001'}    | ${'VBS1/00001-1'}
    ${'VBS1/00001-1'}  | ${'VBS1/00001-2'}
    ${'VBS1-00001'}    | ${'VBS1-00001-1'}
    ${'VBS1-00001-1'}  | ${'VBS1-00001-2'}
    ${'VBS/100001'}    | ${'VBS/100001-1'}
    ${'VBS/100001-1'}  | ${'VBS/100001-2'}
    ${'VBS/1/00001'}   | ${'VBS/1/00001-1'}
    ${'VBS/1/00001-1'} | ${'VBS/1/00001-2'}
    ${'VBS/1-00001'}   | ${'VBS/1-00001-1'}
    ${'VBS/1-00001-1'} | ${'VBS/1-00001-2'}
    ${'VBS-100001'}    | ${'VBS-100001-1'}
    ${'VBS-100001-1'}  | ${'VBS-100001-2'}
    ${'VBS-1/00001'}   | ${'VBS-1/00001-1'}
    ${'VBS-1/00001-1'} | ${'VBS-1/00001-2'}
    ${'VBS-1-00001'}   | ${'VBS-1-00001-1'}
    ${'VBS-1-00001-1'} | ${'VBS-1-00001-2'}
  `('Calculates replace document number', ({ documentNo, result }) => {
    const replaceDocumentNo = OB.App.State.DocumentSequence.Utils.calculateReplaceDocumentNumber(
      documentNo,
      { replaceNumberSeparator: '-', documentNumberPadding: 5 }
    );
    expect(replaceDocumentNo).toEqual(result);
  });

  test.each`
    documentNo        | replaceNumberSeparator | result
    ${'VBS1/00001'}   | ${undefined}           | ${'VBS1/00001-1'}
    ${'VBS1/00001-1'} | ${undefined}           | ${'VBS1/00001-2'}
    ${'VBS1/00001'}   | ${'-'}                 | ${'VBS1/00001-1'}
    ${'VBS1/00001-1'} | ${'-'}                 | ${'VBS1/00001-2'}
    ${'VBS1/00001'}   | ${'/'}                 | ${'VBS1/00001/1'}
    ${'VBS1/00001/1'} | ${'/'}                 | ${'VBS1/00001/2'}
  `(
    'Allows to define different replaceNumberSeparator',
    ({ documentNo, replaceNumberSeparator, result }) => {
      const replaceDocumentNo = OB.App.State.DocumentSequence.Utils.calculateReplaceDocumentNumber(
        documentNo,
        { replaceNumberSeparator, documentNumberPadding: 5 }
      );
      expect(replaceDocumentNo).toEqual(result);
    }
  );

  test.each`
    documentNo         | documentNumberPadding | result
    ${'VBS1/0001'}     | ${4}                  | ${'VBS1/0001-1'}
    ${'VBS1/0001-1'}   | ${4}                  | ${'VBS1/0001-2'}
    ${'VBS1/00001'}    | ${5}                  | ${'VBS1/00001-1'}
    ${'VBS1/00001-1'}  | ${5}                  | ${'VBS1/00001-2'}
    ${'VBS1/000001'}   | ${6}                  | ${'VBS1/000001-1'}
    ${'VBS1/000001-1'} | ${6}                  | ${'VBS1/000001-2'}
  `(
    'Allows to define different documentNumberPadding',
    ({ documentNo, documentNumberPadding, result }) => {
      const replaceDocumentNo = OB.App.State.DocumentSequence.Utils.calculateReplaceDocumentNumber(
        documentNo,
        { replaceNumberSeparator: '-', documentNumberPadding }
      );
      expect(replaceDocumentNo).toEqual(result);
    }
  );
});
