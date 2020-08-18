/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.UTIL = window.OB.UTIL || {};
  OB.UTIL.TicketUtils = OB.UTIL.TicketUtils || {};

  OB.UTIL.TicketUtils.loadAndSyncTicketFromState = function() {
    if (OB.App.StateBackwardCompatibility) {
      const tmpTicket = new OB.Model.Order();
      const compatibleModel = OB.App.StateBackwardCompatibility.bind(
        OB.App.State.Ticket,
        tmpTicket
      );

      const stateTicket = OB.App.State.getState().Ticket;
      if (Object.keys(stateTicket).length !== 0) {
        compatibleModel.handleStateChange(stateTicket);
      }

      OB.MobileApp.model.receipt.clearWith(tmpTicket);

      OB.App.StateBackwardCompatibility.bind(
        OB.App.State.Ticket,
        OB.MobileApp.model.receipt
      );
    }
  };

  OB.UTIL.TicketUtils.addTicketCreationDataToPayload = function(payload) {
    const newPayload = { ...payload };
    newPayload.businessPartner = JSON.parse(
      JSON.stringify(OB.App.TerminalProperty.get('businessPartner'))
    );
    newPayload.terminal = OB.App.TerminalProperty.get('terminal');
    newPayload.session = OB.App.TerminalProperty.get('session');
    newPayload.orgUserId = OB.App.TerminalProperty.get('orgUserId');
    newPayload.pricelist = OB.App.TerminalProperty.get('pricelist');
    newPayload.contextUser = OB.App.TerminalProperty.get('context').user;

    return newPayload;
  };
})();
