/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global lodash */
/* eslint-disable no-use-before-define */

(function AddProductDefinition() {
  window.newAddProduct = true; // TODO: remove this testing code

  OB.App.StateAPI.Ticket.registerAction('addProduct', (state, payload) => {
    const ticket = { ...state };
    const { products, options } = payload;

    ticket.lines = ticket.lines.map(l => {
      return { ...l };
    });
    products.forEach(productInfo => {
      const lineToEdit = getLineToEdit(productInfo, ticket, options);
      if (lineToEdit) {
        lineToEdit.qty += productInfo.qty;
      } else {
        const newLine = createLine(productInfo, ticket, options);
        ticket.lines.push(newLine);
      }
    });

    return ticket;
  });

  OB.App.StateAPI.Ticket.addProduct.addActionPreparation(
    async (state, payload) => {
      let newPayload = { ...payload };

      newPayload = await prepareScaleProducts(newPayload);
      return newPayload;
    }
  );

  function getLineToEdit(productInfo, ticket, options = {}) {
    const { product, qty } = productInfo;
    if (product.obposScale || !product.groupProduct) {
      return undefined;
    }

    if (options.line) {
      return ticket.lines.find(l => l.id === options.line);
    }

    return ticket.lines.find(
      l =>
        l.product.id === product.id &&
        l.isEditable &&
        Math.sign(l.qty) === Math.sign(qty)
      // TODO: attributeSearchAllowed
    );
  }

  function createLine(productInfo, ticket, options = {}) {
    const { product, qty } = productInfo;

    // TODO: properly calculate organization
    const organization = {
      id: ticket.organization,
      country: OB.MobileApp.model.get('terminal').organizationCountryId,
      region: OB.MobileApp.model.get('terminal').organizationRegionId
    };

    // TODO: validateAllowSalesWithReturn

    const newLine = {
      id: OB.App.UUID.generate(),
      product,
      organization,
      uOM: product.uOM,
      qty: OB.DEC.number(qty, product.uOMstandardPrecision),
      grossPrice: OB.DEC.number(product.standardPrice),
      priceList: OB.DEC.number(product.listPrice),
      priceIncludesTax: ticket.priceIncludesTax,
      isEditable: lodash.has(options, 'isEditable') ? options.isEditable : true,
      isDeletable: lodash.has(options, 'isDeletable')
        ? options.isDeletable
        : true
    };

    setDeliveryMode(newLine, ticket);
    // TODO: related lines
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
    let productDeliveryTime;

    if (ticket.isLayaway || ticket.orderType === 2) {
      productDeliveryMode = line.product.obrdmDeliveryModeLyw;
    } else {
      productDeliveryMode = line.product.obrdmDeliveryMode;

      productDeliveryDate = line.product.obrdmDeliveryDate;
      productDeliveryTime = line.product.obrdmDeliveryTime;
    }

    const deliveryMode =
      productDeliveryMode || ticket.obrdmDeliveryModeProperty || 'PickAndCarry';

    // eslint-disable-next-line no-param-reassign
    line.obrdmDeliveryMode = deliveryMode;

    if (
      deliveryMode === 'PickupInStoreDate' ||
      deliveryMode === 'HomeDelivery'
    ) {
      // TODO: review
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

    if (deliveryMode === 'HomeDelivery') {
      const currentTime = new Date();
      currentTime.setSeconds(0);
      currentTime.setMilliseconds(0);

      // eslint-disable-next-line no-param-reassign
      line.obrdmDeliveryTime = productDeliveryMode
        ? productDeliveryTime || currentTime
        : ticket.obrdmDeliveryTimeProperty;
    }
  }

  async function prepareScaleProducts(payload) {
    const { products } = payload;
    if (!products.some(pi => pi.product.obposScale)) {
      return payload;
    }

    if (products.length > 1) {
      throw new Error('Cannot handle more than one scale product');
    }

    const newPayload = { ...payload };
    const weightResponse = await OB.POS.hwserver.getAsyncWeight();
    const weight = weightResponse.result;

    if (weight === 0) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_WeightZero'
      });
    }

    newPayload.products = [{ ...newPayload.products[0], qty: weight }];
    return newPayload;
  }
})();
