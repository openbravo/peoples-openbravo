/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  /*
  Add action preparation to add the ticket id to the payload,
  this is only for the pos,
  it will be executed later the action preparation of mobile.core
  and will use the ticketid to calculate the contex.
  The priority of these action preparation is set to -100 so it is
  executed before the default one that is 100, that is the one that
  have the mobile.core action preparation
  */
  async function addTicketIdToPayload(terminalLog, payload, options) {
    const newPayload = { ...payload };
    newPayload.ticketid = options.globalState.Ticket.id;
    return newPayload;
  }

  OB.App.StateAPI.TerminalLog.addKey.addActionPreparation(
    addTicketIdToPayload,
    undefined,
    -100
  );
  OB.App.StateAPI.TerminalLog.addButtonClick.addActionPreparation(
    addTicketIdToPayload,
    undefined,
    -100
  );
  OB.App.StateAPI.TerminalLog.addProcess.addActionPreparation(
    addTicketIdToPayload,
    undefined,
    -100
  );
  OB.App.StateAPI.TerminalLog.addLog.addActionPreparation(
    addTicketIdToPayload,
    undefined,
    -100
  );
})();
