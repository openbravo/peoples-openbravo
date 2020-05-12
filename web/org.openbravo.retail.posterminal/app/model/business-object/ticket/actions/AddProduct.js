/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* eslint-disable no-use-before-define */

(function AddProductDefinition() {
  window.newAddProduct = true; // TODO: remove this testing code

  OB.App.StateAPI.Ticket.registerAction('addProduct', (state, payload) => {
    const ticket = { ...state };
    const { products } = payload;

    ticket.lines = [...ticket.lines];
    products.forEach(productInfo => {
      const lineToEdit = getLineToEdit(productInfo.product, ticket);
      if (lineToEdit) {
        lineToEdit.qty += productInfo.qty;
      } else {
        const newLine = createLine(productInfo, ticket);
        ticket.lines.push(newLine);
      }
    });

    return ticket;
  });

  function getLineToEdit(product, ticket) {
    return ticket.lines.find(l => l.product.id === product.id);
  }

  function createLine(productInfo, ticket) {
    const { product, qty } = productInfo;

    // TODO: properly calculate organization
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
      priceIncludesTax: ticket.priceIncludesTax,
      isEditable: true, // TODO: calculate
      isDeletable: true // TODO: calculate
    };

    return newLine;
  }

  OB.App.StateAPI.Ticket.addProduct.addActionPreparation(
    async (state, payload) => {
      return payload;
    }
  );
})();
