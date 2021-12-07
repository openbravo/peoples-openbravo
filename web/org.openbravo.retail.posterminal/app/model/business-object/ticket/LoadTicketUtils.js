/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/**
 * @fileoverview Define utility functions for the Add Product action
 */

OB.App.StateAPI.Ticket.registerUtilityFunctions({
  /**
   * Load ticket information for ticket defined in the payload
   *
   * @param {object} payload - The payload with the ticket id or ticket documentNo which needs to be loaded
   *
   * @returns {object} the payload with the ticket information
   */
  async loadTicket(payload) {
    const data = await OB.App.Request.mobileServiceRequest(
      'org.openbravo.retail.posterminal.PaidReceipts',
      {
        orderid: payload.ticket.id,
        // If action was called without order id, we can specify the docNo to load the ticket
        documentNo: payload.ticket.id ? undefined : payload.ticket.documentNo,
        crossStore: payload.ticket.isCrossStore
      }
    );

    if (data.response.error) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: data.response.error.message
      });
    } else if (data.response.data[0].recordInImportEntry) {
      throw new OB.App.Class.ActionCanceled({
        errorConfirmation: 'OBPOS_ReceiptNotSynced',
        messageParams: [data.response.data[0].documentNo]
      });
    }

    return {
      ...payload,
      ticket: { ...data.response.data[0] }
    };
  },

  /**
   * Load business partner information for ticket defined in the payload
   *
   * @param {object} payload - The payload with the ticket for which business partner needs to be loaded
   *
   * @returns {object} the payload with the ticket including business partner information
   */
  async loadBusinessPartner(payload) {
    const isRemoteCustomer = OB.App.Security.hasPermission(
      'OBPOS_remote.customer'
    );
    const getBusinessPartnerFromBackoffice = async () => {
      try {
        const data = await OB.App.Request.mobileServiceRequest(
          'org.openbravo.retail.posterminal.master.LoadedCustomer',
          {
            parameters: {
              bpartnerId: { value: payload.ticket.bp },
              bpLocationId: { value: payload.ticket.bpLocId },
              bpBillLocationId:
                payload.ticket.bpLocId !== payload.ticket.bpBillLocId
                  ? { value: payload.ticket.bpBillLocId }
                  : undefined
            }
          }
        );
        if (data.response.data.length < 2) {
          throw new OB.App.Class.ActionCanceled({
            errorConfirmation: 'OBPOS_NoCustomerForPaidReceipt'
          });
        }
        const [businessPartner, ...locations] = data.response.data;
        return { ...businessPartner, locations };
      } catch (error) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_NoCustomerForPaidReceipt'
        });
      }
    };
    const getBusinessPartner = async () => {
      const getRemoteBusinessPartner = async () => {
        const remoteBusinessPartner = await OB.App.DAL.remoteGet(
          'BusinessPartner',
          payload.ticket.bp
        );
        return remoteBusinessPartner;
      };
      const getLocalBusinessPartner = async () => {
        const localBusinessPartner = await OB.App.MasterdataModels.BusinessPartner.withId(
          payload.ticket.bp
        );
        return localBusinessPartner;
      };
      const businessPartner =
        (isRemoteCustomer
          ? await getRemoteBusinessPartner()
          : await getLocalBusinessPartner()) ||
        (await getBusinessPartnerFromBackoffice());
      return businessPartner;
    };
    const getBusinessPartnerLocation = async () => {
      const getRemoteBusinessPartnerLocation = async () => {
        const remoteBusinessPartnerLocation =
          payload.ticket.bpLocId === payload.ticket.bpBillLocId
            ? await OB.App.DAL.remoteGet('BPLocation', payload.ticket.bpLocId)
            : await OB.App.DAL.remoteSearch('BPLocation', {
                remoteFilters: [
                  {
                    columns: ['id'],
                    operator: 'equals',
                    value: [payload.ticket.bpLocId, payload.ticket.bpBillLocId]
                  }
                ]
              });
        return Array.isArray(remoteBusinessPartnerLocation)
          ? remoteBusinessPartnerLocation
          : [remoteBusinessPartnerLocation];
      };
      const getLocalBusinessPartnerLocation = async () => {
        const localBusinessPartnerLocation =
          payload.ticket.bpLocId === payload.ticket.bpBillLocId
            ? await OB.App.MasterdataModels.BusinessPartnerLocation.withId(
                payload.ticket.bpLocId
              )
            : await OB.App.MasterdataModels.BusinessPartnerLocation.find(
                new OB.App.Class.Criteria()
                  .criterion(
                    'id',
                    [payload.ticket.bpLocId, payload.ticket.bpBillLocId],
                    'in'
                  )
                  .build()
              );
        return Array.isArray(localBusinessPartnerLocation)
          ? localBusinessPartnerLocation
          : [localBusinessPartnerLocation];
      };
      const businessPartnerLocation =
        (isRemoteCustomer
          ? await getRemoteBusinessPartnerLocation()
          : await getLocalBusinessPartnerLocation()) ||
        (await getBusinessPartnerFromBackoffice().locations);
      return businessPartnerLocation;
    };

    const newPayload = { ...payload };
    if (newPayload.ticket.externalBusinessPartnerReference) {
      newPayload.ticket.externalBusinessPartner = await OB.App.ExternalBusinessPartnerAPI.getBusinessPartner(
        newPayload.ticket.externalBusinessPartnerReference
      );
      newPayload.ticket.externalBusinessPartner = newPayload.ticket.externalBusinessPartner.getPlainObject();
    }
    newPayload.ticket.businessPartner = await getBusinessPartner();
    newPayload.ticket.businessPartner.locations =
      newPayload.ticket.businessPartner.locations ||
      (await getBusinessPartnerLocation());

    return newPayload;
  },

  /**
   * Load lines information for ticket defined in the payload
   *
   * @param {object} payload - The payload with the ticket for which lines need to be loaded
   *
   * @returns {object} the payload with the ticket including lines information
   */
  async loadLines(payload) {
    const isRemoteProduct = OB.App.Security.hasPermission(
      'OBPOS_remote.product'
    );
    const { priceIncludesTax } = payload.ticket;
    const getProduct = async line => {
      const getRemoteProduct = async productId => {
        const remoteProduct = await OB.App.DAL.remoteGet('Product', productId);
        return remoteProduct;
      };
      const getLocalProduct = async productId => {
        const localProduct = await OB.App.MasterdataModels.Product.withId(
          productId
        );
        return localProduct;
      };
      const getProductFromBackoffice = async (lineId, productId) => {
        try {
          const data = await OB.App.Request.mobileServiceRequest(
            'org.openbravo.retail.posterminal.master.LoadedProduct',
            {
              salesOrderLineId: lineId,
              productId
            }
          );
          return data.response.data[0];
        } catch (error) {
          throw new OB.App.Class.ActionCanceled({
            errorConfirmation: 'OBPOS_NoReceiptLoadedText'
          });
        }
      };
      const product =
        (isRemoteProduct
          ? await getRemoteProduct(line.id)
          : await getLocalProduct(line.id)) ||
        (await getProductFromBackoffice(line.lineId, line.id));
      return product;
    };
    const getService = async product => {
      const getRemoteService = async () => {
        try {
          const data = await OB.App.Request.mobileServiceRequest(
            'org.openbravo.retail.posterminal.process.HasServices',
            {
              product: product.id,
              productCategory: product.productCategory,
              parameters: {
                terminalTime: new Date(),
                terminalTimeOffset: new Date().getTimezoneOffset()
              },
              remoteFilters: [
                {
                  columns: [],
                  operator: 'filter',
                  value: 'OBRDM_DeliveryServiceFilter',
                  params: [false]
                }
              ]
            }
          );
          return data.response.data.hasservices;
        } catch (error) {
          return false;
        }
      };
      const getLocalService = async () => {
        try {
          const criteria = await OB.App.StandardFilters.Services.apply({
            productId: product.id,
            productCategory: product.productCategory
          });
          const data = await OB.App.MasterdataModels.Product.find(
            criteria.criterion('obrdmIsdeliveryservice', false).build()
          );
          return data.length > 0;
        } catch (error) {
          return false;
        }
      };

      const service = isRemoteProduct
        ? await getRemoteService()
        : await getLocalService();
      return service;
    };

    const isBOM = product => {
      return (
        OB.Taxes.Pos.taxCategoryBOM.find(
          taxCategory => taxCategory.id === product.taxCategory
        ) && !product.productBOM
      );
    };

    const getAttributeDescription = line => {
      const attributeInfo = {
        description: '',
        keyValue: []
      };
      if (
        OB.App.Security.hasPermission(
          'OBPOS_EnableSupportForProductAttributes'
        ) &&
        line.attributeValue
      ) {
        let attSetInstanceDescription = '';
        const attributesList = [];
        const jsonAttValues = JSON.parse(line.attributeValue);
        const standardAttributes = ['lot', 'serialno', 'guaranteedate'];
        Object.keys(jsonAttValues).forEach(key => {
          if (
            Object.prototype.hasOwnProperty.call(jsonAttValues, key) &&
            standardAttributes.indexOf(key) === -1
          ) {
            attributesList.push(jsonAttValues[key]);
          }
        });
        attributesList.sort((a, b) => a.name.localeCompare(b.name));
        standardAttributes.forEach(attribute => {
          if (
            Object.prototype.hasOwnProperty.call(jsonAttValues, attribute) &&
            jsonAttValues[attribute].label
          ) {
            attributesList.push(jsonAttValues[attribute]);
          }
        });
        attributesList.forEach(attribute => {
          if (
            attribute &&
            attribute.value &&
            (typeof attribute.value === 'string' ||
              attribute.value instanceof String) &&
            attribute.value.length > 0
          ) {
            attributeInfo.keyValue.push(
              `${attribute.label ? attribute.label : attribute.name}: ${
                attribute.value
              }`
            );
            attSetInstanceDescription += `_${attribute.value}`;
          }
        });
        if (attributeInfo.keyValue.length > 0) {
          attributeInfo.description = attSetInstanceDescription.substring(1);
        }
      }
      return attributeInfo;
    };

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
          grossUnitAmount: priceIncludesTax
            ? OB.DEC.mul(bomLine.bomprice, bomLine.bomquantity)
            : undefined,
          netUnitAmount: priceIncludesTax
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

    const lines = await Promise.all(
      payload.ticket.receiptLines.map(async line => {
        if (line.product) {
          return line;
        }
        const newLine = { ...line };
        const product = await getProduct(newLine);
        if (newLine.obposExtendProductModel) {
          Object.keys(line.obposExtendProductModel).forEach(prodModelKey => {
            product[prodModelKey] = line.obposExtendProductModel[prodModelKey];
          });
        }
        delete newLine.obposExtendProductModel;
        const hasRelatedServices =
          newLine.quantity <= 0 || product.productType === 'S'
            ? false
            : await getService(product);
        product.img = undefined;

        if (isBOM(product)) {
          if (!product.productBOM) {
            const productBOM = await getProductBOM(product.id);
            if (productBOM) {
              product.productBOM = productBOM;
            }
          }
        }

        const attributeInfo = getAttributeDescription(line);
        if (attributeInfo.description) {
          newLine.attSetInstanceDesc = attributeInfo.description;
        }

        newLine.id = newLine.lineId;
        newLine.product = product;
        newLine.hasRelatedServices = hasRelatedServices;
        return newLine;
      })
    );
    return { ...payload, ticket: { ...payload.ticket, receiptLines: lines } };
  },

  /**
   * Adds business partner information as needed by ticket model from the payload to the ticket
   *
   * @param {object} ticket - The ticket for which business partner information needs to be added
   * @param {object} payload - The payload from where business partner information is read
   *
   * @returns {object} the ticket with the business partner information
   */
  addBusinessPartner(ticket, payload) {
    const shippingLocation = payload.ticket.businessPartner.locations[0];
    const invoicingLocation = payload.ticket.businessPartner.locations[1];
    return {
      ...ticket,
      businessPartner: {
        ...payload.ticket.businessPartner,
        shipLocId: shippingLocation.id,
        shipLocName: shippingLocation.name,
        shipPostalCode: shippingLocation.postalCode,
        shipCityName: shippingLocation.cityName,
        shipCountryId: shippingLocation.countryId,
        shipCountryName: shippingLocation.countryName,
        shipRegionId: shippingLocation.regionId,
        locId: (invoicingLocation || shippingLocation).id,
        locName: (invoicingLocation || shippingLocation).name,
        postalCode: (invoicingLocation || shippingLocation).postalCode,
        cityName: (invoicingLocation || shippingLocation).cityName,
        countryId: (invoicingLocation || shippingLocation).countryId,
        countryName: (invoicingLocation || shippingLocation).countryName,
        regionId: (invoicingLocation || shippingLocation).regionId,
        locationModel: shippingLocation,
        locationBillModel: invoicingLocation
      }
    };
  },

  setLoadedTicketAttributes(ticket) {
    return {
      ...ticket,
      hasbeenpaid: ticket.isQuotation ? 'Y' : 'N',
      isEditable: false,
      isPaid: !ticket.isLayaway,
      isModified: false
    };
  }
});
