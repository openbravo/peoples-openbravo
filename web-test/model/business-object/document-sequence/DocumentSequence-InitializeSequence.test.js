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
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/document-sequence/DocumentSequence');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/document-sequence/actions/InitializeSequence');

describe('Document Sequence Initialize Sequence action', () => {
  it('should keep same state if sequences array is empty', () => {
    const currentState = OB.App.StateAPI.DocumentSequence.initialState;
    const sequences = [];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {};

    expect(newState).toEqual(expectedState);
  });
});
