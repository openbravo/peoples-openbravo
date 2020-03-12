/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupDocumentSequence');
const deepfreeze = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/deepfreeze-2.0.0');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/document-sequence/actions/InitializeSequence');
require('./SetupDocumentSequenceUtils');

describe('Document Sequence Initialize Sequence action', () => {
  it('should keep same state if empty state and empty sequences in payload', () => {
    const currentState = deepfreeze({});
    const sequences = [];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {};

    expect(newState).toEqual(expectedState);
  });

  it('should keep same state if one sequence in state and empty sequences in payload', () => {
    const currentState = deepfreeze({ orderSequence: 0 });
    const sequences = [];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = { orderSequence: 0 };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if empty state and one sequence in payload', () => {
    const currentState = deepfreeze({});
    const sequences = [{ sequenceName: 'orderSequence', sequenceNumber: 0 }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = { orderSequence: 0 };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and different sequence in payload', () => {
    const currentState = deepfreeze({ orderSequence: 0 });
    const sequences = [{ sequenceName: 'returnSequence', sequenceNumber: 0 }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = { orderSequence: 0, returnSequence: 0 };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and higher sequence in payload', () => {
    const currentState = deepfreeze({ orderSequence: 0 });
    const sequences = [{ sequenceName: 'orderSequence', sequenceNumber: 1 }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = { orderSequence: 1 };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and lower sequence in payload', () => {
    const currentState = deepfreeze({ orderSequence: 1 });
    const sequences = [{ sequenceName: 'orderSequence', sequenceNumber: 0 }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = { orderSequence: 1 };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and very higher sequence in payload', () => {
    const currentState = deepfreeze({ orderSequence: 1 });
    const sequences = [{ sequenceName: 'orderSequence', sequenceNumber: 10 }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = { orderSequence: 10 };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and very lower sequence in payload', () => {
    const currentState = deepfreeze({ orderSequence: 10 });
    const sequences = [{ sequenceName: 'orderSequence', sequenceNumber: 1 }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = { orderSequence: 10 };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if no sequence in state and undefined sequence in payload', () => {
    const currentState = deepfreeze({});
    const sequences = [{ sequenceName: 'orderSequence' }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = { orderSequence: 0 };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and undefined sequence in payload', () => {
    const currentState = deepfreeze({ orderSequence: 10 });
    const sequences = [{ sequenceName: 'orderSequence' }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = { orderSequence: 10 };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if no sequence in state and null sequence in payload', () => {
    const currentState = deepfreeze({});
    const sequences = [{ sequenceName: 'orderSequence', sequenceNumber: null }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = { orderSequence: 0 };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and null sequence in payload', () => {
    const currentState = deepfreeze({ orderSequence: 10 });
    const sequences = [{ sequenceName: 'orderSequence', sequenceNumber: null }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = { orderSequence: 10 };

    expect(newState).toEqual(expectedState);
  });
});
