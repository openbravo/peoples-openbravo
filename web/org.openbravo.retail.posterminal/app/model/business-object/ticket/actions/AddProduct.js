/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function AddProductDefinition() {
  window.newAddProduct = true; // TODO: remove this testing code

  OB.App.StateAPI.Ticket.registerAction('addProduct', (state, payload) => {
    const ticket = { ...state };
    const { products } = payload;

    ticket.lines = [...ticket.lines];
    products.forEach(productInfo => {
      // eslint-disable-next-line no-use-before-define
      const newLine = createLine(productInfo, ticket);
      ticket.lines.push(newLine);
    });

    return ticket;
  });

  function createLine(productInfo, ticket) {
    const { product, qty } = productInfo;

    const organization = {
      id: ticket.organization,
      country: OB.MobileApp.model.get('terminal').organizationCountryId,
      region: OB.MobileApp.model.get('terminal').organizationRegionId
    };

    const newLine = {
      id: OB.App.UUID.generate(),
      product,
      organization,
      uOM: product.uOM,
      qty: OB.DEC.number(qty, product.uOMstandardPrecision),
      grossPrice: OB.DEC.number(product.standardPrice),
      priceList: OB.DEC.number(product.listPrice),
      priceIncludesTax: ticket.priceIncludesTax
    };

    return newLine;
  }

  OB.App.StateAPI.Ticket.addProduct.addActionPreparation(
    async (state, payload) => {
      return payload;
    }
  );
})();
