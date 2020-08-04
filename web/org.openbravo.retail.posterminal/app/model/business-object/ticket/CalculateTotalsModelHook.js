/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines a hook for the Ticket model in charge of calculating the totals in order to maintain
 * all the ticket data consistent. The ticket totals include taxes, discounts and other calculated fields.
 */

OB.App.StateAPI.Ticket.addModelHook({
  generatePayload: () => {
    const { qtyEdition } = OB.Format.formats;
    return {
      discountRules: OB.Discounts.Pos.ruleImpls,
      taxRules: OB.Taxes.Pos.ruleImpls,
      bpSets: OB.Discounts.Pos.bpSets,
      qtyScale: qtyEdition.length - qtyEdition.indexOf('.') - 1
    };
  },

  hook: (ticket, payload) => {
    return OB.App.State.Ticket.Utils.calculateTotals(ticket, payload);
  }
});
