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
    const { products, options, attrs } = payload;

    ticket.lines = ticket.lines.map(l => {
      return { ...l };
    });
    products
      .map(productInfo => {
        return {
          ...productInfo,
          qty: isAReturn(ticket) ? -productInfo.qty : productInfo.qty
        };
      })
      .forEach(productInfo => {
        const lineToEdit = getLineToEdit(productInfo, ticket, options, attrs);
        if (lineToEdit) {
          lineToEdit.qty += productInfo.qty;
          setLineAttributes(lineToEdit, attrs, productInfo);
        } else {
          const newLines = createLines(productInfo, ticket, options, attrs);
          newLines.forEach(newLine => {
            setLineAttributes(newLine, attrs, productInfo);
            ticket.lines.push(newLine);
          });
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

      let newPayload = { options: {}, attrs: {}, ...payload };
      newPayload.attrs.hasMandatoryServices = false;
      newPayload.attrs.hasRelatedServices = false;

      newPayload.products = newPayload.products.map(productInfo => {
        return { ...productInfo, qty: productInfo.qty || 1 };
      });

      checkRestrictions(ticket, newPayload);

      newPayload = await prepareScaleProducts(newPayload);
      newPayload = await prepareBOMProducts(ticket, newPayload);
      newPayload = await prepareProductService(newPayload);
      newPayload = await prepareProductCharacteristics(newPayload);
      newPayload = await prepareProductAttributes(ticket, newPayload);

      await checkStock(ticket, payload);

      const payloadWithApprovals = await checkApprovals(ticket, newPayload);

      return payloadWithApprovals;
    }
  );

  function isAReturn(ticket) {
    return ticket.orderType === 1;
  }

  function setLineAttributes(line, attrs, productInfo) {
    const lineAttrs = { ...attrs };
    if (
      productInfo.product.productType === 'S' &&
      lineAttrs.relatedLines &&
      line.relatedLines
    ) {
      lineAttrs.relatedLines = OB.UTIL.mergeArrays(
        line.relatedLines,
        lineAttrs.relatedLines
      );
    }
    Object.assign(line, attrs);
  }

  function getLineToEdit(productInfo, ticket, options = {}, attrs = {}) {
    const { product, qty } = productInfo;
    if (product.obposScale || !product.groupProduct) {
      return undefined;
    }

    if (options.line) {
      return ticket.lines.find(l => l.id === options.line);
    }

    const attributeValue = attrs.attributeSearchAllowed && attrs.attributeValue;
    const serviceProduct =
      product.productType !== 'S' ||
      (product.productType === 'S' && !product.isLinkedToProduct);

    return ticket.lines.find(
      l =>
        l.product.id === product.id &&
        l.isEditable &&
        Math.sign(l.qty) === Math.sign(qty) &&
        (!attributeValue || l.attributeValue === attributeValue) &&
        !l.splitline &&
        (l.qty > 0 || !l.replacedorderline) &&
        (qty !== 1 || l.qty !== -1 || serviceProduct)
    );
  }

  function createLines(productInfo, ticket, options = {}, attrs = {}) {
    const { product, qty } = productInfo;

    if (product.groupProduct || product.avoidSplitProduct) {
      const lineQty =
        attrs.relatedLines &&
        attrs.relatedLines[0].deferred &&
        product.quantityRule === 'PP' &&
        qty > 0
          ? attrs.relatedLines[0].qty
          : qty;
      return [createNewLine(product, lineQty, ticket, options)];
    }

    const newLines = [];
    for (let count = 0; count < Math.abs(qty); count += 1) {
      newLines.push(createNewLine(product, Math.sign(qty), ticket, options));
    }
    return newLines;
  }

  function createNewLine(product, qty, ticket, options) {
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

    // TODO: related lines
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

    checkProductWithoutPrice(products);
    checkGenericProduct(products);
    checkCancelAndReplaceQty(ticket, options);
    checkAnonymousBusinessPartner(ticket, products, options);
    checkNotReturnable(ticket, products, options);
    checkClosedQuotation(ticket);
    checkProductLocked(ticket, products);
  }

  function checkProductWithoutPrice(products) {
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
  }

  function checkGenericProduct(products) {
    const someGeneric = products.some(p => p.product.isGeneric);
    if (someGeneric) {
      throw new OB.App.Class.ActionCanceled({
        warningMsg: 'OBPOS_GenericNotAllowed'
      });
    }
  }

  function checkCancelAndReplaceQty(ticket, options) {
    if (options.line && ticket.lines) {
      const line = ticket.lines.find(l => l.id === options.line);
      if (line && line.replacedorderline && line.qty < 0) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_CancelReplaceQtyEditReturn'
        });
      }
    }
  }

  function checkAnonymousBusinessPartner(ticket, products, options) {
    const anonymousNotAllowed =
      options.businessPartner === ticket.businessPartner.id &&
      products.some(p => p.product.oBPOSAllowAnonymousSale === false);

    if (anonymousNotAllowed) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_AnonymousSaleNotAllowed'
      });
    }
  }

  function checkNotReturnable(ticket, products, options) {
    const line = options.line
      ? ticket.lines.find(l => l.id === options.line)
      : null;
    const notReturnable = products.find(p => {
      const qty = isAReturn(ticket) ? -p.qty : p.qty;
      return (line ? line.qty + qty : qty) < 0 && !p.product.returnable;
    });
    if (notReturnable) {
      throw new OB.App.Class.ActionCanceled({
        title: 'OBPOS_UnreturnableProduct',
        errorConfirmation: 'OBPOS_UnreturnableProductMessage',
        // eslint-disable-next-line no-underscore-dangle
        messageParams: [notReturnable.product._identifier]
      });
    }
  }

  function checkClosedQuotation(ticket) {
    if (ticket.isQuotation && ticket.hasbeenpaid === 'Y') {
      throw new OB.App.Class.ActionCanceled({
        errorMsg: 'OBPOS_QuotationClosed'
      });
    }
  }

  function checkProductLocked(ticket, products) {
    const getProductStatus = product => {
      const status = product.productAssortmentStatus || product.productStatus;
      if (status) {
        // TODO: replace this
        return _.find(
          OB.MobileApp.model.get('productStatusList'),
          productStatus => {
            return status === productStatus.id;
          }
        );
      }
      return {};
    };

    const productLocked = products
      .filter(p => {
        const qty = isAReturn(ticket) ? -p.qty : p.qty;
        return OB.DEC.compare(qty) === 1;
      })
      .map(p => {
        const obj = {
          // eslint-disable-next-line no-underscore-dangle
          identifier: p.product._identifier,
          productStatus: getProductStatus(p.product)
        };
        return obj;
      })
      .find(o => o.productStatus && o.productStatus.restrictsalefrompos);

    if (productLocked) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_ErrorProductLocked',
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

  async function prepareBOMProducts(ticket, payload) {
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
          grossUnitAmount: ticket.priceIncludesTax
            ? OB.DEC.mul(bomLine.bomprice, bomLine.bomquantity)
            : undefined,
          netUnitAmount: ticket.priceIncludesTax
            ? undefined
            : OB.DEC.mul(bomLine.bomprice, bomLine.bomquantity),
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

  async function prepareProductService(payload) {
    const shouldLinkService = product => {
      return product.modifyTax && !product.productServiceLinked;
    };

    const toProductWithService = async productInfo => {
      const { product } = productInfo;
      let productServiceLinked;
      if (OB.App.Security.hasPermission('OBPOS_remote.product')) {
        productServiceLinked = await OB.App.DAL.find('ProductServiceLinked', {
          product: product.id,
          remoteFilters: [
            {
              columns: ['product'],
              operator: 'equals',
              value: product.id
            }
          ]
        });
      } else {
        productServiceLinked = await OB.App.MasterdataModels.ProductServiceLinked.find(
          new OB.App.Class.Criteria().criterion('product', product.id).build()
        );
      }
      if (productServiceLinked.length > 0) {
        return {
          ...productInfo,
          product: { ...product, productServiceLinked }
        };
      }
      return productInfo;
    };

    const newPayload = { ...payload };

    const productServiceLinkings = newPayload.products.map(async p => {
      // In case product is Service with modify tax enabled and it doesn't have ProductServiceLinked information yet, add it
      if (shouldLinkService(p.product)) {
        const withService = await toProductWithService(p);
        return withService;
      }
      return p;
    });

    newPayload.products = await Promise.all(productServiceLinkings);

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
      try {
        const productCharacteristics = await OB.App.MasterdataModels.ProductCharacteristicValue.find(
          new OB.App.Class.Criteria().criterion('product', product.id).build()
        );
        return {
          ...productInfo,
          product: { ...product, productCharacteristics }
        };
      } catch (error) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_CouldNotFindCharacteristics',
          // eslint-disable-next-line no-underscore-dangle
          messageParams: [product._identifier]
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
    const { options } = payload;
    const attributeSearchAllowed = OB.App.Security.hasPermission(
      'OBPOS_EnableSupportForProductAttributes'
    );
    const hasAttributes = productInfo => {
      return attributeSearchAllowed && productInfo.product.hasAttributes;
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
    const { product, qty } = products[0];

    let attributeValue = null;
    const isQuotationAndAttributeAllowed = OB.App.Security.hasPermission(
      'OBPOS_AskForAttributesWhenCreatingQuotation'
    );
    if (
      !options.line &&
      product.hasAttributes &&
      qty >= 1 &&
      (!ticket.isQuotation || isQuotationAndAttributeAllowed)
    ) {
      attributeValue = await OB.App.View.DialogUIHandler.inputData(
        'modalProductAttribute',
        newPayload.options
      );
      if (attributeValue === null || attributeValue === undefined) {
        throw new OB.App.Class.ActionSilentlyCanceled(
          `No attribute provided for product ${product.id}`
        );
      }
      if (lodash.isEmpty(attributeValue)) {
        // the attributes for layaways accepts empty values, but for manage later easy to be null instead ""
        attributeValue = null;
      }
      newPayload.attrs.attributeValue = attributeValue;
    }

    newPayload.attrs.attributeSearchAllowed = attributeSearchAllowed;
    if (options.line) {
      newPayload.attrs.productHavingSameAttribute = true;
    } else {
      if (!checkSerialAttribute(product, attributeValue)) {
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
        newPayload.line = lineWithAttributeValue.id;
      } else {
        newPayload.attrs.productHavingSameAttribute = false;
      }
    }
    return newPayload;
  }

  async function checkApprovals(ticket, payload) {
    const { products, options } = payload;
    const line = options.line
      ? ticket.lines.find(l => l.id === options.line)
      : null;
    const requireServiceReturnApproval = products.some(p => {
      const qty = isAReturn(ticket) ? -p.qty : p.qty;
      return (
        (line ? line.qty + qty : qty) < 0 &&
        p.product.productType === 'S' &&
        !p.product.ignoreReturnApproval
      );
    });
    if (requireServiceReturnApproval) {
      const newPayload = await OB.App.Security.requestApprovalForAction(
        'OBPOS_approval.returnService',
        payload
      );
      return newPayload;
    }
    return payload;
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
