/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* eslint-disable no-use-before-define */

/**
 * @fileoverview Declares a global action that loads the Ticket passed as payload as the current active ticket
 * and enqueues the active ticket into the list
 */
(() => {
  OB.App.StateAPI.Global.registerAction(
    'loadTicket',
    (globalState, payload) => {
      const newGlobalState = { ...globalState };

      let newTicket = createTicket(payload);
      newTicket = addBusinessPartner(newTicket);
      newTicket = addPayments(newTicket, payload);
      newTicket = addProducts(newTicket, payload);

      newGlobalState.TicketList = [
        { ...newGlobalState.Ticket },
        ...newGlobalState.TicketList
      ];
      newGlobalState.Ticket = newTicket;

      return newGlobalState;
    }
  );

  const createTicket = payload => {
    const newTicket = {
      ...payload.ticket,
      id: payload.ticket.orderid,
      isbeingprocessed: 'N',
      hasbeenpaid: payload.ticket.isQuotation ? 'Y' : 'N',
      isEditable: false,
      isModified: false,
      generateInvoice: payload.ticket.generateInvoice || false,
      fullInvoice: payload.ticket.fullInvoice || false,
      orderDate: OB.I18N.normalizeDate(payload.ticket.orderDate),
      creationDate: OB.I18N.normalizeDate(payload.ticket.creationDate),
      updatedBy: payload.orgUserId,
      paidPartiallyOnCredit: false,
      paidOnCredit: false,
      session: payload.session,
      skipApplyPromotions: true,
      payments: payload.ticket.receiptPayments,
      grossAmount: payload.ticket.totalamount,
      netAmount: payload.ticket.totalNetAmount,
      approvals: [],
      orderType:
        payload.ticket.documentType ===
        payload.terminal.terminalType.documentTypeForReturns
          ? 1
          : 0
    };

    newTicket.taxes = payload.ticket.receiptTaxes
      .map(tax => ({
        id: tax.taxid,
        net: tax.net,
        amount: tax.amount,
        name: tax.name,
        docTaxAmount: tax.docTaxAmount,
        rate: tax.rate,
        cascade: tax.cascade,
        lineNo: tax.lineNo
      }))
      .reduce((obj, item) => {
        const newObj = { ...obj };
        newObj[[item.id]] = item;
        return newObj;
      }, {});

    // TODO
    // if (newTicket.isQuotation) {
    //   newTicket.oldId = newTicket.orderid;
    //   newTicket.documentType =
    //     payload.terminal.terminalType.documentTypeForQuotations;
    // }

    return newTicket;
  };

  const addBusinessPartner = ticket => {
    const shippingLocation = ticket.businessPartner.locations[0];
    const invoicingLocation = ticket.businessPartner.locations[1];
    return {
      ...ticket,
      businessPartner: {
        ...ticket.businessPartner,
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
        countryName: (invoicingLocation || shippingLocation).countryName,
        regionId: (invoicingLocation || shippingLocation).regionId,
        locationModel: shippingLocation,
        locationBillModel: invoicingLocation
      }
    };
  };

  const addPayments = (ticket, payload) => {
    const newTicket = OB.App.State.Ticket.Utils.adjustPayments(
      {
        ...ticket,
        change: OB.DEC.Zero,
        isPaid: !ticket.isLayaway
      },
      payload
    );

    newTicket.payments = newTicket.payments
      .map(payment => {
        const newPayment = {
          ...payment,
          date: new Date(payment.paymentDate),
          paymentDate: new Date(payment.paymentDate).toISOString(),
          orderGross: newTicket.grossAmount,
          origAmount: OB.DEC.Zero,
          isPaid: newTicket.isPaid
        };

        const reversedPayment = newTicket.payments.find(
          p => p.reversedPaymentId === payment.paymentId
        );
        if (payment.isReversed) {
          newPayment.reversedPaymentId = undefined;
        }
        if (reversedPayment) {
          newPayment.reversedPaymentId = reversedPayment.paymentId;
          newPayment.isReversePayment = true;
        }

        return newPayment;
      })
      .sort((payment1, payment2) => {
        if (payment1.paymentId === payment2.reversedPaymentId) {
          return -1;
        }
        if (payment1.reversedPaymentId === payment2.paymentId) {
          return 1;
        }
        return payment1;
      });

    if (!newTicket.isQuotation && !newTicket.isLayaway) {
      const paidByPayments = newTicket.payments.reduce(
        (accumulator, payment) =>
          OB.DEC.add(accumulator, OB.DEC.mul(payment.amount, payment.rate)),
        OB.DEC.Zero
      );
      const creditAmount = OB.DEC.sub(newTicket.grossAmount, paidByPayments);
      if (
        OB.DEC.compare(newTicket.grossAmount) > 0 &&
        OB.DEC.compare(creditAmount) > 0
      ) {
        newTicket.creditAmount = creditAmount;
        if (paidByPayments) {
          newTicket.paidPartiallyOnCredit = true;
        }
        newTicket.paidOnCredit = true;
      } else if (
        (newTicket.totalamount > 0 &&
          newTicket.totalamount > newTicket.payment) ||
        (newTicket.totalamount < 0 &&
          (newTicket.payment === 0 ||
            OB.DEC.abs(newTicket.totalamount) > newTicket.payment))
      ) {
        newTicket.paidOnCredit = true;
      }
    }

    return newTicket;
  };

  const addProducts = (ticket, payload) => {
    // if (iter.relatedLines && !order.get('hasServices')) {
    //   order.set('hasServices', true);
    // }

    //
    //     for (let promotion of iter.promotions) {
    //       try {
    //         const discount = OB.Discounts.Pos.manualRuleImpls.find(
    //           discount => discount.id === promotion.ruleId
    //         );
    //         if (
    //           discount &&
    //           OB.Discounts.Pos.getManualPromotions().includes(
    //             discount.discountType
    //           )
    //         ) {
    //           var percentage;
    //           if (discount.obdiscPercentage) {
    //             percentage = OB.DEC.mul(
    //               OB.DEC.div(promotion.amt, iter.lineGrossAmount),
    //               new BigDecimal('100')
    //             );
    //           }
    //           promotion.userAmt = percentage ? percentage : promotion.amt;
    //           promotion.discountType = discount.discountType;
    //           promotion.manual = true;
    //         }
    //       } catch (error) {
    //         OB.UTIL.showError(error);
    //       }
    //     }
    //
    //     if (
    //       OB.MobileApp.model.hasPermission(
    //         'OBPOS_EnableSupportForProductAttributes',
    //         true
    //       )
    //     ) {
    //       if (iter.attributeValue && _.isString(iter.attributeValue)) {
    //         var processedAttValues = OB.UTIL.AttributeUtils.generateDescriptionBasedOnJson(
    //           iter.attributeValue
    //         );
    //         if (
    //           processedAttValues &&
    //           processedAttValues.keyValue &&
    //           _.isArray(processedAttValues.keyValue) &&
    //           processedAttValues.keyValue.length > 0
    //         ) {
    //           iter.attSetInstanceDesc = processedAttValues.description;
    //         }
    //       }
    //     }
    //

    //       order.set(
    //         'isPartiallyDelivered',
    //         hasDeliveredProducts && hasNotDeliveredProducts ? true : false
    //       );
    //       if (hasDeliveredProducts && !hasNotDeliveredProducts) {
    //         order.set('isFullyDelivered', true);
    //       }
    //       if (order.get('isPartiallyDelivered')) {
    //         var partiallyPaid = 0;
    //         _.each(
    //           _.filter(order.get('receiptLines'), function(reciptLine) {
    //             return reciptLine.deliveredQuantity;
    //           }),
    //           function(deliveredLine) {
    //             partiallyPaid = OB.DEC.add(
    //               partiallyPaid,
    //               OB.DEC.mul(
    //                 deliveredLine.deliveredQuantity,
    //                 deliveredLine.grossUnitPrice
    //               )
    //             );
    //           }
    //         );
    //         order.set('deliveredQuantityAmount', partiallyPaid);
    //         if (
    //           order.get('deliveredQuantityAmount') &&
    //           order.get('deliveredQuantityAmount') > order.get('payment')
    //         ) {
    //           order.set('isDeliveredGreaterThanGross', true);
    //         }
    //       }
    //     }
    //   }
    // );

    const newTicket = {
      ...ticket,
      qty: payload.ticket.lines.reduce(
        (accumulator, line) => OB.DEC.add(accumulator, line.quantity),
        OB.DEC.Zero
      )
    };
    newTicket.lines = newTicket.lines.map(line => ({
      ...line,
      qty: OB.DEC.number(line.quantity, line.product.uOMstandardPrecision),
      netListPrice: line.listPrice,
      grossListPrice: line.grossListPrice,
      baseNetUnitPrice: line.standardPrice,
      baseGrossUnitPrice: line.baseGrossUnitPrice,
      netUnitPrice: line.unitPrice,
      grossUnitPrice: line.grossUnitPrice,
      baseNetUnitAmount: line.lineNetAmount,
      baseGrossUnitAmount: line.lineGrossAmount,
      netUnitAmount: line.lineNetAmount,
      grossUnitAmount: line.lineGrossAmount,
      priceIncludesTax: ticket.priceIncludesTax,
      warehouse: {
        id: line.warehouse,
        warehousename: line.warehousename
      },
      groupService: line.product.groupProduct,
      isEditable: true,
      isDeletable: true,
      country:
        // eslint-disable-next-line no-nested-ternary
        line.obrdmDeliveryMode === 'HomeDelivery'
          ? ticket.businessPartner.shipLocId
            ? ticket.businessPartner.locationModel.countryId
            : null
          : line.organization
          ? line.organization.country
          : payload.terminal.organizationCountryId,
      region:
        // eslint-disable-next-line no-nested-ternary
        line.obrdmDeliveryMode === 'HomeDelivery'
          ? ticket.businessPartner.shipLocId
            ? ticket.businessPartner.locationModel.regionId
            : null
          : line.organization
          ? line.organization.region
          : payload.terminal.organizationRegionId,
      taxes: line.taxes
        .map(tax => ({
          id: tax.taxId,
          net: tax.taxableAmount,
          amount: tax.taxAmount,
          name: tax.identifier,
          docTaxAmount: tax.docTaxAmount,
          rate: tax.taxRate,
          cascade: tax.cascade,
          lineNo: tax.lineNo
        }))
        .reduce((obj, item) => {
          const newObj = { ...obj };
          newObj[[item.id]] = item;
          return newObj;
        }, {})
    }));

    return newTicket;
  };

  OB.App.StateAPI.Global.loadTicket.addActionPreparation(
    async (globalState, payload) => {
      let newPayload = { ...payload };

      newPayload = await checkCrossStore(newPayload);
      // TODO: checkPermissions, checkSession, searchRelatedReceipts
      newPayload = await loadTicket(newPayload);
      newPayload = await loadBusinessPartner(newPayload);
      newPayload = await loadProducts(newPayload);

      return newPayload;
    }
  );

  const checkCrossStore = async payload => {
    const isCrossStore = OB.App.State.Ticket.Utils.isCrossStore(
      payload.ticket,
      payload
    );
    if (!isCrossStore) {
      return payload;
    }
    await OB.App.View.DialogUIHandler.askConfirmationWithCancel({
      title: 'OBPOS_LblCrossStorePayment',
      message: 'OBPOS_LblCrossStoreDeliveryMessage',
      messageParams: [payload.ticket.documentNo, payload.ticket.store]
    });
    return { ...payload, ticket: { ...payload.ticket, isCrossStore } };
  };

  const loadTicket = async payload => {
    const data = await OB.App.Request.mobileServiceRequest(
      'org.openbravo.retail.posterminal.PaidReceipts',
      {
        orderid: payload.ticket.id,
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
  };

  const loadBusinessPartner = async payload => {
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
        const businessPartner = await OB.App.DAL.remoteGet(
          'BusinessPartner',
          payload.ticket.bp
        );
        return businessPartner;
      };
      const getLocalBusinessPartner = async () => {
        const businessPartner = await OB.App.MasterdataModels.BusinessPartner.withId(
          payload.ticket.bp
        );
        return businessPartner;
      };
      return (
        (isRemoteCustomer
          ? await getRemoteBusinessPartner()
          : await getLocalBusinessPartner()) ||
        getBusinessPartnerFromBackoffice()
      );
    };
    const getBusinessPartnerLocation = async () => {
      const getRemoteBusinessPartnerLocation = async () => {
        const businessPartnerLocations =
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
        return Array.isArray(businessPartnerLocations)
          ? businessPartnerLocations
          : [businessPartnerLocations];
      };
      const getLocalBusinessPartnerLocation = async () => {
        const businessPartnerLocations =
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
        return Array.isArray(businessPartnerLocations)
          ? businessPartnerLocations
          : [businessPartnerLocations];
      };
      return (
        (isRemoteCustomer
          ? await getRemoteBusinessPartnerLocation()
          : await getLocalBusinessPartnerLocation()) ||
        getBusinessPartnerFromBackoffice().locations
      );
    };

    const newPayload = { ...payload };
    if (newPayload.ticket.externalBusinessPartnerReference) {
      newPayload.ticket.externalBusinessPartner = await OB.App.ExternalBusinessPartnerAPI.getBusinessPartner(
        newPayload.ticket.externalBusinessPartnerReference
      );
    }
    newPayload.ticket.businessPartner = await getBusinessPartner();
    newPayload.ticket.businessPartner.locations =
      newPayload.ticket.businessPartner.locations ||
      (await getBusinessPartnerLocation());

    return newPayload;
  };

  const loadProducts = async payload => {
    const isRemoteProduct = OB.App.Security.hasPermission(
      'OBPOS_remote.product'
    );
    const getProduct = async line => {
      const getRemoteProduct = async productId => {
        const product = await OB.App.DAL.remoteGet('Product', productId);
        return product;
      };
      const getLocalProduct = async productId => {
        const product = await OB.App.MasterdataModels.Product.withId(productId);
        return product;
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
      return (
        (isRemoteProduct
          ? await getRemoteProduct(line.id)
          : await getLocalProduct(line.id)) ||
        getProductFromBackoffice(line.lineId, line.id)
      );
    };
    const getService = async product => {
      const getRemoteService = async () => {
        try {
          const data = await OB.App.Request.mobileServiceRequest(
            'org.openbravo.retail.posterminal.process.HasServices',
            {
              product: product.productId,
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
          return data.response.data.length > 0;
        } catch (error) {
          return false;
        }
      };
      const getLocalService = async () => {
        try {
          const criteria = await OB.App.StandardFilters.Services.apply({
            productId: product.productId,
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

      return isRemoteProduct
        ? getRemoteService(product)
        : getLocalService(product);
    };

    const lines = await Promise.all(
      payload.ticket.receiptLines.map(async line => {
        const product = await getProduct(line);
        const hasRelatedServices =
          line.qty <= 0 || product.productType === 'S'
            ? false
            : await getService(product);
        return { ...line, id: line.lineId, product, hasRelatedServices };
      })
    );
    return { ...payload, ticket: { ...payload.ticket, lines } };
  };
})();
