/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */
OB = { App: { Class: {} } };
global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/cashup/Cashup');

describe('Cashup Model', () => {
  it('should have an null id in its initial state', () => {
    const initialState = OB.App.StateAPI.Cashup.initialState;

    expect(initialState.id).toBeNull();
  });
});
