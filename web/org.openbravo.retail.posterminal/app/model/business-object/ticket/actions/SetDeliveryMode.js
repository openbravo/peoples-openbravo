/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the action to set the delivery mode to the ticket
 */

(function SetDeliveryModeDefinition() {
  OB.App.StateAPI.Ticket.registerAction(
    'setDeliveryMode',
    (ticket, payload) => {
      const newTicket = { ...ticket };

      newTicket.obrdmDeliveryModeProperty = payload.obrdmDeliveryModeProperty;
      newTicket.obrdmDeliveryDateProperty = payload.obrdmDeliveryDateProperty;
      newTicket.obrdmDeliveryTimeProperty = payload.obrdmDeliveryTimeProperty;

      newTicket.lines = newTicket.lines.map(line => {
        let newLine = { ...line };

        newLine.obrdmDeliveryMode = payload.obrdmDeliveryModeProperty;
        newLine.obrdmDeliveryDate = payload.obrdmDeliveryDateProperty;
        newLine.obrdmDeliveryTime = payload.obrdmDeliveryTimeProperty;

        newLine = OB.App.State.Ticket.Utils.setDeliveryMode(newTicket, newLine)
          .line;

        return newLine;
      });

      return newTicket;
    }
  );
})();
