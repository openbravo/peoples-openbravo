/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  /**
   * This action preparation verifies if there are more payments than needed to pay the ticket
   * This check is only done in old POS
   */
  OB.App.StateAPI.Global.completeTicket.addActionPreparation(
    async (globalState, payload) => {
      const newPayload = { ...payload };

      return OB.App.State.Ticket.Utils.checkExtraPayments(
        globalState.Ticket,
        newPayload
      );
    },
    async (globalState, payload) => payload,
    120
  );
})();
