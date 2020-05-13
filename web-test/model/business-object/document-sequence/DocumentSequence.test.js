/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupDocumentSequence');

describe('Document Sequence Model', () => {
  it('should have an empty object in its initial state', () => {
    const initialState = OB.App.StateAPI.DocumentSequence.initialState;
    const expectedState = {};
    expect(initialState).toEqual(expectedState);
  });
});
