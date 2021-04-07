/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global lodash */

(function AddProductDefinition() {
  OB.App.StateAPI.Ticket.registerAction('addProduct', (ticket, payload) => {
    return OB.App.State.Ticket.Utils.addProduct(ticket, payload);
  });

  // prepares the initial payload information including pack processing and checks the restrictions
  OB.App.StateAPI.Ticket.addProduct.addActionPreparation(
    async (ticket, payload) => {
      let newPayload = { ...payload };
      newPayload.products = newPayload.products
        .filter(productInfo => productInfo.qty !== 0)
        .map(productInfo => {
          let qty = productInfo.qty || 1;
          qty = OB.App.State.Ticket.Utils.isReturn(ticket) ? -qty : qty;
          const options = productInfo.options || {};
          const attrs = productInfo.attrs || {};
          return { ...productInfo, qty, options, attrs };
        });

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

  function checkRestrictions(ticket, payload) {
    const { products } = payload;

    checkClosedQuotation(ticket);
    OB.App.State.Ticket.Utils.checkIsEditable(ticket);
    checkQuantities(ticket, products);
    checkProductWithoutPrice(products);
    checkGenericProduct(products);
    checkCancelAndReplaceQty(ticket, products);
    checkAnonymousBusinessPartner(ticket, products);
    checkNotReturnableProduct(ticket, products);
    checkNotReturnableService(ticket, products);
    checkProductLocked(products);
    checkAllowSalesWithReturn(ticket, products);
  }

  function checkClosedQuotation(ticket) {
    if (
      OB.App.State.Ticket.Utils.isQuotation(ticket) &&
      ticket.hasbeenpaid === 'Y'
    ) {
      throw new OB.App.Class.ActionCanceled({
        errorMsg: 'OBPOS_QuotationClosed'
      });
    }
  }

  function checkQuantities(ticket, products) {
    products.forEach(pi => {
      const line = OB.App.State.Ticket.Utils.getLineToEdit(ticket, pi);
      const qty = line ? line.qty + pi.qty : pi.qty;

      if (
        (!OB.App.Security.hasPermission('OBPOS_ReturnLine') ||
          OB.App.State.Ticket.Utils.isQuotation(ticket)) &&
        qty < 0
      ) {
        throw new OB.App.Class.ActionCanceled({
          errorMsg: 'OBPOS_MsgCannotAddNegative'
        });
      }

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
      p => p.product.listPrice == null && !p.product.ispack
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

  function checkCancelAndReplaceQty(ticket, products) {
    products
      .filter(p => p.options.line)
      .forEach(p => {
        const line = ticket.lines.find(l => l.id === p.options.line);
        if (line && line.replacedorderline && line.qty < 0) {
          throw new OB.App.Class.ActionCanceled({
            errorConfirmation: 'OBPOS_CancelReplaceQtyEditReturn'
          });
        }
      });
  }

  function checkAnonymousBusinessPartner(ticket, products) {
    const anonymousNotAllowed = products.some(
      p =>
        p.product.oBPOSAllowAnonymousSale === false &&
        p.options.businessPartner === ticket.businessPartner.id
    );

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

  function checkNotReturnableProduct(ticket, products) {
    const notReturnable = products.find(p => {
      const line = p.options.line
        ? ticket.lines.find(l => l.id === p.options.line)
        : null;
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

  function checkNotReturnableService(ticket, products) {
    const service = findServiceForNegativeProduct(ticket, products);
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

  function findServiceForNegativeProduct(ticket, products) {
    return products.find(pi => {
      const line = OB.App.State.Ticket.Utils.getLineToEdit(ticket, pi);
      let relatedLines = pi.attrs.relatedLines || [];
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

  function checkAllowSalesWithReturn(ticket, products) {
    const newLineProducts = products.filter(
      pi => OB.App.State.Ticket.Utils.getLineToEdit(ticket, pi) === undefined
    );

    newLineProducts.forEach(pi => {
      if (
        OB.App.Security.hasPermission('OBPOS_NotAllowSalesWithReturn') &&
        !pi.options.allowLayawayWithReturn
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
        !pi.options.allowLayawayWithReturn
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

    const { discountRules } = payload.extraData;
    const packProcessings = payload.products.map(async pi => {
      const pack = OB.App.ProductPackProvider.getPack(pi, discountRules);
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
    if (newPayload.products.length === 0) {
      throw new OB.App.Class.ActionSilentlyCanceled(
        `No products to add after processing packs`
      );
    } else {
      newPayload.products.forEach(p => {
        if (!p.product) {
          throw new OB.App.Class.ActionCanceled({
            errorMsg: 'OBPOS_ErrorProductPack',
            // eslint-disable-next-line no-underscore-dangle
            messageParams: [packs[0].product._identifier]
          });
        }
      });
    }

    return newPayload;
  }

  async function prepareScaleProducts(payload) {
    const { products } = payload;
    if (
      !products.some(
        pi => pi.product.obposScale && !pi.options.isVerifiedReturn
      )
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
        productServiceLinked = await OB.App.DAL.remoteSearch(
          'ProductServiceLinked',
          {
            product: product.id,
            remoteFilters: [
              {
                columns: ['product'],
                operator: 'equals',
                value: product.id
              }
            ]
          }
        );
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

    const newPayload = { ...payload };

    const isQuotationAndAttributeAllowed = OB.App.Security.hasPermission(
      'OBPOS_AskForAttributesWhenCreatingQuotation'
    );

    const attributeDataRequests = newPayload.products.map(async pi => {
      const { product, qty, options } = pi;
      const productInfo = { ...pi };
      const newAttrs = {};
      const newOptions = {};
      let attributeValue = null;

      if (
        !options.line &&
        product.hasAttributes &&
        qty >= 1 &&
        (!OB.App.State.Ticket.Utils.isQuotation(ticket) ||
          isQuotationAndAttributeAllowed)
      ) {
        attributeValue = await OB.App.View.DialogUIHandler.inputData(
          'modalProductAttribute',
          { options }
        );
        attributeValue = Array.isArray(attributeValue)
          ? attributeValue[0]
          : attributeValue;
        if (attributeValue === null || attributeValue === undefined) {
          throw new OB.App.Class.ActionSilentlyCanceled(
            `No attribute provided for product ${product.id}`
          );
        }
        if (lodash.isEmpty(attributeValue)) {
          // the attributes for layaways accepts empty values, but for manage later easy to be null instead ""
          attributeValue = null;
        }
        newAttrs.attributeValue = attributeValue;
      }

      newAttrs.attributeSearchAllowed = attributeSearchAllowed;
      if (options.line) {
        newAttrs.productHavingSameAttribute = true;
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
          newAttrs.productHavingSameAttribute = true;
          newOptions.line = lineWithAttributeValue.id;
        } else {
          newAttrs.productHavingSameAttribute = false;
        }
      }

      productInfo.attrs = { ...productInfo.attrs, ...newAttrs };
      productInfo.options = { ...productInfo.options, ...newOptions };
      return productInfo;
    });

    newPayload.products = await Promise.all(attributeDataRequests);

    return newPayload;
  }

  async function prepareRelatedServices(payload) {
    const { products } = payload;
    const newPayload = { ...payload };
    const productsWithRelatedServicesInfo = products.map(async pi => {
      if (
        pi.product.productType === 'S' ||
        pi.options.isSilentAddProduct ||
        pi.attrs.originalOrderLineId
      ) {
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
        // Error getting related services information
        return pi;
      }

      const productInfo = { ...pi };
      productInfo.attrs = {
        ...productInfo.attrs,
        hasRelatedServices: data.hasservices,
        hasMandatoryServices: data.hasmandatoryservices
      };
      return productInfo;
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
      const criteria = await OB.App.StandardFilters.Services.apply({
        productId,
        productCategory
      });
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
        productPrices = await OB.App.DAL.remoteSearch('ProductPrice', {
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

  async function checkStock(ticket, payload) {
    const { products } = payload;

    for (let i = 0; i < products.length; i += 1) {
      const { product, qty, options, attrs } = products[i];
      const line = OB.App.State.Ticket.Utils.getLineToEdit(ticket, products[i]);
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
