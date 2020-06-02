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
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/document-sequence/actions/DecreaseSequence');
require('./SetupDocumentSequenceUtils');

describe('Document Sequence Increment Sequence action', () => {
  it('should keep same state if empty state and null sequenceName in payload', () => {
    const currentState = deepfreeze({});
    const sequenceName = null;
    const newState = OB.App.StateAPI.DocumentSequence.decreaseSequence(
      currentState,
      { sequenceName: sequenceName }
    );
    const expectedState = {};
    expect(newState).toEqual(expectedState);
  });

  it('should keep same state if empty state and undefined sequenceName in payload', () => {
    const currentState = deepfreeze({});
    const sequenceName = undefined;
    const newState = OB.App.StateAPI.DocumentSequence.decreaseSequence(
      currentState,
      { sequenceName: sequenceName }
    );
    const expectedState = {};
    expect(newState).toEqual(expectedState);
  });

  it('should keep same state if one sequence in state and null sequenceName in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 }
    });
    const sequenceName = null;
    const newState = OB.App.StateAPI.DocumentSequence.decreaseSequence(
      currentState,
      { sequenceName: sequenceName }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 }
    };
    expect(newState).toEqual(expectedState);
  });

  it('should keep same state if one sequence in state and undefined sequenceName in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 }
    });
    const sequenceName = undefined;
    const newState = OB.App.StateAPI.DocumentSequence.decreaseSequence(
      currentState,
      { sequenceName: sequenceName }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 }
    };
    expect(newState).toEqual(expectedState);
  });

  it('should keep same state if empty state and one sequenceName in payload', () => {
    const currentState = deepfreeze({});
    const sequenceName = 'orderSequence';
    const newState = OB.App.StateAPI.DocumentSequence.decreaseSequence(
      currentState,
      { sequenceName: sequenceName }
    );
    const expectedState = {};
    expect(newState).toEqual(expectedState);
  });

  it('should keep same state if one sequence in state and different sequence in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 }
    });
    const sequenceName = 'returnSequence';
    const newState = OB.App.StateAPI.DocumentSequence.decreaseSequence(
      currentState,
      { sequenceName: sequenceName }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 }
    };
    expect(newState).toEqual(expectedState);
  });

  it('should decrease sequence if one sequence in state with a low value and same sequence in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 1 }
    });
    const sequenceName = 'orderSequence';
    const newState = OB.App.StateAPI.DocumentSequence.decreaseSequence(
      currentState,
      { sequenceName: sequenceName }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 1 }
    };
    expect(newState).toEqual(expectedState);
  });

  it('should decrease sequence if one sequence in state with a high value and same sequence in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 1000 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 1000 }
    });
    const sequenceName = 'orderSequence';
    const newState = OB.App.StateAPI.DocumentSequence.decreaseSequence(
      currentState,
      { sequenceName: sequenceName }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 999 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 1000 }
    };
    expect(newState).toEqual(expectedState);
  });

  it('should decrease sequence if one sequence in state with a zero value and same sequence in payload', () => {
    const currentState = deepfreeze({
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 0 }
    });
    const sequenceName = 'orderSequence';
    const newState = OB.App.StateAPI.DocumentSequence.decreaseSequence(
      currentState,
      { sequenceName: sequenceName }
    );
    const expectedState = {
      orderSequence: { sequencePrefix: 'OS', sequenceNumber: 0 },
      returnSequence: { sequencePrefix: 'RS', sequenceNumber: 0 }
    };
    expect(newState).toEqual(expectedState);
  });
});
