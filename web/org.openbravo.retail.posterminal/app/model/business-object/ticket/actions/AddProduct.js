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
      const ticket = state.Ticket;

      if (!ticket.id) {
        // need to set the ticket ID to avoid an error when initializing the backbone order
        ticket.id = OB.App.UUID.generate();
      }

      checkRestrictions(ticket, payload);

      let newPayload = { ...payload };
      newPayload = await prepareScaleProducts(newPayload);
      newPayload = await prepareBOMProducts(newPayload);
      newPayload = await prepareProductCharacteristics(newPayload);
      newPayload = await prepareProductAttributes(ticket, newPayload);

      await checkStock(ticket, payload);

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
      priceList: OB.DEC.number(product.listPrice),
      priceIncludesTax: ticket.priceIncludesTax,
      isEditable: lodash.has(options, 'isEditable') ? options.isEditable : true,
      isDeletable: lodash.has(options, 'isDeletable')
        ? options.isDeletable
        : true
    };

    if (newLine.priceIncludesTax) {
      newLine.grossPrice = OB.DEC.number(product.standardPrice);
    } else {
      newLine.netPrice = OB.DEC.number(product.standardPrice);
    }

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

  function checkRestrictions(ticket, payload) {
    const { products, options } = payload;
    const productWithoutPrice = products.find(
      p => !p.product.listPrice && !p.product.ispack
    );
    if (productWithoutPrice) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_productWithoutPriceInPriceList',
        // eslint-disable-next-line no-underscore-dangle
        messageParams: [productWithoutPrice.product._identifier]
      });
    }

    const someGeneric = products.some(p => p.product.isGeneric);
    if (someGeneric) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_GenericNotAllowed'
      });
    }

    if (options.line && ticket.lines) {
      const line = ticket.lines.find(l => l.id === options.line);
      if (line && line.replacedorderline && line.qty < 0) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_CancelReplaceQtyEditReturn'
        });
      }
    }

    const anonymousNotAllowed =
      options.businessPartner === ticket.businessPartner.id &&
      products.some(p => p.product.oBPOSAllowAnonymousSale === false);

    if (anonymousNotAllowed) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_AnonymousSaleNotAllowed'
      });
    }

    const notReturnable = products.find(
      p =>
        (options.line ? options.line.qty + p.qty : p.qty) < 0 &&
        !p.product.returnable
    );
    if (notReturnable) {
      throw new OB.App.Class.ActionCanceled({
        title: 'OBPOS_UnreturnableProduct',
        errorConfirmation: 'OBPOS_UnreturnableProductMessage',
        // eslint-disable-next-line no-underscore-dangle
        messageParams: [notReturnable.product._identifier]
      });
    }

    if (ticket.isQuotation && ticket.hasbeenpaid === 'Y') {
      throw new OB.App.Class.ActionCanceled({
        errorMsg: 'OBPOS_QuotationClosed'
      });
    }

    const productLocked = products
      .filter(p => OB.DEC.compare(p.qty) === 1)
      .map(p => {
        const obj = {
          // eslint-disable-next-line no-underscore-dangle
          identifier: p.product._identifier,
          productStatus: OB.UTIL.ProductStatusUtils.getProductStatus(p.product)
        };
        return obj;
      })
      .find(o => o.productStatus && o.productStatus.restrictsalefrompos);

    if (productLocked) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_ErrorProductLocked',
        // eslint-disable-next-line no-underscore-dangle
        messageParams: [
          productLocked.identifier,
          productLocked.productStatus.name
        ]
      });
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

    // TODO: should we allow it if options.isVerifiedReturn?

    const weightResponse = await OB.POS.hwserver.getAsyncWeight();

    if (weightResponse.exception) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_MsgScaleServerNotAvailable'
      });
    }

    const weight = weightResponse.result;
    if (weight === 0) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_WeightZero'
      });
    }

    const newPayload = { ...payload };
    newPayload.products = [{ ...newPayload.products[0], qty: weight }];
    return newPayload;
  }

  async function prepareBOMProducts(payload) {
    const getProductBOM = async productId => {
      const productBOM = await OB.App.MasterdataModels.ProductBOM.find(
        new OB.App.Class.Criteria().criterion('product', productId).build()
      );

      if (productBOM.length === 0) {
        return undefined;
      }

      if (productBOM.find(bomLine => !bomLine.bomprice)) {
        OB.error(
          `A BOM product related with product ${productId} has no price`
        );
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_BOM_NoPrice'
        });
      }

      return productBOM.map(bomLine => {
        return {
          amount: OB.DEC.mul(bomLine.bomprice, bomLine.bomquantity),
          qty: bomLine.bomquantity,
          product: {
            id: bomLine.bomproduct,
            taxCategory: bomLine.bomtaxcategory
          }
        };
      });
    };

    const isBOM = product => {
      return (
        OB.Taxes.Pos.taxCategoryBOM.find(
          taxCategory => taxCategory.id === product.taxCategory
        ) && !product.productBOM
      );
    };

    const toProductWithBOM = async productInfo => {
      const { product } = productInfo;
      const productBOM = await getProductBOM(product.id);
      if (productBOM) {
        return {
          ...productInfo,
          product: { ...product, productBOM }
        };
      }
      return productInfo;
    };

    const newPayload = { ...payload };

    const productBOMAddings = newPayload.products.map(async p => {
      if (isBOM(p.product)) {
        const withBOM = await toProductWithBOM(p);
        return withBOM;
      }
      return p;
    });

    newPayload.products = await Promise.all(productBOMAddings);

    return newPayload;
  }

  async function prepareProductCharacteristics(payload) {
    const hasCharacteristics = product => {
      return (
        !product.productCharacteristics &&
        product.characteristicDescription &&
        !OB.App.Security.hasPermission('OBPOS_remote.product')
      );
    };

    const toProductWithCharacteristics = async productInfo => {
      const { product } = productInfo;
      const criteria = new OB.App.Class.Criteria()
        .criterion('product', product.id)
        .build();
      try {
        const productCharacteristics = await OB.App.MasterdataModels.ProductCharacteristicValue.find(
          criteria
        );
        return {
          ...productInfo,
          product: { ...product, productCharacteristics }
        };
      } catch (error) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBMOBC_Error' // TODO: create AD_Message for this
        });
      }
    };

    const newPayload = { ...payload };

    const productCharacteristicsAddings = newPayload.products.map(async p => {
      if (hasCharacteristics(p.product)) {
        const withCharacteristics = await toProductWithCharacteristics(p);
        return withCharacteristics;
      }
      return p;
    });

    newPayload.products = await Promise.all(productCharacteristicsAddings);

    return newPayload;
  }

  async function prepareProductAttributes(ticket, payload) {
    const { options, attrs } = payload;
    const attributeSearchAllowed = OB.App.Security.hasPermission(
      'OBPOS_EnableSupportForProductAttributes'
    );
    const isQuotationAndAttributeAllowed =
      ticket.isQuotation &&
      OB.App.Security.hasPermission(
        'OBPOS_AskForAttributesWhenCreatingQuotation'
      );
    const hasAttributes = productInfo => {
      return (
        !options.line &&
        attributeSearchAllowed &&
        productInfo.product.hasAttributes &&
        productInfo.qty >= 1 &&
        (!ticket.isQuotation || isQuotationAndAttributeAllowed)
      );
    };

    const checkSerialAttribute = (product, attributeValue) => {
      if (!attributeValue || !product.isSerialNo) {
        return true;
      }
      return ticket.lines.some(
        l =>
          (l.attSetInstanceDesc === attributeValue ||
            l.attributeValue === attributeValue) &&
          product.id === l.product.id
      );
    };

    const { products } = payload;
    if (!products.some(p => hasAttributes(p))) {
      return payload;
    }

    if (products.length > 1) {
      throw new Error('Cannot handle attributes for more than one product');
    }

    const newPayload = { ...payload };
    const { product } = products[0];

    const attributeValue = await OB.App.View.User.requestData({
      popup: 'modalProductAttribute',
      options: newPayload.options
    });

    if (OB.UTIL.isNullOrUndefined(attributeValue)) {
      throw new OB.App.Class.ActionSilentlyCanceled(
        `No attribute provided for product ${product.id}`
      );
    }

    // the attributes for layaways accepts empty values, but for manage later easy to be null instead ""
    newPayload.attrs.attributeValue = attributeValue || null;

    if (options && options.line) {
      newPayload.attrs.productHavingSameAttribute = true;
    } else {
      if (attrs && !checkSerialAttribute(product, attrs.attributeValue)) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_ProductDefinedAsSerialNo'
        });
      }
      const lineWithAttributeValue = ticket.lines.find(
        l =>
          attributeValue &&
          l.attributeValue === attributeValue &&
          product.id === l.product.id
      );
      if (lineWithAttributeValue) {
        newPayload.attrs.productHavingSameAttribute = true;
        newPayload.line = lineWithAttributeValue;
      }
    }
    return newPayload;
  }

  async function checkStock(ticket, payload) {
    const { products, options, attrs } = payload;

    if (products.length > 1) {
      throw new Error('Cannot check stock for more than one product');
    }

    const { product } = products[0];
    const { qty } = products[0];
    const settings = { ticket, options, attrs };

    const hasStock = await OB.App.StockChecker.hasStock(product, qty, settings);
    if (!hasStock) {
      throw new OB.App.Class.ActionSilentlyCanceled(
        `Add product canceled: there is no stock of product ${product.id}`
      );
    }
  }
})();
