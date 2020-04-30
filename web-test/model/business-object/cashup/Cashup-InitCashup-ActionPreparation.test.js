/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('./SetupCashup');
const {
  executeActionPreparations
} = require('../../../../../org.openbravo.mobile.core/web-test/base/state-utils');
const deepfreeze = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/deepfreeze-2.0.0');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/initCashup');
require('./SetupCashupUtils');

describe('init cashup Action Preparation', () => {
  it('initialize from backend', async () => {
    const currentState = {
      Cashup: {
        id: 'D208D4D868EC1E5C9316006606A90911'
      }
    };

    const expectedPayload = {
      initCashupFrom: 'local'
    };

    deepfreeze(currentState);
    const result = await executeActionPreparations(
      OB.App.StateAPI.Cashup.initCashup,
      currentState,
      {}
    );

    expect(result).toEqual(expectedPayload);
  });
});
