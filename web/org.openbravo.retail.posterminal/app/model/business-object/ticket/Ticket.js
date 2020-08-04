/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

if (!OB.App.StateBackwardCompatibility) {
  // StateBackwardCompatibility is not currenlty deployed. This can occur in case only business logic
  // is deployed without BackBone modelds. Let's regitser the ticket as a standard state model without
  // backbone compatibility.
  OB.App.StateAPI.registerModel('Ticket');
} else {
  const initialState = {};
  const options = {
    ignoredProperties: [
      'undo',
      'json',
      'lines[*].product.img',
      'lines[*].product._filter'
    ],
    resetEvents: ['paintTaxes'],
    mapProperties: {
      bp: 'businessPartner',
      gross: 'grossAmount',
      net: 'netAmount',
      'lines[*].gross': 'baseGrossUnitAmount',
      'lines[*].net': 'baseNetUnitAmount',
      'lines[*].unitPrice': 'netUnitPrice',
      'lines[*].listPrice': 'netListPrice',
      'lines[*].price': bbTicket =>
        bbTicket.get('priceIncludesTax')
          ? 'baseGrossUnitPrice'
          : 'baseNetUnitPrice',
      'lines[*].pricenet': bbTicket =>
        bbTicket.get('priceIncludesTax') ? 'baseNetUnitPrice' : undefined,
      'lines[*].lineRate': 'taxRate',
      'lines[*].taxLines': 'taxes'
    },
    mapStateBackboneProperties: {
      'lines[*].baseGrossUnitPrice': ticket =>
        ticket.priceIncludesTax ? 'price' : undefined,
      'lines[*].baseNetUnitPrice': ticket =>
        ticket.priceIncludesTax ? undefined : 'price'
    }
  };

  OB.App.StateAPI.registerBackwardCompatibleModel(
    'Ticket',
    'OB.Model.Order',
    initialState,
    options
  );

  OB.UTIL.HookManager.registerHook(
    'ModelReady:pointOfSale',
    (args, callbacks) => {
      const backboneCurrentTicket = OB.MobileApp.model.receipt;

      // Associate backbone current ticket model with BackwardCompatDemoTicket Sate model
      // So that changes in one are reflected in the other
      OB.App.StateBackwardCompatibility.bind(
        OB.App.State.Ticket,
        backboneCurrentTicket
      );
      backboneCurrentTicket.trigger('change'); // forces backbone -> state propagation

      OB.UTIL.HookManager.callbackExecutor(args, callbacks);
    }
  );
}
