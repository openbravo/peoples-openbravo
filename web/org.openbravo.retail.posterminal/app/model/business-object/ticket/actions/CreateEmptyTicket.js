/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines an action that creates an empty ticket using the utility function
 */
OB.App.StateAPI.Ticket.registerAction(
  'createEmptyTicket',
  (ticket, payload) => {
    return OB.App.State.Ticket.Utils.newTicket(
      payload.businessPartner,
      payload.terminal,
      payload.session,
      payload.orgUserId,
      payload.pricelist,
      payload.contextUser
    );
  }
);

OB.App.StateAPI.Ticket.createEmptyTicket.addActionPreparation(
  async (ticket, payload) => {
    const newPayload = { ...payload };
    newPayload.businessPartner = JSON.parse(
      JSON.stringify(OB.MobileApp.model.get('businessPartner'))
    );
    newPayload.terminal = OB.MobileApp.model.get('terminal');
    newPayload.session = OB.MobileApp.model.get('session');
    newPayload.orgUserId = OB.MobileApp.model.get('orgUserId');
    newPayload.pricelist = OB.MobileApp.model.get('pricelist');
    newPayload.contextUser = OB.MobileApp.model.get('context').user;

    return newPayload;
  }
);
