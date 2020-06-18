/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.UTIL = window.OB.UTIL || {};
  OB.UTIL.TicketListUtils = OB.UTIL.TicketListUtils || {};

  OB.UTIL.TicketListUtils.loadTicket = async function(ticketModel) {
    await OB.App.State.Global.loadTicket({
      ticket: JSON.parse(JSON.stringify(ticketModel.toJSON()))
    });
    OB.MobileApp.model.receipt.trigger('updateView');
    OB.MobileApp.model.receipt.trigger('paintTaxes');
    OB.MobileApp.model.receipt.trigger('updatePending');
  };

  OB.UTIL.TicketListUtils.loadTicketById = async function(ticketId) {
    await OB.App.State.Global.loadTicketById({
      id: ticketId
    });
    OB.MobileApp.model.receipt.trigger('updateView');
    OB.MobileApp.model.receipt.trigger('paintTaxes');
    OB.MobileApp.model.receipt.trigger('updatePending');
  };
})();
