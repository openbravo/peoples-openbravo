/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.UTIL = window.OB.UTIL || {};
  OB.UTIL.TerminalLog = window.OB.UTIL.TerminalLog || {};

  /**
   * Get the context of the log line
   *
   * The context is calculated from the local storage variable terminalLogContext,
   * that contains the window in which we are, or the documentno in case a ticket is open
   * also is added the content of local storage variable terminalLogContextPopUp, that
   * contains the opened popups.
   *
   * @param {string} state - The current state
   * @param {string} contextFromId - Optional, an array containing the id of a web element splited by '_'
   * @returns {string} The context for the log line
   */
  OB.UTIL.TerminalLog.calculateContext = function(payload) {
    const { state, orderid, contextFromId } = payload;
    let context = [];
    let terminalLogContextPopUp;
    let contextString;
    let currentContext;
    if (OB.UTIL.isNullOrUndefined(orderid)) {
      currentContext = state.context;
    } else if (state.context === 'retail.pointofsale') {
      currentContext = orderid;
    } else {
      currentContext = state.context;
    }
    const currentContextPopup = state.contextPopup;
    context.push(currentContext);
    if (contextFromId && currentContextPopup.length === 0) {
      context.push(contextFromId);
    } else {
      terminalLogContextPopUp = currentContextPopup;
      if (terminalLogContextPopUp && terminalLogContextPopUp.length > 0) {
        context = context.concat(terminalLogContextPopUp);
      }
    }
    contextString = context.join('  |  ');
    if (contextString.length > 250) {
      contextString = `${contextString.substring(1, 245)}[...]`;
    }
    return contextString;
  };

  OB.App.StateAPI.TerminalLog.addKey.addActionPreparation(
    async (terminalLog, payload, options) => {
      const newPayload = { ...payload };
      newPayload.context = OB.UTIL.TerminalLog.calculateContext({
        state: terminalLog,
        orderid: options.globalState.Ticket.id,
        contextFromId: payload.contextFromId
      });
      return newPayload;
    }
  );

  OB.App.StateAPI.TerminalLog.addButtonClick.addActionPreparation(
    async (terminalLog, payload, options) => {
      const newPayload = { ...payload };
      newPayload.context = OB.UTIL.TerminalLog.calculateContext({
        state: terminalLog,
        orderid: options.globalState.Ticket.id,
        contextFromId: payload.contextFromId
      });
      return newPayload;
    }
  );

  OB.App.StateAPI.TerminalLog.addProcess.addActionPreparation(
    async (terminalLog, payload, options) => {
      const newPayload = { ...payload };
      newPayload.context = OB.UTIL.TerminalLog.calculateContext({
        state: terminalLog,
        orderid: options.globalState.Ticket.id
      });
      return newPayload;
    }
  );

  OB.App.StateAPI.TerminalLog.addLog.addActionPreparation(
    async (terminalLog, payload, options) => {
      const newPayload = { ...payload };
      newPayload.context = OB.UTIL.TerminalLog.calculateContext({
        state: terminalLog,
        orderid: options.globalState.Ticket.id
      });
      return newPayload;
    }
  );
})();
