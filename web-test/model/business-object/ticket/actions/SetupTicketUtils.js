/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

require('../../../../../web/org.openbravo.retail.posterminal/app/model/business-object/ticket/TicketUtils');

OB.App.State = {
  Ticket: {
    Utils: {}
  }
};

OB.App.StateAPI.Ticket.utilities.forEach(util => {
  OB.App.State.Ticket.Utils[util.functionName] = util.implementation;
});
