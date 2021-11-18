/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.App.StateAPI.Global.completeTicket.addActionPreparation(
    async (globalState, payload) => {
      const newPayload = { ...payload };

      return OB.App.State.Ticket.Utils.checkExtraPayments(
        globalState.Ticket,
        newPayload
      );
    },
    async (state, payload) => payload,
    120
  );
})();
