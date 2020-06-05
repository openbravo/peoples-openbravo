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
        const { product } = productInfo;
        const lineToEdit = getLineToEdit(productInfo, ticket, options, attrs);
        if (lineToEdit) {
          lineToEdit.qty += productInfo.qty;
          setLineAttributes(lineToEdit, attrs, productInfo);
        } else if (product.groupProduct || product.avoidSplitProduct) {
          const newLine = createLine(productInfo, ticket, options, attrs);
          ticket.lines.push(newLine);
        } else {
          ticket.lines = ticket.lines.concat(
            createLines(productInfo, ticket, options, attrs)
          );
        }
      });

    return ticket;
  });

  OB.App.StateAPI.Ticket.addProduct.addActionPreparation(
    async (state, payload) => {
      const ticket = state.Ticket;

      let newPayload = { options: {}, attrs: {}, ...payload };

      newPayload.products = newPayload.products.map(productInfo => {
        return { ...productInfo, qty: productInfo.qty || 1 };
      });

      newPayload = await preparePacks(newPayload);

      checkRestrictions(ticket, newPayload);

      newPayload = await prepareScaleProducts(newPayload);
      newPayload = await prepareBOMProducts(ticket, newPayload);
      newPayload = await prepareServiceLinkedToProduct(newPayload);
      newPayload = await prepareProductCharacteristics(newPayload);
      newPayload = await prepareProductAttributes(ticket, newPayload);
      newPayload = await prepareRelatedServices(newPayload);
      newPayload = await prepareProductPrices(ticket, newPayload);

      await checkStock(ticket, newPayload);

      const payloadWithApprovals = await checkApprovals(ticket, newPayload);

      delete ticket.deferredOrder;

      return payloadWithApprovals;
    }
  );

  function isAReturn(ticket) {
    return ticket.orderType === 1;
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
    const { qty } = productInfo;

    const newLines = [];
    for (let count = 0; count < Math.abs(qty); count += 1) {
      newLines.push(createLine(productInfo, ticket, options, attrs));
    }
    return newLines;
  }

  function createLine(productInfo, ticket, options = {}, attrs = {}) {
    const { product, qty } = productInfo;

    const organization = getOrganization(ticket, product, attrs);
    const warehouse = getWarehouse(attrs);

    // TODO: validateAllowSalesWithReturn

    const lineQty =
      attrs.relatedLines &&
      attrs.relatedLines[0].deferred &&
      product.quantityRule === 'PP' &&
      qty > 0
        ? attrs.relatedLines[0].qty
        : qty;

    const newLine = {
      id: OB.App.UUID.generate(),
      product,
      organization,
      warehouse,
      uOM: product.uOM,
      qty: OB.DEC.number(lineQty, product.uOMstandardPrecision),
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

    setLineAttributes(newLine, attrs, productInfo);
    setRelatedLines(newLine, ticket);
    setDeliveryMode(newLine, ticket);
    return newLine;
  }

  function getOrganization(ticket, product, attrs) {
    if (attrs.organization) {
      return {
        id: attrs.organization.id,
        orgName: attrs.organization.name,
        country: attrs.organization.country,
        region: attrs.organization.region
      };
    }
    if (product.crossStore) {
      const store = OB.App.TerminalProperty.get('store').find(
        s => s.id === ticket.organization
      );
      return {
        id: store.id,
        orgName: store.name,
        country: store.country,
        region: store.region
      };
    }
    return {
      id: OB.App.TerminalProperty.get('terminal').organization,
      orgName: OB.App.TerminalProperty.get('terminal').organization$_identifier,
      country: OB.App.TerminalProperty.get('terminal').organizationCountryId,
      region: OB.App.TerminalProperty.get('terminal').organizationRegionId
    };
  }

  function getWarehouse(attrs) {
    const splitLine = attrs.splitline == null;
    return {
      id: splitLine
        ? OB.App.TerminalProperty.get('warehouses')[0].warehouseid
        : attrs.originalLine.warehouse.id,
      warehousename: splitLine
        ? OB.App.TerminalProperty.get('warehouses')[0].warehousename
        : attrs.originalLine.warehouse.warehousename
    };
  }

  function setLineAttributes(line, attrs, productInfo) {
    const lineAttrs = {
      ...attrs,
      hasRelatedServices: productInfo.hasRelatedServices,
      hasMandatoryServices: productInfo.hasMandatoryServices
    };
    if (
      productInfo.product.productType === 'S' &&
      lineAttrs.relatedLines &&
      line.relatedLines
    ) {
      lineAttrs.relatedLines = OB.App.ArrayUtils.union(
        line.relatedLines,
        lineAttrs.relatedLines
      );
    }
    Object.assign(line, lineAttrs);
  }

  function setRelatedLines(line, ticket) {
    if (!line.relatedLines) {
      return;
    }
    // eslint-disable-next-line no-param-reassign
    line.groupService = line.product.groupProduct;
    // Set the 'hasServices' property if the new line is adding a service related to a product to the order
    // Without the 'hasServices' property the quantity rules for services are not executed
    // TODO: do we need this?
    if (!ticket.hasServices) {
      // eslint-disable-next-line no-param-reassign
      ticket.hasServices = true;
    }

    // Check if it is necessary to modify the tax category of related products
    if (!line.product.productServiceLinked) {
      return;
    }

    ticket.product.productServiceLinked.forEach(productServiceLinked => {
      ticket.relatedLines
        .filter(
          relatedProduct =>
            relatedProduct.productCategory ===
            productServiceLinked.productCategory
        )
        .forEach(relatedProduct => {
          const relatedLine = ticket.lines.find(
            l => l.id === relatedProduct.orderlineId
          );
          if (relatedLine) {
            relatedLine.product.oldTaxCategory =
              relatedLine.product.taxCategory;
            relatedLine.product.taxCategory = productServiceLinked.taxCategory;
            relatedLine.previousLineRate = relatedLine.lineRate;
            // TODO: do we need this?
            relatedLine.recalculateTax = true;
          }
        });
    });
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
      if (ticket.deferredOrder) {
        // eslint-disable-next-line no-param-reassign
        delete ticket.deferredOrder;
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_AnonymousSaleNotAllowedDeferredSale'
        });
      }
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
        return OB.App.TerminalProperty.get('productStatusList').find(
          productStatus => status === productStatus.id
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

  async function preparePacks(payload) {
    if (
      !payload.products.some(
        p =>
          p.product.ispack &&
          p.product.productCategory === 'BE5D42E554644B6AA262CCB097753951'
      )
    ) {
      return payload;
    }

    const newPayload = { ...payload };
    newPayload.products = [];

    for (let i = 0; i < payload.products.length; i += 1) {
      const p = payload.products[i];
      if (
        p.product.ispack &&
        p.product.productCategory === 'BE5D42E554644B6AA262CCB097753951'
      ) {
        const discount = OB.Discounts.Pos.ruleImpls.find(
          d => d.id === p.product.id
        );
        if (discount.endingDate && discount.endingDate.length > 0) {
          const objDate = new Date(discount.endingDate);
          const now = new Date();
          const nowWithoutTime = new Date(now.toISOString().split('T')[0]);
          if (nowWithoutTime > objDate) {
            throw new OB.App.Class.ActionCanceled({
              title: 'OBPOS_PackExpired_header',
              errorConfirmation: 'OBPOS_PackExpired_body',
              messageParams: [
                // eslint-disable-next-line no-underscore-dangle
                discount._identifier,
                objDate.toLocaleDateString()
              ]
            });
          }
        }
        for (let j = 0; j < discount.products.length; j += 1) {
          // eslint-disable-next-line no-await-in-loop
          const product = await OB.App.MasterdataModels.Product.withId(
            discount.products[j].product.id
          );
          newPayload.products.push({
            product,
            qty: discount.products[j].obdiscQty,
            belongsToPack: true
          });
        }
      } else {
        newPayload.products.push(p);
      }
    }
    return newPayload;
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

  async function prepareServiceLinkedToProduct(payload) {
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

  async function prepareRelatedServices(payload) {
    const { products, options, attrs } = payload;
    if (options.isSilentAddProduct || attrs.originalOrderLineId) {
      return payload;
    }
    const newPayload = { ...payload };
    const productsWithRelatedServicesInfo = products.map(async pi => {
      if (pi.product.productType === 'S') {
        return pi;
      }
      const productId =
        pi.product.isNew &&
        OB.App.Security.hasPermission('OBPOS_remote.product')
          ? null
          : pi.product.forceFilterId || pi.product.id;
      const data = await loadRelatedServices(
        pi.product.productType,
        productId,
        pi.product.productCategory
      );
      if (data.exception) {
        OB.error('Error getting related services information');
        return pi;
      }
      return {
        ...pi,
        hasRelatedServices: data.hasservices,
        hasMandatoryServices: data.hasmandatoryservices
      };
    });
    newPayload.products = await Promise.all(productsWithRelatedServicesInfo);
    return newPayload;
  }

  async function loadRelatedServices(productType, productId, productCategory) {
    const result = { hasservices: false, hasMandatoryServices: false };
    if (productType === 'S') {
      return result;
    }
    if (OB.App.Security.hasPermission('OBPOS_remote.product')) {
      const params = {};
      const date = new Date();
      params.terminalTime = date;
      params.terminalTimeOffset = date.getTimezoneOffset();

      const body = {
        product: productId,
        productCategory,
        parameters: params,
        remoteFilters: [
          {
            columns: [],
            operator: 'filter',
            value: 'OBRDM_DeliveryServiceFilter',
            params: [false]
          }
        ]
      };
      try {
        const data = await OB.App.Request.mobileServiceRequest(
          'org.openbravo.retail.posterminal.process.HasServices',
          body
        );
        return data.response.data || result;
      } catch (error) {
        return { exception: error };
      }
    } else {
      // non-high volumes: indexedDB
      let criteria = new OB.App.Class.Criteria();
      criteria = await OB.UTIL.servicesFilter(
        criteria,
        productId,
        productCategory
      );
      criteria.criterion('obrdmIsdeliveryservice', false);
      try {
        const products = await OB.App.MasterdataModels.Product.find(
          criteria.build()
        );
        if (products) {
          result.hasservices = products.length > 0;
          result.hasmandatoryservices = products.some(
            product => product.proposalType === 'MP'
          );
        }
        return result;
      } catch (error) {
        return { exception: error };
      }
    }
  }

  async function prepareProductPrices(ticket, payload) {
    if (!OB.App.Security.hasPermission('EnableMultiPriceList')) {
      return payload;
    }
    const newPayload = { ...payload };
    const productPriceUpdates = payload.products.map(async pi => {
      const result = { ...pi };
      if (pi.product.ispack || pi.product.updatePriceFromPricelist === false) {
        return result;
      }
      let productPrice;
      try {
        productPrice = await getProductPrice(ticket, pi);
      } catch (error) {
        throw new OB.App.Class.ActionCanceled({
          warningMsg: 'OBPOS_ProductNotFoundInPriceList'
        });
      }
      if (!productPrice) {
        throw new OB.App.Class.ActionCanceled({
          warningMsg: 'OBPOS_ProductNotFoundInPriceList'
        });
      }
      result.product = { ...result.product, ...productPrice };
      return result;
    });
    newPayload.products = await Promise.all(productPriceUpdates);
    return newPayload;
  }

  async function getProductPrice(ticket, productInfo) {
    const { product } = productInfo;
    let result = {
      standardPrice: product.standardPrice,
      listPrice: product.listPrice
    };
    if (product.crossStore) {
      let productPrice;
      if (product.productPrices) {
        productPrice = product.productPrices.find(
          pp => pp.priceListId === ticket.businessPartner.priceList
        );
      }
      result = productPrice
        ? {
            standardPrice: productPrice.price,
            listPrice: productPrice.price,
            currentPrice: productPrice
          }
        : undefined;
    } else if (
      ticket.priceList !== OB.App.TerminalProperty.get('terminal').priceList
    ) {
      let productPrices;
      if (OB.App.Security.hasPermission('OBPOS_remote.product')) {
        const productId = {
          columns: ['m_product_id'],
          operator: 'equals',
          value: product.id,
          isId: true
        };
        const pricelistId = {
          columns: ['m_pricelist_id'],
          operator: 'equals',
          value: ticket.priceList,
          isId: true
        };
        productPrices = await OB.App.DAL.find('ProductPrice', {
          remoteFilters: [productId, pricelistId]
        });
      } else {
        productPrices = await OB.App.MasterdataModels.ProductPrice.find(
          new OB.App.Class.Criteria()
            .criterion('m_pricelist_id', ticket.priceList)
            .criterion('m_product_id', product.id)
            .build()
        );
      }
      result =
        productPrices && productPrices.length > 0
          ? {
              standardPrice: productPrices[0].pricestd,
              listPrice: productPrices[0].pricelist
            }
          : undefined;
    }
    return result;
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

    for (let i = 0; i < products.length; i += 1) {
      const { product } = products[i];
      const qty = isAReturn(ticket) ? -products[i].qty : products[i].qty;
      const line = getLineToEdit(products[i], ticket, options, attrs);
      const lineId = line ? line.id : payload.line;
      const settings = { ticket, lineId, options, attrs };

      // eslint-disable-next-line no-await-in-loop
      const hasStock = await OB.App.StockChecker.hasStock(
        product,
        qty,
        settings
      );

      if (!hasStock) {
        throw new OB.App.Class.ActionSilentlyCanceled(
          `Add product canceled: there is no stock of product ${product.id}`
        );
      }
    }
  }
})();
