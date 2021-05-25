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
        newTicket = OB.App.State.Ticket.Utils.setDeliveryMode(line, newTicket);
      });
      return newTicket;
    }
  );

  OB.App.StateAPI.Ticket.setDeliveryMode.addActionPreparation(
    async (ticket, payload) => {
      let newPayload = { ...payload };

      newPayload = hasErrorLines(newPayload, ticket);

      return newPayload;
    },
    async (ticket, payload) => payload,
    10
  );

  function hasErrorLines(newPayload, ticket) {
    if (ticket.isQuotation !== true) {
      ticket.lines.forEach(line => {
        if (
          (!line.obrdmDeliveryMode ||
            line.obrdmDeliveryMode === 'PickAndCarry') &&
          !line.obposCanbedelivered &&
          line.deliveredQuantity !== line.qty
        ) {
          throw new OB.App.Class.ActionCanceled({
            warningMsg: 'OBRDM_PickAndCarryError'
          });
        }
      });
    }
    return newPayload;
  }
})();
