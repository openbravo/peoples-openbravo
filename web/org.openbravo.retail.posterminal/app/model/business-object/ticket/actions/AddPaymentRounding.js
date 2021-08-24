/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action to add a payment rounding to the ticket
 * @author Miguel de Juana <miguel.dejuana@openbravo.com>
 */

(function AddPaymenRoundingtDefinition() {
  OB.App.StateAPI.Ticket.registerAction(
    'addPaymentRounding',
    (ticket, payload) => {
      let newTicket = { ...ticket };

      ({ ticket: newTicket } = OB.App.State.Ticket.Utils.addPaymentRounding(
        newTicket,
        payload
      ));

      return newTicket;
    }
  );
})();
