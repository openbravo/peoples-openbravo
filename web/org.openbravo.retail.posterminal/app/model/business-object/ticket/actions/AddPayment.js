/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action to add a payment to the ticket
 * @author Miguel de Juana <miguel.dejuana@openbravo.com>
 */

(function AddPaymentDefinition() {
  OB.App.StateAPI.Ticket.registerAction('addPayment', (ticket, payload) => {
    const newTicket = { ...ticket };
    return OB.App.State.Ticket.Utils.addPayment(newTicket, payload);
  });
})();
