/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

if (!OB.App.StateBackwardCompatibility) {
  // StateBackwardCompatibility is not currenlty deployed. This can occur in case only business logic
  // is deployed without BackBone modelds. Let's regitser the ticket as a standard state model without
  // backbone compatibility.
  OB.App.StateAPI.registerModel('Ticket', {}, { isUndoable: true });
} else {
  const initialState = {};
  const options = {
    ignoredProperties: [
      'undo',
      'json',
      'lines[*].product.img',
      'lines[*].product._filter',
      'payments[*].paymentData.provider.image'
    ],
    resetEvents: ['paintTaxes'],
    mapProperties: {
      bp: 'businessPartner',
      gross: 'grossAmount',
      net: 'netAmount',
      'calculatedInvoice.bp': 'businessPartner',
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
      'lines[*].lineRate': 'taxRate'
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
    options,
    { isUndoable: true }
  );
}

OB.App.StateAPI.registerIdentifierForMessages('OBPOS_Order', message => {
  return message.documentNo;
});
