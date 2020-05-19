/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines a hook for the Ticket model in charge of checking the ticket discounts
 * and recalculating the taxes in order to maintain all the ticket data consistent.
 */

OB.App.StateAPI.Ticket.addModelHook({
  generatePayload: () => {
    return {
      discountRules: OB.Discounts.Pos.ruleImpls,
      taxRules: OB.Taxes.Pos.ruleImpls,
      bpSets: OB.Discounts.Pos.bpSets
    };
  },

  hook: (ticket, payload) => {
    return OB.App.State.Ticket.Utils.applyDiscountsAndTaxes(ticket, payload);
  }
});
