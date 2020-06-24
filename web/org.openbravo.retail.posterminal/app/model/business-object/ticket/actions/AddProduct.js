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
    let ticket = { ...state };
    // products: the product information to create the lines
    // options: settings that allow to change the behavior of the action
    // attrs: additional properties to be included in the created lines
    const { products, options, attrs } = payload;
    delete ticket.deferredOrder;

    ticket.lines = ticket.lines.map(l => {
      return { ...l };
    });

    products.forEach(productInfo => {
      const { product } = productInfo;
      const lineToEdit = getLineToEdit(productInfo, ticket, options, attrs);
      if (lineToEdit) {
        // add product to an existing line
        lineToEdit.qty += productInfo.qty;
        setLineAttributes(lineToEdit, attrs, productInfo);
      } else if (product.groupProduct || product.avoidSplitProduct) {
        // add product to a new line
        ticket = createLine(productInfo, ticket, options, attrs);
      } else {
        // add product creating multiple new lines with quantity 1 each
        ticket = createLines(productInfo, ticket, options, attrs);
      }
    });

    // delete the lines resulting with quantity zero
    ticket.lines = ticket.lines.filter(l => l.qty !== 0);

    ticket = OB.App.State.Ticket.Utils.updateServicesInformation(ticket);

    return ticket;
  });

  // prepares the initial payload information including pack processing and checks the restrictions
  OB.App.StateAPI.Ticket.addProduct.addActionPreparation(
    async (ticket, payload) => {
      let newPayload = { options: {}, attrs: {}, ...payload };
      newPayload.products = newPayload.products
        .filter(productInfo => productInfo.qty !== 0)
        .map(productInfo => {
          let qty = productInfo.qty || 1;
          qty = OB.App.State.Ticket.Utils.isReturn(ticket) ? -qty : qty;
          return { ...productInfo, qty };
        });
      newPayload.options.taxRules = OB.Taxes.Pos.ruleImpls;
      newPayload = await processPacks(newPayload);
      newPayload = await prepareProductAttributes(ticket, newPayload);

      checkRestrictions(ticket, newPayload);
      return newPayload;
    },
    async (ticket, payload) => payload,
    100
  );

  // main action preparations of the products to be added which may include validations
  OB.App.StateAPI.Ticket.addProduct.addActionPreparation(
    async (ticket, payload) => {
      let newPayload = { ...payload };

      newPayload = await prepareScaleProducts(newPayload);
      newPayload = await prepareBOMProducts(ticket, newPayload);
      newPayload = await prepareProductServiceLinked(newPayload);
      newPayload = await prepareProductCharacteristics(newPayload);
      newPayload = await prepareRelatedServices(newPayload);
      newPayload = await prepareProductPrices(ticket, newPayload);

      return newPayload;
    },
    async (ticket, payload) => payload,
    200
  );

  // checks the stock, this may be the most time-cost operation so it is executed once the payload is validated
  OB.App.StateAPI.Ticket.addProduct.addActionPreparation(
    async (ticket, payload) => {
      const newPayload = { ...payload };
      await checkStock(ticket, newPayload);
      return newPayload;
    },
    async (ticket, payload) => payload,
    300
  );

  // check the approvals
  OB.App.StateAPI.Ticket.addProduct.addActionPreparation(
    async (ticket, payload) => {
      const newPayload = { ...payload };
      const payloadWithApprovals = await checkApprovals(ticket, newPayload);
      return payloadWithApprovals;
    },
    async (ticket, payload) => payload,
    400
  );

  function getLineToEdit(productInfo, ticket, options, attrs) {
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

  function createLines(productInfo, ticket, options, attrs) {
    const { qty } = productInfo;
    let newTicket = ticket;
    const lineQty = Math.sign(qty) === -1 ? -1 : 1;

    for (let count = 0; count < Math.abs(qty); count += 1) {
      const newProductInfo = { ...productInfo, qty: lineQty };
      newTicket = createLine(newProductInfo, newTicket, options, attrs);
    }

    return newTicket;
  }

  function createLine(productInfo, ticket, options, attrs) {
    const { product, qty } = productInfo;

    const lineQty =
      attrs.relatedLines &&
      attrs.relatedLines[0].deferred &&
      product.quantityRule === 'PP' &&
      qty > 0
        ? attrs.relatedLines[0].qty
        : qty;

    const { newTicket, newLine } = OB.App.State.Ticket.Utils.createLine(
      ticket,
      product,
      lineQty
    );

    if (attrs.organization) {
      newLine.organization = {
        id: attrs.organization.id,
        orgName: attrs.organization.name,
        country: attrs.organization.country,
        region: attrs.organization.region
      };
    }

    if (attrs.splitline != null && attrs.originalLine) {
      const originalLine = newTicket.lines.find(
        l => l.id === attrs.originalLine
      );
      newLine.warehouse = {
        id: originalLine.warehouse.id,
        warehousename: originalLine.warehouse.warehousename
      };
    }

    if (lodash.has(options, 'isEditable')) {
      newLine.isEditable = options.isEditable;
    }

    if (lodash.has(options, 'isDeletable')) {
      newLine.isDeletable = options.isDeletable;
    }

    setLineAttributes(newLine, attrs, productInfo);
    updateServiceRelatedLines(newLine, newTicket, options.taxRules);

    return newTicket;
  }

  function setLineAttributes(line, attrs, productInfo) {
    const lineAttrs = {
      ...attrs,
      hasRelatedServices: productInfo.hasRelatedServices,
      hasMandatoryServices: attrs.splitline
        ? false
        : productInfo.hasMandatoryServices
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

  function updateServiceRelatedLines(line, ticket, taxRules) {
    if (!line.relatedLines) {
      return;
    }
    // eslint-disable-next-line no-param-reassign
    line.groupService = line.product.groupProduct;
    // Set the 'hasServices' property if the new line is adding a service related to a product
    // Without the 'hasServices' property the quantity rules for services are not executed
    if (!ticket.hasServices) {
      // eslint-disable-next-line no-param-reassign
      ticket.hasServices = true;
    }

    if (!line.product.productServiceLinked) {
      return;
    }

    // Check if it is necessary to modify the tax category of related lines
    line.product.productServiceLinked.forEach(productServiceLinked => {
      line.relatedLines
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
            relatedLine.product = {
              ...relatedLine.product,
              previousTaxCategory: relatedLine.product.taxCategory,
              taxCategory: productServiceLinked.taxCategory
            };
            if (relatedLine.priceIncludesTax) {
              relatedLine.previousBaseGrossUnitPrice =
                relatedLine.baseGrossUnitPrice;
            }
          }
        });
    });

    // Update the price of the related lines whose tax category has changed
    if (
      ticket.priceIncludesTax &&
      ticket.lines.some(l => l.previousBaseGrossUnitPrice && l.taxRate)
    ) {
      const taxes = OB.Taxes.Pos.applyTaxes(ticket, taxRules);
      ticket.lines
        .filter(l => l.previousBaseGrossUnitPrice && l.taxRate)
        .forEach(l => {
          const lineTax = taxes.lines.find(lt => lt.id === l.id);
          // eslint-disable-next-line no-param-reassign
          l.baseGrossUnitPrice = OB.DEC.mul(
            OB.DEC.div(l.previousBaseGrossUnitPrice, l.taxRate),
            lineTax.taxRate
          );
        });
    }
  }

  function checkRestrictions(ticket, payload) {
    const { products, options, attrs } = payload;

    checkQuantities(ticket, products, options, attrs);
    checkProductWithoutPrice(products);
    checkGenericProduct(products);
    checkCancelAndReplaceQty(ticket, options);
    checkAnonymousBusinessPartner(ticket, products, options);
    checkNotReturnableProduct(ticket, products, options);
    checkNotReturnableService(ticket, products, options, attrs);
    checkClosedQuotation(ticket);
    checkProductLocked(products);
    checkAllowSalesWithReturn(ticket, products, options, attrs);
  }

  function checkQuantities(ticket, products, options, attrs) {
    products.forEach(pi => {
      const line = getLineToEdit(pi, ticket, options, attrs);
      const qty = line ? line.qty + pi.qty : pi.qty;

      if (OB.App.State.Ticket.Utils.isReturn(ticket) && qty > 0) {
        throw new OB.App.Class.ActionCanceled({
          errorMsg: 'OBPOS_MsgCannotAddPostiveToReturn'
        });
      }
    });
  }

  function checkProductWithoutPrice(products) {
    if (
      OB.App.Security.hasPermission('OBPOS_allowProductsNoPriceInMainPricelist')
    ) {
      // With the preference OBPOS_allowProductsNoPriceInMainPricelist
      // it is possible to add product without price in the terminal's main list
      return;
    }
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
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_AnonymousSaleNotAllowedDeferredSale'
        });
      }
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_AnonymousSaleNotAllowed'
      });
    }
  }

  function checkNotReturnableProduct(ticket, products, options) {
    const line = options.line
      ? ticket.lines.find(l => l.id === options.line)
      : null;
    const notReturnable = products.find(p => {
      return (line ? line.qty + p.qty : p.qty) < 0 && !p.product.returnable;
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

  function checkNotReturnableService(ticket, products, options, attrs) {
    const service = findServiceForNegativeProduct(
      ticket,
      products,
      options,
      attrs
    );
    if (service && !service.product.returnable) {
      // Cannot add not returnable service to a negative product
      throw new OB.App.Class.ActionCanceled({
        title: 'OBPOS_UnreturnableProduct',
        errorConfirmation: 'OBPOS_UnreturnableProductMessage',
        // eslint-disable-next-line no-underscore-dangle
        messageParams: [service.product._identifier]
      });
    }
  }

  function findServiceForNegativeProduct(ticket, products, options, attrs) {
    return products.find(pi => {
      const line = getLineToEdit(pi, ticket, options, attrs);
      let relatedLines = attrs.relatedLines || [];
      if (line && line.relatedLines) {
        relatedLines = OB.App.ArrayUtils.union(relatedLines, line.relatedLines);
      }

      if (relatedLines.length === 0 || (line && line.originalOrderLineId)) {
        return false;
      }

      let newqtyminus = ticket.lines
        .filter(
          l => l.qty < 0 && relatedLines.some(rl => rl.orderlineId === l.id)
        )
        .reduce((t, l) => t + l.qty, 0);

      if (pi.product.quantityRule === 'UQ') {
        newqtyminus = newqtyminus ? -1 : 0;
      }

      const lineQty = line ? line.qty + pi.qty : pi.qty;
      return newqtyminus && lineQty > 0;
    });
  }

  function checkClosedQuotation(ticket) {
    if (ticket.isQuotation && ticket.hasbeenpaid === 'Y') {
      throw new OB.App.Class.ActionCanceled({
        errorMsg: 'OBPOS_QuotationClosed'
      });
    }
  }

  function checkProductLocked(products) {
    const getProductStatus = product => {
      const status = product.productAssortmentStatus || product.productStatus;
      if (status) {
        return OB.App.TerminalProperty.get('productStatusList').find(
          productStatus => status === productStatus.id
        );
      }
      return undefined;
    };

    const productLocked = products
      .filter(p => OB.DEC.compare(p.qty) === 1)
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

  function checkAllowSalesWithReturn(ticket, products, options, attrs) {
    const newLineProducts = products.filter(
      pi => getLineToEdit(pi, ticket, options, attrs) === undefined
    );

    newLineProducts.forEach(pi => {
      if (
        OB.App.Security.hasPermission('OBPOS_NotAllowSalesWithReturn') &&
        !options.allowLayawayWithReturn
      ) {
        const receiptLines = ticket.lines.length;
        const negativeLines = ticket.lines.filter(l => l.qty < 0).length;
        if (pi.qty > 0 && negativeLines > 0) {
          throw new OB.App.Class.ActionCanceled({
            errorMsg: 'OBPOS_MsgCannotAddPositive'
          });
        } else if (pi.qty < 0 && negativeLines !== receiptLines) {
          throw new OB.App.Class.ActionCanceled({
            errorMsg: 'OBPOS_MsgCannotAddNegative'
          });
        }
      }
      if (
        !OB.App.Security.hasPermission('OBPOS_AllowLayawaysNegativeLines') &&
        OB.App.State.Ticket.Utils.isLayaway(ticket) &&
        pi.qty < 0 &&
        !options.allowLayawayWithReturn
      ) {
        throw new OB.App.Class.ActionCanceled({
          errorMsg: 'OBPOS_layawaysOrdersWithReturnsNotAllowed'
        });
      }
    });
  }

  async function processPacks(payload) {
    const packs = payload.products.filter(p => p.product.ispack);
    if (packs.length === 0) {
      return payload;
    }
    if (packs.length > 1) {
      throw new Error('Cannot handle more than one pack');
    }
    if (packs[0].qty !== 1) {
      throw new Error('Cannot handle more than unit of a pack');
    }

    const packProcessings = payload.products.map(async pi => {
      const pack = OB.App.ProductPackProvider.getPack(pi.product);
      if (!pack) {
        return pi;
      }
      try {
        const packProducts = await pack.process();
        return packProducts;
      } catch (error) {
        throw new OB.App.Class.ActionCanceled(error);
      }
    });
    const newPayload = { ...payload };
    newPayload.products = (await Promise.all(packProcessings)).flat();
    return newPayload;
  }

  async function prepareScaleProducts(payload) {
    const { products, options } = payload;
    if (
      !products.some(pi => pi.product.obposScale) ||
      options.isVerifiedReturn
    ) {
      return payload;
    }

    if (products.length > 1) {
      throw new Error('Cannot handle more than one scale product');
    }

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

  async function prepareProductServiceLinked(payload) {
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
      if (!attributeValue || !product.isSerialNo || ticket.lines.length === 0) {
        return true;
      }
      return !ticket.lines.some(
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
    const result = { hasservices: false, hasmandatoryservices: false };
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
    const { products, options, attrs } = payload;
    const line = options.line
      ? ticket.lines.find(l => l.id === options.line)
      : null;
    let newPayload = { ...payload };

    const linesWithQuantityZero = products.some(pi => {
      const lineToEdit = getLineToEdit(pi, ticket, options, attrs);
      const qty = lineToEdit ? lineToEdit.qty + pi.qty : pi.qty;
      return qty === 0;
    });

    if (linesWithQuantityZero) {
      newPayload = await OB.App.Security.requestApprovalForAction(
        'OBPOS_approval.deleteLine',
        newPayload
      );
    }

    const servicesWithReturnApproval = products.filter(p => {
      return (
        (line ? line.qty + p.qty : p.qty) < 0 &&
        p.product.productType === 'S' &&
        !p.product.ignoreReturnApproval
      );
    });

    if (servicesWithReturnApproval.length === 0) {
      const service = findServiceForNegativeProduct(
        ticket,
        products,
        options,
        attrs
      );
      if (service) {
        servicesWithReturnApproval.push(service);
      }
    }

    if (servicesWithReturnApproval.length > 0) {
      const separator = `<br>${OB.App.SpecialCharacters.bullet()}`;
      const servicesToApprove = `
        ${separator} 
        ${servicesWithReturnApproval
          // eslint-disable-next-line no-underscore-dangle
          .map(p => p._identifier)
          .join(separator)}`;
      newPayload = await OB.App.Security.requestApprovalForAction(
        {
          approvalType: 'OBPOS_approval.returnService',
          message: 'OBPOS_approval.returnService',
          params: [servicesToApprove]
        },
        payload
      );
    }
    return newPayload;
  }

  async function checkStock(ticket, payload) {
    const { products, options, attrs } = payload;

    for (let i = 0; i < products.length; i += 1) {
      const { product, qty } = products[i];
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
