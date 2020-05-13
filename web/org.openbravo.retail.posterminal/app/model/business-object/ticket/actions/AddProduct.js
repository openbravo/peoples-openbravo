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

    setDeliveryMode(newLine, ticket);
    return newLine;
  }

  function setDeliveryMode(line, ticket) {
    if (
      line.product.productType === 'S' ||
      line.obrdmDeliveryMode // TODO: can line have it already set?
    ) {
      return;
    }

    let productDeliveryMode;
    let productDeliveryDate;

    if (ticket.isLayaway || ticket.orderType === 2) {
      productDeliveryMode = line.product.obrdmDeliveryModeLyw;
    } else {
      productDeliveryMode = line.product.obrdmDeliveryMode;

      productDeliveryDate = line.product.obrdmDeliveryDate;
    }

    const deliveryMode =
      productDeliveryMode || ticket.obrdmDeliveryModeProperty || 'PickAndCarry';

    // eslint-disable-next-line no-param-reassign
    line.obrdmDeliveryMode = deliveryMode;

    if (
      deliveryMode === 'PickupInStoreDate' ||
      deliveryMode === 'HomeDelivery'
    ) {
      const currentDate = new Date();
      currentDate.setHours(0);
      currentDate.setMinutes(0);
      currentDate.setSeconds(0);
      currentDate.setMilliseconds(0);

      // eslint-disable-next-line no-param-reassign
      line.obrdmDeliveryDate = productDeliveryMode
        ? productDeliveryDate || currentDate
        : ticket.obrdmDeliveryDateProperty;
    }
  }
})();
