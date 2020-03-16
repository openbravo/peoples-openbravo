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
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 }
    });
    const sequences = [];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if empty state and one sequence in payload', () => {
    const currentState = deepfreeze({});
    const sequences = [
      {
        sequenceName: 'orderSequence',
        sequencePrefix: 'OS',
        sequenceNumber: 0
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and different sequence in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 }
    });
    const sequences = [
      {
        sequenceName: 'returnSequence',
        sequencePrefix: 'RS',
        sequenceNumber: 0
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 0 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and higher sequence in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 0 }
    });
    const sequences = [
      {
        sequenceName: 'orderSequence',
        sequencePrefix: 'OS',
        sequenceNumber: 1
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 0 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and lower sequence in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 1 }
    });
    const sequences = [
      {
        sequenceName: 'orderSequence',
        sequencePrefix: 'OS',
        sequenceNumber: 0
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 1 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and very higher sequence in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 1 }
    });
    const sequences = [
      {
        sequenceName: 'orderSequence',
        sequencePrefix: 'OS',
        sequenceNumber: 1000
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 1 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if one sequence in state and very lower sequence in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 1000 }
    });
    const sequences = [
      {
        sequenceName: 'orderSequence',
        sequencePrefix: 'OS',
        sequenceNumber: 1
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 1000 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if many sequences in state and different sequences in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 0 }
    });
    const sequences = [
      {
        sequenceName: 'orderSequence',
        sequencePrefix: 'OS',
        sequenceNumber: 0
      },
      {
        sequenceName: 'returnSequence',
        sequencePrefix: 'RS',
        sequenceNumber: 1000
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 1000 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if no sequence in state and undefined sequence in payload', () => {
    const currentState = deepfreeze({});
    const sequences = [{ sequenceName: 'orderSequence' }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequenceNumber: 0 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should keep same state if one sequence in state and undefined sequence number in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 }
    });
    const sequences = [{ sequenceName: 'orderSequence', sequencePrefix: 'OS' }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should reset sequence if one sequence in state and undefined sequence prefix in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 }
    });
    const sequences = [{ sequenceName: 'orderSequence', sequenceNumber: 0 }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequenceNumber: 0 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should reset sequence if one sequence in state and undefined sequence prefix and number in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 }
    });
    const sequences = [{ sequenceName: 'orderSequence' }];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequenceNumber: 0 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should initialize sequence if no sequence in state and null sequence in payload', () => {
    const currentState = deepfreeze({});
    const sequences = [
      {
        sequenceName: 'orderSequence',
        sequencePrefix: null,
        sequenceNumber: null
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: null, sequenceNumber: 0 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should keep same state if one sequence in state and null sequence number in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 }
    });
    const sequences = [
      {
        sequenceName: 'orderSequence',
        sequencePrefix: 'OS',
        sequenceNumber: null
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should reset sequence if one sequence in state and null sequence prefix in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 }
    });
    const sequences = [
      {
        sequenceName: 'orderSequence',
        sequencePrefix: null,
        sequenceNumber: 0
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: null, sequenceNumber: 0 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should reset sequence if one sequence in state and null sequence prefix and number in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 }
    });
    const sequences = [
      {
        sequenceName: 'orderSequence',
        sequencePrefix: null,
        sequenceNumber: null
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: null, sequenceNumber: 0 }
    };

    expect(newState).toEqual(expectedState);
  });

  it('should reset sequence if one sequence in state and different sequence prefix in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 }
    });
    const sequences = [
      {
        sequenceName: 'orderSequence',
        sequencePrefix: 'OS2',
        sequenceNumber: 0
      }
    ];

    const newState = OB.App.StateAPI.DocumentSequence.initializeSequence(
      currentState,
      { sequences: sequences }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS2', sequenceNumber: 0 }
    };

    expect(newState).toEqual(expectedState);
  });
});
