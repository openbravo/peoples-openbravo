/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function AssignExternalBusinessPartner() {
  OB.App.StateAPI.Ticket.registerAction(
    'assignExternalBusinessPartner',
    (ticket, payload) => {
      const newTicket = { ...ticket };
      const { businessPartner } = payload;
      newTicket.externalBusinessPartnerReference = businessPartner
        ? businessPartner.getKey()
        : null;
      newTicket.externalBusinessPartnerCategory = new OB.App.Class.ExternalBusinessPartner(
        businessPartner ? businessPartner.getPlainObject() : null
      ).getCategoryKey();
      newTicket.externalBusinessPartner = businessPartner
        ? businessPartner.getPlainObject()
        : null;
      if (
        payload.addressConfig &&
        newTicket.obrdmDeliveryModeProperty === 'HomeDelivery'
      ) {
        newTicket.alternateAddress = OB.App.ExternalBusinessPartnerAPI.getAlternateDeliveryLocation(
          businessPartner,
          payload.addressConfig
        );
      }
      return newTicket;
    }
  );
})();
