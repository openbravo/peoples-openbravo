/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global global */
global.OB = {
  App: {
    Class: {},
    UUID: { generate: jest.fn() }
  }
};

global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
require('../../../../../org.openbravo.client.kernel/web/org.openbravo.client.kernel/js/BigDecimal-all-1.0.3');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/source/utils/ob-arithmetic');

require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
OB.App.StateAPI.registerModel('Ticket');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');

// set Ticket model utility functions
OB.App.State = { Ticket: { Utils: {} } };
OB.App.StateAPI.Ticket.utilities.forEach(
  util => (OB.App.State.Ticket.Utils[util.functionName] = util.implementation)
);
