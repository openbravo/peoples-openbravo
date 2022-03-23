/*
 ************************************************************************************
 * Copyright (C) 2021-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  /**
   * This action preparation verifies if is necessary to add more payment if the ticket is already paid
   * This check is only done in old POS
   */
  OB.App.StateAPI.Ticket.addPayment.addActionPreparation(
    async (ticket, payload) => {
      const newPayload = { ...payload };

      return OB.App.State.Ticket.Utils.checkExactPaid(ticket, newPayload);
    },
    async (ticket, payload) => payload,
    130
  );

  /**
   * This action preparation verifies if is necessary to show a popup to indicate what to do with a prepayment change
   * This check is only done in old POS
   */
  OB.App.StateAPI.Ticket.addPayment.addActionPreparation(
    async (ticket, payload) => {
      const newPayload = { ...payload };

      return OB.App.State.Ticket.Utils.managePrePaymentChange(
        ticket,
        newPayload
      );
    },
    async (ticket, payload) => payload,
    150
  );
})();
