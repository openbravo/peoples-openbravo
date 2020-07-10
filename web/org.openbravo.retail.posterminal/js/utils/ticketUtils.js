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
})();
