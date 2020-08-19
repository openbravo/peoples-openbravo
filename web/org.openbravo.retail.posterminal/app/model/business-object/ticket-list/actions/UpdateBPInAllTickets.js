/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function updateBpInAllTickets() {
  const updateBpInTicket = (ticket, newBusinessPartner) => {
    const newTicket = { ...ticket };
    if (ticket.businessPartner.id === newBusinessPartner.id) {
      const clonedBP = { ...newBusinessPartner };
      const bp = ticket.businessPartner;
      if (newTicket.businessPartner.locId !== newBusinessPartner.locId) {
        // if the order has a different address but same BP than the bp
        // then copy over the address data
        clonedBP.locId = bp.locId;
        clonedBP.locName = bp.locName;
        clonedBP.postalCode = bp.postalCode;
        clonedBP.cityName = bp.cityName;
        clonedBP.countryName = bp.countryName;
        clonedBP.locationModel = bp.locationModel;
      }
      if (
        newTicket.businessPartner.shipLocId !== newBusinessPartner.shipLocId
      ) {
        clonedBP.shipLocId = bp.shipLocId;
        clonedBP.shipLocName = bp.shipLocName;
        clonedBP.shipPostalCode = bp.shipPostalCode;
        clonedBP.shipCityName = bp.shipCityName;
        clonedBP.shipCountryName = bp.shipCountryName;
      }
      newTicket.businessPartner = clonedBP;
    }

    return newTicket;
  };

  OB.App.StateAPI.Global.registerAction(
    'updateBpInAllTickets',
    (state, payload) => {
      const newState = { ...state };
      const { customer } = payload;

      if (newState.Ticket.isEditable) {
        newState.Ticket = updateBpInTicket(newState.Ticket, customer);
      }
      newState.TicketList = newState.TicketList.map(ticket =>
        ticket.isEditable ? updateBpInTicket(ticket, customer) : ticket
      );

      return newState;
    }
  );
})();
