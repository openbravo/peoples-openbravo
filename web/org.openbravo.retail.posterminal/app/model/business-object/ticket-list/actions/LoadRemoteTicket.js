/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Declares a global action that loads from backoffice the Ticket passed as payload as the current active ticket
 * and enqueues the active ticket into the list
 */
(() => {
  OB.App.StateAPI.Global.registerAction(
    'loadRemoteTicket',
    (globalState, payload) => {
      const newGlobalState = { ...globalState };

      if (payload.ticketInSession) {
        if (payload.ticketInSession.id === newGlobalState.Ticket.id) {
          return newGlobalState;
        }
        newGlobalState.TicketList = [
          { ...newGlobalState.Ticket },
          ...newGlobalState.TicketList.filter(
            ticket => ticket.id !== payload.ticketInSession.id
          )
        ];
        newGlobalState.Ticket = payload.ticketInSession;
        return newGlobalState;
      }

      let newTicket = createTicket(payload);
      newTicket = OB.App.State.Ticket.Utils.addBusinessPartner(
        newTicket,
        payload
      );
      newTicket = addPayments(newTicket, payload);
      newTicket = addLines(newTicket, payload);

      newGlobalState.TicketList = [
        { ...newGlobalState.Ticket },
        ...newGlobalState.TicketList
      ];
      newGlobalState.Ticket = newTicket;

      return newGlobalState;
    }
  );

  function createTicket(payload) {
    let newTicket = {
      ...payload.ticket,
      id: payload.ticket.orderid,
      timezoneOffset: new Date().getTimezoneOffset(),
      isbeingprocessed: 'N',
      hasbeenpaid: payload.ticket.isQuotation ? 'Y' : 'N',
      isEditable: false,
      isModified: false,
      generateInvoice: payload.ticket.generateInvoice || false,
      fullInvoice: payload.ticket.fullInvoice || false,
      orderDate: new Date(payload.ticket.orderDate).toISOString(),
      creationDate: new Date(payload.ticket.creationDate).toISOString(),
      updatedBy: payload.orgUserId,
      paidPartiallyOnCredit: false,
      paidOnCredit: false,
      session: payload.session,
      skipApplyPromotions: true,
      print: true,
      grossAmount: payload.ticket.totalamount,
      netAmount: payload.ticket.totalNetAmount,
      approvals: [],
      taxes: payload.ticket.receiptTaxes,
      payments: [],
      lines: [],
      orderType:
        payload.ticket.documentType ===
        payload.terminal.terminalType.documentTypeForReturns
          ? 1
          : 0
    };
    newTicket = OB.App.State.Ticket.Utils.setLoadedTicketAttributes(newTicket);
    newTicket.taxes = newTicket.taxes
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

    return newTicket;
  }

  function addPayments(ticket, payload) {
    const payments = payload.ticket.receiptPayments.map(payment => {
      const newPayment = {
        ...payment,
        date: new Date(payment.paymentDate),
        paymentDate: new Date(payment.paymentDate).toISOString(),
        orderGross: ticket.grossAmount,
        origAmount: OB.DEC.Zero,
        isPaid: !ticket.isLayaway
      };

      const reversedPayment = payload.ticket.receiptPayments.find(
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
    });

    const newTicket = OB.App.State.Ticket.Utils.adjustPayments(
      {
        ...ticket,
        change: OB.DEC.Zero,
        isPaid: !ticket.isLayaway,
        obposPrepaymentlimitamt: OB.DEC.Zero,
        payments: payments.reduce(
          (accumulator, payment) => {
            if (payment.reversedPaymentId) {
              // Move reverse payments inmediatly after their reversed payment
              accumulator.splice(
                accumulator.findIndex(
                  p => p.paymentId === payment.reversedPaymentId
                ),
                0,
                ...accumulator.splice(
                  accumulator.findIndex(p => p.paymentId === payment.paymentId),
                  1
                )
              );
            }
            return accumulator;
          },
          [...payments]
        )
      },
      payload
    );

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
  }

  function addLines(ticket, payload) {
    const newTicket = {
      ...ticket
    };
    const calculateGrossAmount = (lineGrossAmount, promotions) => {
      return (
        lineGrossAmount -
        promotions.reduce(
          (sumDiscounts, promotion) => sumDiscounts + promotion.amt,
          0
        )
      );
    };
    const calculateUnitAmountWithoutTicketDiscounts = (
      baseUnitPrice,
      promotions
    ) => {
      const ticketDiscountsDiscountTypeIds = OB.Discounts.Pos.getTicketDiscountsDiscountTypeIds();
      const discountedWithoutTicketDiscounts = promotions.reduce(
        (discounted, promotion) => {
          if (
            ticketDiscountsDiscountTypeIds.indexOf(promotion.discountType) !==
            -1
          ) {
            return discounted;
          }

          return OB.DEC.add(discounted, promotion.amt);
        },
        OB.DEC.Zero
      );

      return OB.DEC.sub(baseUnitPrice, discountedWithoutTicketDiscounts);
    };
    const calculateUnitPriceWithoutTicketDiscounts = (unitAmount, quantity) => {
      return quantity === 0 ? 0 : OB.DEC.div(unitAmount, quantity);
    };
    const newLines = payload.ticket.receiptLines.map((line, index) => ({
      ...line,
      linepos: index,
      qty: OB.DEC.number(line.quantity, line.product.uOMstandardPrecision),
      netListPrice: line.listPrice,
      grossListPrice: line.grossListPrice,
      baseNetUnitPrice: line.baseNetUnitPrice,
      baseGrossUnitPrice: line.baseGrossUnitPrice,
      netUnitPrice: line.unitPrice,
      grossUnitPrice: line.grossUnitPrice,
      baseNetUnitAmount: line.lineNetAmount,
      baseGrossUnitAmount: line.lineGrossAmount,
      netUnitAmount: line.lineNetAmount,
      grossUnitAmount: calculateGrossAmount(
        line.lineGrossAmount,
        line.promotions
      ),
      grossUnitAmountWithoutTicketDiscounts: calculateUnitAmountWithoutTicketDiscounts(
        line.lineGrossAmount,
        line.promotions
      ),
      grossUnitPriceWithoutTicketDiscounts: calculateUnitPriceWithoutTicketDiscounts(
        calculateUnitAmountWithoutTicketDiscounts(
          line.lineGrossAmount,
          line.promotions
        ),
        OB.DEC.number(line.quantity, line.product.uOMstandardPrecision)
      ),
      priceIncludesTax: ticket.priceIncludesTax,
      warehouse: {
        id: line.warehouse,
        warehousename: line.warehousename
      },
      groupService: line.product.groupProduct,
      isEditable: true,
      isDeletable: true,
      // eslint-disable-next-line no-nested-ternary
      country: payload.orgLocation
        ? payload.orgLocation.country
        : line.organization
        ? line.organization.country
        : payload.terminal.organizationCountryId,
      // eslint-disable-next-line no-nested-ternary
      region: payload.orgLocation
        ? payload.orgLocation.region
        : line.organization
        ? line.organization.region
        : payload.terminal.organizationRegionId,
      destinationCountry:
        // eslint-disable-next-line no-nested-ternary
        line.obrdmDeliveryMode === 'HomeDelivery'
          ? ticket.businessPartner.shipLocId
            ? ticket.businessPartner.locationModel.countryId
            : null
          : // eslint-disable-next-line no-nested-ternary
          payload.orgLocation
          ? payload.orgLocation.country
          : line.organization
          ? line.organization.country
          : payload.terminal.organizationCountryId,
      destinationRegion:
        // eslint-disable-next-line no-nested-ternary
        line.obrdmDeliveryMode === 'HomeDelivery'
          ? ticket.businessPartner.shipLocId
            ? ticket.businessPartner.locationModel.regionId
            : null
          : // eslint-disable-next-line no-nested-ternary
          payload.orgLocation
          ? payload.orgLocation.region
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
    newTicket.lines = [...newTicket.lines, ...newLines];
    newTicket.qty = newTicket.lines.reduce(
      (accumulator, line) =>
        line.qty > 0 ? OB.DEC.add(accumulator, line.qty) : accumulator,
      OB.DEC.Zero
    );

    const hasDeliveredProducts = newTicket.lines.some(
      line => line.deliveredQuantity && line.deliveredQuantity >= line.quantity
    );
    const hasNotDeliveredProducts = newTicket.lines.some(
      line => !line.deliveredQuantity || line.deliveredQuantity < line.quantity
    );
    newTicket.isPartiallyDelivered =
      hasDeliveredProducts && hasNotDeliveredProducts;
    newTicket.isFullyDelivered =
      hasDeliveredProducts && !hasNotDeliveredProducts;

    if (newTicket.isPartiallyDelivered) {
      newTicket.deliveredQuantityAmount = newTicket.lines.reduce(
        (accumulator, line) =>
          OB.DEC.add(
            accumulator,
            OB.DEC.mul(
              line.deliveredQuantity || OB.DEC.Zero,
              line.grossUnitPrice
            )
          ),
        OB.DEC.Zero
      );
      if (
        newTicket.deliveredQuantityAmount &&
        newTicket.deliveredQuantityAmount > newTicket.payment
      ) {
        newTicket.isDeliveredGreaterThanGross = true;
      }
    }

    return newTicket;
  }

  OB.App.StateAPI.Global.loadRemoteTicket.addActionPreparation(
    async (globalState, payload) => {
      let newPayload = { ...payload };

      newPayload = await checkSession(newPayload);
      if (!newPayload.ticketInSession) {
        newPayload = await checkCrossStore(newPayload);
        newPayload = await OB.App.State.Ticket.Utils.loadTicket(newPayload);
        newPayload = await OB.App.State.Ticket.Utils.loadBusinessPartner(
          newPayload
        );
        newPayload = await OB.App.State.Ticket.Utils.loadLines(newPayload);
      }

      return newPayload;
    }
  );

  async function checkSession(payload) {
    const ticketInSession = OB.App.State.TicketList.Utils.getSessionTickets(
      payload.session
    ).find(
      ticket =>
        (payload.ticket.id &&
          (ticket.id === payload.ticket.id ||
            ticket.oldId === payload.ticket.id ||
            (ticket.canceledorder || {}).id === payload.ticket.id)) ||
        (payload.ticket.documentNo &&
          (ticket.documentNo === payload.ticket.documentNo ||
            (ticket.canceledorder || {}).documentNo ===
              payload.ticket.documentNo))
    );

    if (ticketInSession) {
      if (
        ticketInSession.oldId &&
        payload.ticket.id &&
        ticketInSession.oldId === payload.ticket.id
      ) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_OrderAssociatedToQuotationInProgress',
          messageParams: [
            payload.ticket.documentNo,
            ticketInSession.documentNo,
            payload.ticket.documentNo,
            ticketInSession.documentNo
          ]
        });
      }

      if (
        ticketInSession.canceledorder &&
        ((payload.ticket.id &&
          ticketInSession.canceledorder.id === payload.ticket.id) ||
          (payload.ticket.documentNo &&
            ticketInSession.canceledorder.documentNo ===
              payload.ticket.documentNo))
      ) {
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_OrderAssociatedToCancelInProgress',
          messageParams: [payload.ticket.documentNo, ticketInSession.documentNo]
        });
      }

      await OB.App.View.DialogUIHandler.askConfirmation({
        message: `OBPOS_ticketAlreadyOpened_ORD`,
        messageParams: [ticketInSession.documentNo],
        hideCancel: true
      });
    }

    return { ...payload, ticketInSession };
  }

  async function checkCrossStore(payload) {
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
  }

  OB.App.StateAPI.Global.loadRemoteTicket.addActionPreparation(
    async (ticket, payload) => {
      const orgLocationCountry = OB.UTIL.localStorage.getItem(
        'orglocation_countryid'
      );
      if (orgLocationCountry == null) {
        return payload;
      }
      const newPayload = { ...payload };
      newPayload.orgLocation = {
        country: orgLocationCountry,
        region: OB.UTIL.localStorage.getItem('orglocation_regionid')
      };

      return newPayload;
    }
  );
})();
