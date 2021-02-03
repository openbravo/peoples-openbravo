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
  Add action preparation to add the context of pos application to the payload,
  it will be executed later the action preparation of mobile.core
  and will use if specified the appContext to calculate the context.
  The priority of these action preparation is set to -100 so it is
  executed before the default one that is 100, that is the one that
  have the mobile.core action preparation
  */
  async function addTicketIdToPayload(terminalLog, payload, options) {
    const newPayload = { ...payload };
    if (
      terminalLog.context === 'retail.pointofsale' &&
      options.globalState.Ticket.id
    ) {
      newPayload.appContext = options.globalState.Ticket.id;
    }
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
