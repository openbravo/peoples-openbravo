/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/* global global */

OB = {
  App: {
    Class: {}
  },
  Discounts: {
    Pos: {
      ruleImpls: [],
      bpSets: []
    }
  },
  Taxes: {
    Pos: {
      ruleImpls: []
    }
  }
};

global.lodash = require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/lib/vendor/lodash-4.17.15');
require('../../../../../org.openbravo.mobile.core/web/org.openbravo.mobile.core/app/model/application-state/StateAPI');
OB.App.StateAPI.registerModel('Ticket');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/document-sequence/DocumentSequence');
require('../../../../web/org.openbravo.retail.posterminal/app/model/business-object/document-sequence/DocumentSequenceUtils');

// set Ticket model "applyDiscountsAndTaxes" utility function
OB.App.State = {};
OB.App.State.Ticket = {};
OB.App.State.Ticket.Utils = {};
OB.App.State.DocumentSequence = {};
OB.App.State.DocumentSequence.Utils = {};

OB.App.StateAPI.Ticket.utilities.forEach(util => {
  OB.App.State.Ticket.Utils[util.functionName] = util.implementation;
});

OB.App.StateAPI.DocumentSequence.utilities.forEach(util => {
  OB.App.State.DocumentSequence.Utils[util.functionName] = util.implementation;
});
