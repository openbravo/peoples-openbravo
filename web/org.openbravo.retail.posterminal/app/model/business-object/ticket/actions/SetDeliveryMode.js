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
      let newTicket = { ...ticket };
      if (payload.obrdmDeliveryModeProperty) {
        newTicket.obrdmDeliveryModeProperty = payload.obrdmDeliveryModeProperty;
      }
      if (payload.obrdmDeliveryDateProperty) {
        newTicket.obrdmDeliveryDateProperty = payload.obrdmDeliveryDateProperty;
      }
      if (payload.obrdmDeliveryTimeProperty) {
        newTicket.obrdmDeliveryTimeProperty = payload.obrdmDeliveryTimeProperty;
      }

      newTicket.lines.forEach(line => {
        const newLine = { ...line };

        if (payload.obrdmDeliveryModeProperty) {
          newLine.obrdmDeliveryMode = payload.obrdmDeliveryModeProperty;
        }
        if (payload.obrdmDeliveryDateProperty) {
          newLine.obrdmDeliveryDate = payload.obrdmDeliveryDateProperty;
        }
        if (payload.obrdmDeliveryTimeProperty) {
          newLine.obrdmDeliveryTime = payload.obrdmDeliveryTimeProperty;
        }
        newTicket = OB.App.State.Ticket.Utils.setDeliveryMode(
          newLine,
          newTicket
        );
        newTicket.line = newLine;
      });
      return newTicket;
    }
  );
})();
