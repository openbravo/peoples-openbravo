/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Define utility functions for the Ticket Model
 */

(function TicketUtilsDefinition() {
  // checks if the type of payment is cash
  const isCash = (paymentType, paymentTypes) => {
    const payment = paymentTypes.find(p => p.payment.searchKey === paymentType);
    if (!payment) {
      return false;
    }
    return payment.paymentMethod.iscash;
  };

  // gets the precision of a given payment
  const getPrecision = (payment, paymentTypes) => {
    const terminalPayment = paymentTypes.find(
      p => p.payment.searchKey === payment.kind
    );
    return terminalPayment
      ? terminalPayment.obposPosprecision
      : OB.DEC.getScale();
  };

  // checks if a ticket line includes service information
  const isServiceLine = line => {
    return (
      line.relatedLines &&
      line.relatedLines.length > 0 &&
      !line.originalOrderLineId
    );
  };

  /**
   * Internal helper that allows to apply changes in a ticket in a pure way
   */
  class TicketHandler {
    constructor(ticket) {
      this.ticket = { ...ticket };
    }

    getTicket() {
      return this.ticket;
    }

    createLine(payload) {
      const { product } = payload;
      const newLine = {
        id: OB.App.UUID.generate(),
        product,
        organization: this.getLineOrganization(payload),
        warehouse: {
          id: payload.warehouses[0].warehouseid,
          warehousename: payload.warehouses[0].warehousename
        },
        uOM: product.uOM,
        qty: OB.DEC.number(payload.qty, product.uOMstandardPrecision),
        priceIncludesTax: this.ticket.priceIncludesTax,
        isEditable: true,
        isDeletable: true
      };

      const isDeliveryService =
        newLine.product.obrdmIsdeliveryservice &&
        payload.deliveryPaymentMode === 'PD';
      if (isDeliveryService) {
        newLine.obrdmAmttopayindelivery = OB.DEC.mul(
          product.listPrice,
          newLine.qty
        );
      }

      if (newLine.priceIncludesTax) {
        newLine.grossListPrice = OB.DEC.number(product.listPrice);
        newLine.baseGrossUnitPrice = isDeliveryService
          ? OB.DEC.Zero
          : OB.DEC.number(product.standardPrice);
      } else {
        newLine.netListPrice = OB.DEC.number(product.listPrice);
        newLine.baseNetUnitPrice = isDeliveryService
          ? OB.DEC.Zero
          : OB.DEC.number(product.standardPrice);
      }
      this.setDeliveryMode(newLine);
      this.ticket.lines = this.ticket.lines.concat(newLine);
      return newLine;
    }

    getLineOrganization(payload) {
      if (payload.product.crossStore) {
        const store = payload.store.find(
          s => s.id === this.ticket.organization
        );
        return {
          id: store.id,
          orgName: store.name,
          country: store.country,
          region: store.region
        };
      }
      const { terminal } = payload;
      return {
        id: terminal.organization,
        orgName: terminal.organization$_identifier,
        country: terminal.organizationCountryId,
        region: terminal.organizationRegionId
      };
    }

    setDeliveryMode(line) {
      // this is an internal function used to set the delivery mode information on a newly created line
      // therefore we can safely assign properties to the line received as parameter
      if (line.product.productType !== 'S' && !line.obrdmDeliveryMode) {
        let productDeliveryMode;
        let productDeliveryDate;
        let productDeliveryTime;

        if (OB.App.State.Ticket.Utils.isLayaway(this.ticket)) {
          productDeliveryMode = line.product.obrdmDeliveryModeLyw;
        } else {
          productDeliveryMode = line.product.obrdmDeliveryMode;
          productDeliveryDate = line.product.obrdmDeliveryDate;
          productDeliveryTime = line.product.obrdmDeliveryTime;
        }

        const deliveryMode =
          productDeliveryMode ||
          this.ticket.obrdmDeliveryModeProperty ||
          'PickAndCarry';

        // eslint-disable-next-line no-param-reassign
        line.obrdmDeliveryMode = deliveryMode;

        if (
          deliveryMode === 'PickupInStoreDate' ||
          deliveryMode === 'HomeDelivery'
        ) {
          const currentDate = new Date();
          currentDate.setHours(0, 0, 0, 0);

          // eslint-disable-next-line no-param-reassign
          line.obrdmDeliveryDate = productDeliveryMode
            ? productDeliveryDate || currentDate
            : this.ticket.obrdmDeliveryDateProperty;
        }

        if (deliveryMode === 'HomeDelivery') {
          const currentTime = new Date();
          currentTime.setSeconds(0);
          currentTime.setMilliseconds(0);

          // eslint-disable-next-line no-param-reassign
          line.obrdmDeliveryTime = productDeliveryMode
            ? productDeliveryTime || currentTime
            : this.ticket.obrdmDeliveryTimeProperty;
        }
      }

      let country;
      let region;
      if (line.obrdmDeliveryMode === 'HomeDelivery') {
        const { shipLocId } = this.ticket.businessPartner;
        country = shipLocId
          ? this.ticket.businessPartner.locationModel.countryId
          : null;
        region = shipLocId
          ? this.ticket.businessPartner.locationModel.regionId
          : null;
      } else {
        country = line.organization.country;
        region = line.organization.region;
      }
      // eslint-disable-next-line no-param-reassign
      line.country = country;
      // eslint-disable-next-line no-param-reassign
      line.region = region;
    }

    deleteLine(lineId) {
      this.ticket.lines = this.ticket.lines.filter(l => l.id !== lineId);
    }

    updateServicesInformation(payload) {
      if (!this.shouldUpdateServices()) {
        return;
      }

      this.ticket.lines = this.ticket.lines.map(l =>
        isServiceLine(l) ? { ...l } : l
      );

      const serviceLines = this.ticket.lines.filter(l => isServiceLine(l));

      for (let i = 0; i < serviceLines.length; i += 1) {
        const serviceLine = serviceLines[i];
        const service = serviceLine.product;
        const { relatedLines } = serviceLine;
        const ticketRelatedLines = this.ticket.lines.filter(l =>
          relatedLines.some(rl => rl.orderlineId === l.id)
        );
        const positiveLines = ticketRelatedLines.filter(l => l.qty > 0);
        const negativeLines = ticketRelatedLines.filter(l => l.qty < 0);
        const rlp = relatedLines.filter(rl =>
          positiveLines.some(pl => pl.id === rl.orderlineId)
        );
        const rln = relatedLines.filter(rl =>
          negativeLines.some(nl => nl.id === rl.orderlineId)
        );
        let newqtyplus = positiveLines.reduce((t, l) => t + l.qty, 0);
        let newqtyminus = negativeLines.reduce((t, l) => t + l.qty, 0);
        if (service.quantityRule === 'UQ') {
          newqtyplus = newqtyplus ? 1 : 0;
          newqtyminus = newqtyminus ? -1 : 0;
        }

        const deferredLines = relatedLines.filter(rl => rl.deferred === true);
        const deferredQty = deferredLines
          .map(l => l.qty)
          .reduce((total, qty) => total + qty, 0);

        if (serviceLine.qty > 0) {
          if (newqtyminus && newqtyplus) {
            let newRlp = rlp;
            if (deferredLines.length > 0) {
              newRlp = OB.App.ArrayUtils.union(rlp, deferredLines);
              const qty = service.quantityRule === 'PP' ? deferredQty : 0;
              newqtyplus += qty;
            }
            const newLine = this.createLine({
              ...payload,
              product: service,
              qty: newqtyminus
            });
            newLine.relatedLines = rln;
            newLine.groupService = newLine.product.groupProduct;
            serviceLine.relatedLines = newRlp;
            serviceLine.qty = newqtyplus;
          } else if (newqtyminus) {
            if (deferredLines.length > 0) {
              const qty = service.quantityRule === 'PP' ? deferredQty : 1;
              const newLine = this.createLine({
                ...payload,
                product: service,
                qty
              });
              newLine.relatedLines = deferredLines;
              newLine.qty = qty;
            }
            serviceLine.relatedLines = rln;
            newqtyminus = this.adjustNotGroupedServices(
              serviceLine,
              newqtyminus,
              payload
            );
            serviceLine.qty = newqtyminus;
          } else if (newqtyplus) {
            serviceLine.relatedLines = OB.App.ArrayUtils.union(
              rlp,
              deferredLines
            );
            if (service.quantityRule === 'PP') {
              if (serviceLine.groupService) {
                newqtyplus += deferredQty;
              } else {
                newqtyplus = this.adjustNotGroupedServices(
                  serviceLine,
                  newqtyplus,
                  payload
                );
              }
            }
            serviceLine.qty = newqtyplus;
          } else if (deferredLines.length === 0) {
            this.deleteLine(serviceLine.id);
          } else {
            const qty =
              service.quantityRule === 'PP' && service.groupProduct
                ? deferredQty
                : 1;
            serviceLine.relatedLines = deferredLines;
            serviceLine.qty = qty;
          }
        } else if (newqtyminus && newqtyplus) {
          const newLine = this.createLine({
            ...payload,
            product: service,
            qty: newqtyplus
          });
          newLine.relatedLines = rlp;
          serviceLine.relatedLines = rln;
          serviceLine.qty = newqtyminus;
        } else if (newqtyplus) {
          serviceLine.relatedLines = rlp;
          newqtyplus = this.adjustNotGroupedServices(
            serviceLine,
            newqtyplus,
            payload
          );
          serviceLine.qty = newqtyplus;
        } else if (newqtyminus) {
          serviceLine.relatedLines = rln;
          newqtyminus = this.adjustNotGroupedServices(
            serviceLine,
            newqtyminus,
            payload
          );
          serviceLine.qty = newqtyminus;
        } else if (deferredLines.length === 0 && !serviceLine.obposIsDeleted) {
          this.deleteLine(serviceLine.id);
        }
        this.adjustNotDeferredRelatedLines(serviceLine, payload);
      }
    }

    shouldUpdateServices() {
      const hasServices =
        this.ticket.hasServices || this.ticket.lines.some(l => l.relatedLines);
      return hasServices && this.ticket.isEditable;
    }

    getSiblingServicesLines(productId, orderlineId) {
      return this.ticket.lines.filter(
        l =>
          isServiceLine(l) &&
          l.product.id === productId &&
          l.relatedLines[0].orderlineId === orderlineId
      );
    }

    adjustNotGroupedServices(line, qty, payload) {
      if (line.product.quantityRule === 'PP' && !line.groupService) {
        const qtyService = OB.DEC.abs(qty);
        const qtyLineServ = qty > 0 ? 1 : -1;
        const siblingServicesLines = this.getSiblingServicesLines(
          line.product.id,
          line.relatedLines[0].orderlineId
        );

        // Split/Remove services lines
        if (siblingServicesLines.length < qtyService) {
          for (
            let i = 0;
            i < qtyService - siblingServicesLines.length;
            i += 1
          ) {
            const notGroupedProduct = { ...line.product, groupProduct: false };
            const newLine = this.createLine({
              ...payload,
              product: notGroupedProduct,
              qty: qtyLineServ
            });
            newLine.relatedLines = siblingServicesLines[0].relatedLines;
            newLine.groupService = false;
          }
        } else if (siblingServicesLines.length > qtyService) {
          for (
            let i = 0;
            i < siblingServicesLines.length - qtyService;
            i += 1
          ) {
            this.deleteLine(siblingServicesLines[i].id);
          }
        }
        return qtyLineServ;
      }
      return qty;
    }

    adjustNotDeferredRelatedLines(serviceLine, payload) {
      const notDeferredLines = serviceLine.relatedLines.filter(
        rl => rl.deferred === false
      );
      if (!serviceLine.groupService && notDeferredLines.length > 1) {
        notDeferredLines.forEach(rl => {
          const ticketLine = this.ticket.lines.find(
            l => l.id === rl.orderlineId
          );
          const newLine = this.createLine({
            ...payload,
            product: serviceLine.product,
            qty: ticketLine.qty
          });
          newLine.relatedLines = [rl];
          newLine.groupService = false;
        });
        this.deleteLine(serviceLine.id);
      }
    }

    calculateTotals(payload) {
      this.calculateDiscountsAndTaxes(
        payload.discountRules,
        payload.bpSets,
        payload.taxRules
      );
      this.adjustPayment(payload.payments, payload.paymentcash);
      this.setTotalQuantity(payload.qtyScale);
    }

    calculateDiscountsAndTaxes(discountRules, bpSets, taxRules) {
      const { priceIncludesTax } = this.ticket;

      const discountsResult = OB.Discounts.Pos.applyDiscounts(
        this.ticket,
        discountRules,
        bpSets
      );

      // sets line amounts and prices and applies the discount calculation result into the ticket
      this.ticket.lines = this.ticket.lines.map(line => {
        const hasDiscounts = line.promotions && line.promotions.length > 0;
        if (line.skipApplyPromotions && hasDiscounts) {
          return line;
        }
        const discounts = line.skipApplyPromotions
          ? undefined
          : discountsResult.lines.find(l => l.id === line.id);
        const newLine = {
          ...line,
          promotions: discounts ? discounts.discounts : []
        };
        const { baseGrossUnitPrice, baseNetUnitPrice, qty } = line;
        if (priceIncludesTax) {
          newLine.baseGrossUnitAmount = OB.DEC.mul(baseGrossUnitPrice, qty);
          newLine.baseNetUnitAmount = 0;
          newLine.grossUnitPrice = discounts
            ? discounts.grossUnitPrice
            : baseGrossUnitPrice;
          newLine.grossUnitAmount = discounts
            ? discounts.grossUnitAmount
            : OB.DEC.mul(qty, baseGrossUnitPrice);
        } else {
          newLine.baseGrossUnitAmount = 0;
          newLine.baseNetUnitAmount = OB.DEC.mul(baseNetUnitPrice, qty);
          newLine.netUnitPrice = discounts
            ? discounts.netUnitPrice
            : baseNetUnitPrice;
          newLine.netUnitAmount = discounts
            ? discounts.netUnitAmount
            : OB.DEC.mul(qty, baseNetUnitPrice);
        }
        return newLine;
      });

      let taxesResult;
      try {
        taxesResult = OB.Taxes.Pos.applyTaxes(this.ticket, taxRules);
      } catch (error) {
        const lineIdWithError = error.message.substring(
          error.message.length - 32
        );
        const line =
          this.ticket.lines.find(l => l.id === lineIdWithError) ||
          this.ticket.lines[this.ticket.lines.length - 1];
        throw new OB.App.Class.ActionCanceled({
          errorConfirmation: 'OBPOS_CannotCalculateTaxes',
          // eslint-disable-next-line no-underscore-dangle
          messageParams: [line.qty, line.product._identifier]
        });
      }

      // set the tax calculation result into the ticket
      this.ticket.grossAmount = taxesResult.grossAmount;
      this.ticket.netAmount = taxesResult.netAmount;
      this.ticket.taxes = taxesResult.taxes;
      taxesResult.lines.forEach(taxLine => {
        const line = this.ticket.lines.find(l => l.id === taxLine.id);
        line.grossUnitAmount = taxLine.grossUnitAmount;
        line.netUnitAmount = taxLine.netUnitAmount;
        line.grossUnitPrice = taxLine.grossUnitPrice;
        line.netUnitPrice = taxLine.netUnitPrice;
        line.taxRate = taxLine.taxRate;
        line.tax = taxLine.tax;
        line.taxes = taxLine.taxes;
      });
    }

    setTotalQuantity(qtyScale) {
      this.ticket.qty = this.ticket.lines
        .map(l => l.qty)
        .reduce(
          (total, qty) => (qty > 0 ? OB.DEC.add(total, qty, qtyScale) : total),
          OB.DEC.Zero
        );
    }

    adjustPayment(paymentTypes, paymentCash) {
      const loadedFromBackend = this.ticket.isLayaway || this.ticket.isPaid;

      // set the payments origAmount property
      this.ticket.payments = this.ticket.payments.map(p => {
        const newPayment = this.calculateOrigAmount(p, paymentTypes);
        if (
          !p.isPrePayment &&
          this.ticket.isNegative &&
          loadedFromBackend &&
          !p.reversedPaymentId &&
          !p.signChanged
        ) {
          newPayment.signChanged = true;
          newPayment.amount = -newPayment.amount;
          newPayment.origAmount = -newPayment.origAmount;
          newPayment.paid = -newPayment.paid;
        }
        return newPayment;
      });

      // calculate the processed payments amount
      const processedPaymentsAmount = this.ticket.payments
        .filter(p => p.isPrePayment)
        .reduce(
          (t, p) => OB.DEC.add(t, p.origAmount, getPrecision(p, paymentTypes)),
          this.ticket.nettingPayment || OB.DEC.Zero
        );

      // set the 'isNegative' value
      const { grossAmount } = this.ticket;
      if (loadedFromBackend) {
        this.ticket.isNegative = OB.DEC.compare(grossAmount) === -1;
      } else if (OB.DEC.compare(grossAmount) === -1) {
        this.ticket.isNegative = processedPaymentsAmount >= grossAmount;
      } else {
        this.ticket.isNegative = processedPaymentsAmount > grossAmount;
      }

      // get cash info
      const { defaultCash, nonDefaultCash, noCash } = this.getCashInfo(
        paymentTypes,
        paymentCash
      );

      // calculate the reversed payments amount
      const reversedPaymentsAmount = this.ticket.payments
        .filter(p => !p.isPrePayment && p.isReversePayment)
        .reduce((t, p) => OB.DEC.add(t, p.origAmount), OB.DEC.Zero);

      // sum the total amount of the payments that cannot generate change or over payment
      const notModifiableAmount = OB.DEC.add(
        processedPaymentsAmount,
        reversedPaymentsAmount
      );

      // calculate payment amounts
      const totalCash = OB.DEC.add(defaultCash, nonDefaultCash);
      const totalPaid = OB.DEC.add(
        notModifiableAmount,
        OB.DEC.add(noCash, totalCash)
      );
      const total = this.ticket.prepaymentChangeMode
        ? this.ticket.obposPrepaymentamt
        : grossAmount;
      const cashPayment = this.ticket.payments.find(
        p => p.kind === paymentCash || isCash(p.kind, paymentTypes)
      );
      let cashPaid;
      if (cashPayment) {
        let payment;
        const precision = getPrecision(cashPayment, paymentTypes);
        if (this.ticket.isNegative) {
          if (OB.DEC.add(notModifiableAmount, noCash, precision) < total) {
            payment = OB.DEC.add(notModifiableAmount, noCash, precision);
            cashPaid = OB.DEC.Zero;
            this.ticket.payment = OB.DEC.abs(payment);
            this.ticket.paymentWithSign = payment;
            this.ticket.change = OB.DEC.abs(totalCash);
          } else if (totalPaid < total) {
            cashPaid = OB.DEC.sub(
              cashPayment.origAmount,
              OB.DEC.abs(OB.DEC.sub(total, totalPaid)),
              precision
            );
            this.ticket.payment = OB.DEC.abs(total);
            this.ticket.paymentWithSign = total;
            // The change value will be computed through a rounded total value, to ensure that the total plus change
            // add up to the paid amount without any kind of precission loss
            this.ticket.change = OB.DEC.abs(
              OB.DEC.sub(totalPaid, total, precision)
            );
          } else {
            cashPaid = cashPayment.origAmount;
            this.ticket.payment = OB.DEC.abs(totalPaid);
            this.ticket.paymentWithSign = totalPaid;
            this.ticket.change = OB.DEC.Zero;
          }
        } else if (OB.DEC.add(notModifiableAmount, noCash, precision) > total) {
          payment = OB.DEC.add(notModifiableAmount, noCash, precision);
          cashPaid = OB.DEC.Zero;
          this.ticket.payment = OB.DEC.abs(payment);
          this.ticket.paymentWithSign = payment;
          this.ticket.change = OB.DEC.abs(totalCash);
        } else if (totalPaid > total) {
          cashPaid = OB.DEC.sub(
            cashPayment.origAmount,
            OB.DEC.abs(OB.DEC.sub(totalPaid, total)),
            precision
          );
          this.ticket.payment = OB.DEC.abs(total);
          this.ticket.paymentWithSign = total;
          // The change value will be computed through a rounded total value, to ensure that the total plus change
          // add up to the paid amount without any kind of precission loss
          this.ticket.change = OB.DEC.abs(
            OB.DEC.sub(totalPaid, total, precision)
          );
        } else {
          cashPaid = cashPayment.origAmount;
          this.ticket.payment = OB.DEC.abs(totalPaid);
          this.ticket.paymentWithSign = totalPaid;
          this.ticket.change = OB.DEC.Zero;
        }
      } else {
        this.ticket.payment = OB.DEC.abs(totalPaid);
        this.ticket.paymentWithSign = totalPaid;
        this.ticket.change = OB.DEC.Zero;
      }

      if (cashPaid && cashPayment) {
        this.ticket.payments = this.ticket.payments.map(p => {
          if (p.kind !== cashPayment.kind) {
            return p;
          }
          return { ...p, paid: cashPaid };
        });
      }
    }

    calculateOrigAmount(payment, paymentTypes) {
      const precision = getPrecision(payment, paymentTypes);
      let origAmount;
      if (payment.rate && payment.rate !== '1') {
        origAmount = OB.DEC.div(payment.amount, payment.mulrate);

        // Here we are trying to know if the current payment is making the pending to pay 0.
        // to know that we are suming up every payments except the current one (getSumOfOrigAmounts)
        // then we substract this amount from the total (getDifferenceBetweenPaymentsAndTotal)
        // and finally we transform this difference to the foreign amount
        // if the payment in the foreign amount makes pending to pay zero, then we will ensure that the payment
        // in the default currency is satisfied
        if (
          OB.DEC.compare(
            OB.DEC.sub(
              this.getDifferenceRemovingSpecificPayment(payment, paymentTypes),
              OB.DEC.abs(payment.amount, precision),
              precision
            )
          ) === OB.DEC.Zero
        ) {
          const multiCurrencyDifference = this.getDifferenceBetweenPaymentsAndTotal(
            payment
          );
          if (
            OB.DEC.abs(payment.origAmount) !==
            OB.DEC.abs(multiCurrencyDifference)
          ) {
            origAmount = payment.changePayment
              ? OB.DEC.mul(multiCurrencyDifference, -1)
              : multiCurrencyDifference;
          }
        }
      } else {
        origAmount = payment.amount;
      }
      return { ...payment, origAmount, paid: payment.origAmount, precision };
    }

    // Returns the difference (abs) between total to pay and payments without take into account the provided payment
    getDifferenceRemovingSpecificPayment(payment, paymentTypes) {
      const precision = getPrecision(payment, paymentTypes);
      const differenceInDefaultCurrency = this.getDifferenceBetweenPaymentsAndTotal(
        payment
      );
      if (payment && payment.rate) {
        const differenceInForeingCurrency = OB.DEC.div(
          differenceInDefaultCurrency,
          payment.rate,
          precision
        );
        return differenceInForeingCurrency;
      }
      return differenceInDefaultCurrency;
    }

    // Returns the difference (abs) between total to pay and payments excluding the provided payment
    getDifferenceBetweenPaymentsAndTotal(payment) {
      return OB.DEC.abs(
        OB.DEC.sub(
          OB.DEC.abs(this.ticket.grossAmount),
          OB.DEC.sub(this.getSumOfOrigAmounts(payment), this.ticket.change)
        )
      );
    }

    // Returns a result with the sum up of every payments based on origAmount field excluding the provided payment
    getSumOfOrigAmounts(payment) {
      if (this.ticket.payments.length === 0) {
        return OB.DEC.Zero;
      }
      return this.ticket.payments
        .filter(p => !payment || p.kind !== payment.kind)
        .reduce((t, p) => OB.DEC.add(t, p.origAmount), OB.DEC.Zero);
    }

    getCashInfo(paymentTypes, paymentCash) {
      const loadedFromBackend = this.ticket.isLayaway || this.ticket.isPaid;
      return this.ticket.payments
        .filter(p => !p.isPrePayment)
        .reduce(
          (c, p) => {
            let property;
            if (p.kind === paymentCash) {
              // the default cash method
              property = 'defaultCash';
            } else if (isCash(p.kind, paymentTypes)) {
              // another cash method
              property = 'nonDefaultCash';
            } else {
              property = 'noCash';
            }
            const result = { ...c };
            result[property] =
              !this.ticket.isNegative || loadedFromBackend
                ? OB.DEC.add(c[property], p.origAmount)
                : OB.DEC.sub(c[property], p.origAmount);
            return result;
          },
          {
            defaultCash: OB.DEC.Zero,
            nonDefaultCash: OB.DEC.Zero,
            noCash: OB.DEC.Zero
          }
        );
    }
  }

  OB.App.StateAPI.Ticket.registerUtilityFunctions({
    /**
     * Computes the totals of a given ticket which include: discounts, taxes and other calculated fields.
     *
     * @param {object} ticket - The ticket whose totals will be calculated
     * @param {object} payload - The calculation payload, which include:
     *             * discountRules - The discount rules to be considered
     *             * taxRules - The tax rules to be considered
     *             * bpSets - The businessPartner sets
     *             * qtyScale - The scale of the ticket quantity (qty)
     *             * payments - The available payment types
     *             * paymentcash - Default cash payment
     * @returns The ticket with the result of the totals calculation
     */
    calculateTotals(ticket, payload) {
      const handler = new TicketHandler(ticket);
      handler.calculateTotals(payload);
      return handler.getTicket();
    },

    /**
     * Checks if a ticket is a layaway
     *
     * @param {object} ticket - The ticket to check
     * @returns {boolean} - True if the given ticket is a layaway, otherwise false is returned
     */
    isLayaway(ticket) {
      return (
        ticket.orderType === 2 ||
        ticket.orderType === 3 ||
        ticket.isLayaway === true
      );
    },

    /**
     * Checks if a ticket is a return
     *
     * @param {object} ticket - The ticket to check
     * @returns {boolean} - True if the given ticket is a return, otherwise false is returned
     */
    isReturn(ticket) {
      return ticket.orderType === 1;
    },

    /**
     * Checks whether a ticket is a sale or a return based on ticket lines.
     *
     * @param {object} ticket - The ticket to check
     * @param {object} payload - The calculation payload, which include:
     *             * preferences.salesWithOneLineNegativeAsReturns - OBPOS_SalesWithOneLineNegativeAsReturns preference value
     *
     * @returns {boolean} true in case the ticket is a sale, false in case it is a return.
     */
    isSale(ticket, payload) {
      if (!ticket.lines || !ticket.lines.length) {
        return true;
      }

      const existsPositiveLines = ticket.lines.some(line => line.qty >= 0);
      const existsNegativeLines = ticket.lines.some(line => line.qty < 0);
      return (
        !existsNegativeLines ||
        (existsPositiveLines &&
          (!payload ||
            !payload.preferences ||
            !payload.preferences.salesWithOneLineNegativeAsReturns))
      );
    },

    /**
     * Checks if a ticket is a quotation
     *
     * @param {object} ticket - The ticket to check
     * @returns {boolean} - True if the given ticket is a quotation, otherwise false is returned
     */
    isQuotation(ticket) {
      return ticket.isQuotation;
    },

    /**
     * Generates a new ticket resulting of adding a new line into the provided ticket
     *
     * @param {object} ticket - The ticket which we want to add a line
     * @param {object} payload - The information required to create the new line:
     *                              * product - The product of the new line
     *                              * qty - The quantity of the new line
     *                              * terminal: terminal information
     *                              * store: store information
     *                              * warehouses: warehouse information
     *                              * deliveryPaymentMode: delivery payment mode
     * @returns {object} - An object with the following properties:
     *                   * ticket: a new ticket result of adding the new line into the provided ticket
     *                   * line: the newly created line
     */
    createLine(ticket, payload) {
      const handler = new TicketHandler(ticket);
      const line = handler.createLine(payload);
      return { ticket: handler.getTicket(), line };
    },

    /**
     * Generates a new ticket resulting of deleting a line of the provided ticket
     *
     * @param {object} ticket - The ticket which we want to delete a line
     * @param {object} lineId - The id of the line to be deleted
     * @returns {object} - A new ticket result of deleting the line of the provided ticket
     */
    deleteLine(ticket, lineId) {
      const handler = new TicketHandler(ticket);
      handler.deleteLine(lineId);
      return handler.getTicket();
    },

    /**
     * Updates information regarding services of a ticket by handling its lines with services and their related lines
     *
     * @param {object} ticket - The ticket whose information about services should be updated
     * @param {settings} payload - Additional information which contains:
     *                              * terminal: terminal information
     *                              * store: store information
     *                              * warehouses: warehouse information
     *                              * deliveryPaymentMode: delivery payment mode
     * @returns {object} - A new ticket with the services information properly updated
     */
    updateServicesInformation(ticket, payload) {
      const handler = new TicketHandler(ticket);
      handler.updateServicesInformation(payload);
      return handler.getTicket();
    },

    /**
     * Given a ticket and the information of a product to be added on it, returns the candidate line where the new product information would be added
     *
     * @param {object} ticket - A ticket
     * @param {productInfo} productInfo - The product information that would be added to the ticket
     * @returns {object} - The line where the product information would be added or undefined if no candidate line is found
     */
    getLineToEdit(ticket, productInfo) {
      const { product, qty, options, attrs } = productInfo;
      if (product.obposScale || !product.groupProduct) {
        return undefined;
      }

      if (options.line) {
        return ticket.lines.find(l => l.id === options.line);
      }

      const attributeValue =
        attrs.attributeSearchAllowed && attrs.attributeValue;
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
    },

    /**
     * Creates a new Ticket object with preset data using the information given in the payload object
     *
     * @param {{
     *   terminal: Object,
     *   businessPartner: Object,
     *   session: string,
     *   orgUserId: string,
     *   pricelist: Object,
     *   ticketExtraProperties: Object,
     *   context: Object,
     *   preferences: Object,
     *   constants: Object }
     * } payload - The payload object with all data required to create the ticket object. See OB.UTIL.TicketUtils.addTicketCreationDataToPayload for a full initialization of this payload object
     * @return {Object} - The new Ticket object
     */
    newTicket(payload) {
      const ticket = {
        ...payload.ticketExtraProperties,
        // ticket default properties
        id: OB.App.UUID.generate(),
        orderType: 0, // 0: Sales order, 1: Return order
        orderDate: new Date().toISOString(),
        creationDate: new Date().toISOString(),
        documentNo: '',
        lines: [],
        orderManualPromotions: [],
        payments: [],
        payment: OB.DEC.Zero,
        paymentWithSign: OB.DEC.Zero,
        nettingPayment: OB.DEC.Zero,
        change: OB.DEC.Zero,
        qty: OB.DEC.Zero,
        grossAmount: OB.DEC.Zero,
        netAmount: OB.DEC.Zero,
        taxes: {},
        hasbeenpaid: 'N',
        isbeingprocessed: 'N',
        description: '',
        print: true,
        sendEmail: false,
        isPaid: false,
        creditAmount: OB.DEC.Zero,
        paidPartiallyOnCredit: false,
        paidOnCredit: false,
        isLayaway: false,
        isPartiallyDelivered: false,
        isEditable: true,
        openDrawer: false,
        approvals: [],
        obposPrepaymentamt: OB.DEC.Zero,
        obposPrepaymentlimitamt: OB.DEC.Zero,
        obposPrepaymentlaylimitamt: OB.DEC.Zero
      };

      ticket.client = payload.terminal.client;
      ticket.organization = payload.terminal.organization;
      ticket.organizationAddressIdentifier =
        payload.terminal.organizationAddressIdentifier;
      ticket.trxOrganization = payload.terminal.organization;
      ticket.createdBy = payload.orgUserId;
      ticket.updatedBy = payload.orgUserId;
      ticket.documentType = payload.terminal.terminalType.documentType;
      ticket.orderType = payload.terminal.terminalType.layawayorder ? 2 : 0; // 0: Sales order, 1: Return order, 2: Layaway, 3: Void Layaway
      ticket.generateInvoice = payload.terminal.terminalType.generateInvoice;

      ticket.isQuotation = false;
      ticket.oldId = null;
      ticket.session = payload.session;
      ticket.cashVAT = payload.terminal.cashVat;
      ticket.businessPartner = payload.businessPartner;
      ticket.invoiceTerms = payload.businessPartner.invoiceTerms;
      if (payload.preferences.enableMultiPriceList) {
        // Set price list for order
        ticket.priceList = payload.businessPartner.priceList;
        let { priceIncludesTax } = payload.businessPartner;
        if (OB.UTIL.isNullOrUndefined(priceIncludesTax)) {
          priceIncludesTax = payload.pricelist.priceIncludesTax;
        }
        ticket.priceIncludesTax = priceIncludesTax;
      } else {
        ticket.priceList = payload.terminal.priceList;
        ticket.priceIncludesTax = payload.pricelist.priceIncludesTax;
      }
      ticket.currency = payload.terminal.currency;
      ticket[
        `currency${payload.constants.fieldSeparator}${payload.constants.identifierSuffix}`
      ] =
        payload.terminal[
          `currency${payload.constants.fieldSeparator}${payload.constants.identifierSuffix}`
        ];
      ticket.warehouse = payload.terminal.warehouse;
      if (payload.context.user.isSalesRepresentative) {
        ticket.salesRepresentative = payload.context.user.id;
        ticket[
          `salesRepresentative${payload.constants.fieldSeparator}${payload.constants.identifierSuffix}`
          // eslint-disable-next-line no-underscore-dangle
        ] = payload.context.user._identifier;
      } else {
        ticket.salesRepresentative = null;
        ticket[
          `salesRepresentative${payload.constants.fieldSeparator}${payload.constants.identifierSuffix}`
        ] = null;
      }
      ticket.posTerminal = payload.terminal.id;
      ticket[
        `posTerminal${payload.constants.fieldSeparator}${payload.constants.identifierSuffix}`
        // eslint-disable-next-line no-underscore-dangle
      ] = payload.terminal._identifier;

      if (payload.preferences.enableDeliveryModes) {
        ticket.obrdmDeliveryModeProperty = 'PickAndCarry';
      }

      return ticket;
    },

    /**
     * Given a ticket, returns true if there is at least one payment created in this session (so not coming from the backend)
     *
     * @param {object} ticket - A ticket
     * @returns {object} - true if there is at least one payment created in the current session
     */
    checkTicketPayments(ticket) {
      if (!ticket.receiptPayments) {
        return ticket.payments && ticket.payments.length > 0;
      }
      return ticket.payments.length > ticket.receiptPayments.length;
    },

    /**
     * Given a ticket line, returns the discounted line price (so the price resulting from applying all current discounts)
     *
     * @param {object} line - A ticket line
     * @param {ignoreExecutedAtTheEndPromo} boolean - if true, discounts executed at the end of the process are not considered for the calculation
     * @returns {object} - The discounted line price
     */
    getCurrentDiscountedLinePrice(line, ignoreExecutedAtTheEndPromo) {
      let currentDiscountedLinePrice;
      let allDiscountedAmt = OB.DEC.Zero;
      let i = 0;
      if (line.promotions) {
        for (i = 0; i < line.promotions.length; i += 1) {
          if (!line.promotions[i].hidden) {
            if (
              !ignoreExecutedAtTheEndPromo ||
              !line.promotions[i].executedAtTheEndPromo
            ) {
              allDiscountedAmt = OB.DEC.add(
                allDiscountedAmt,
                line.promotions[i].amt
              );
            }
          }
        }
      }
      if (allDiscountedAmt > OB.DEC.Zero && line.qty > 0) {
        currentDiscountedLinePrice = OB.DEC.sub(
          line.baseGrossUnitPrice,
          OB.DEC.div(allDiscountedAmt, line.qty, OB.DEC.getRoundingMode()),
          OB.DEC.getRoundingMode()
        );
      } else {
        currentDiscountedLinePrice = line.baseGrossUnitPrice;
      }

      return currentDiscountedLinePrice;
    },

    /**
     * Given a ticket line, returns a new line with the discounted price correctly calculated
     *
     * @param {object} line - A ticket line
     * @returns {object} - A new line with the discounted price correctly calculated
     */
    calculateDiscountedLinePrice(line) {
      const newLine = { ...line };
      if (line.qty === 0) {
        delete newLine.discountedLinePrice;
      } else {
        newLine.discountedLinePrice = OB.App.State.Ticket.Utils.getCurrentDiscountedLinePrice(
          newLine,
          false
        );
      }
      return newLine;
    },

    /**
     * Checks whether a ticket belongs to a different store.
     *
     * @param {object} ticket - The ticket to check
     * @param {object} payload - The calculation payload, which include:
     *             * terminal.organization - Organization of the current terminal
     *
     * @returns {boolean} true in case the ticket is cross store, false otherwise.
     */
    isCrossStore(ticket, payload) {
      if (
        !ticket.organization ||
        !payload ||
        !payload.terminal ||
        !payload.terminal.organization
      ) {
        return false;
      }

      return ticket.organization !== payload.terminal.organization;
    },

    /**
     * Checks whether a ticket is fully paid.
     *
     * @param {object} ticket - The ticket whose payment will be checked
     *
     * @returns {boolean} true in case the ticket is fully paid, false in case it is not paid or it is partially paid.
     */
    isFullyPaid(ticket) {
      return ticket.payment >= OB.DEC.abs(ticket.grossAmount);
    },

    /**
     * Returns ticket payment status.
     *
     * @param {object} ticket - The ticket whose payment status will be retrieved
     * @param {object} payload - The calculation payload, which include:
     *             * preferences.salesWithOneLineNegativeAsReturns - OBPOS_SalesWithOneLineNegativeAsReturns preference value
     *
     * @returns {object} Ticket payment status.
     */
    getPaymentStatus(ticket, payload) {
      if (payload && payload.multiTicketList) {
        return {
          total: ticket.total,
          pending:
            OB.DEC.compare(OB.DEC.sub(ticket.payment, ticket.total)) >= 0
              ? OB.DEC.Zero
              : OB.DEC.sub(ticket.total, ticket.payment),
          overpayment:
            OB.DEC.compare(OB.DEC.sub(ticket.payment, ticket.total)) > 0
              ? OB.DEC.sub(ticket.payment, ticket.total)
              : OB.DEC.Zero,
          isReturn: ticket.gross < 0,
          isNegative: ticket.gross < 0,
          totalAmt: ticket.total,
          pendingAmt:
            OB.DEC.compare(OB.DEC.sub(ticket.payment, ticket.total)) >= 0
              ? OB.DEC.Zero
              : OB.DEC.sub(ticket.total, ticket.payment),
          payments: ticket.payments
        };
      }

      const isSale = OB.App.State.Ticket.Utils.isSale(ticket, payload);
      const isReversal = ticket.payments.some(
        payment => payment.reversedPaymentId
      );
      const paymentsAmount = ticket.payments.reduce((total, payment) => {
        if (
          payment.isPrePayment ||
          ticket.isLayaway ||
          ticket.isPaid ||
          !ticket.isNegative ||
          payment.isReversePayment
        ) {
          return OB.DEC.add(total, payment.origAmount);
        }
        return OB.DEC.sub(total, payment.origAmount);
      }, OB.DEC.Zero);
      const remainingToPay = OB.DEC.sub(
        ticket.grossAmount,
        OB.DEC.add(paymentsAmount, ticket.nettingPayment || OB.DEC.Zero)
      );

      if (ticket.isNegative) {
        return {
          done:
            OB.DEC.compare(ticket.lines.length) === 1 &&
            OB.DEC.compare(remainingToPay) !== -1,
          total: ticket.grossAmount,
          pending:
            OB.DEC.compare(remainingToPay) === -1
              ? OB.DEC.mul(remainingToPay, -1)
              : OB.DEC.Zero,
          overpayment:
            OB.DEC.compare(remainingToPay) === 1
              ? OB.DEC.sub(OB.DEC.abs(remainingToPay), ticket.change)
              : OB.DEC.Zero,
          isReturn: !isSale,
          isNegative: ticket.isNegative,
          totalAmt: ticket.grossAmount,
          pendingAmt:
            OB.DEC.compare(remainingToPay) === -1
              ? OB.DEC.mul(remainingToPay, -1)
              : OB.DEC.Zero,
          payments: ticket.payments,
          isReversal
        };
      }

      return {
        done:
          OB.DEC.compare(ticket.lines.length) === 1 &&
          OB.DEC.compare(remainingToPay) !== 1,
        total: ticket.grossAmount,
        pending:
          OB.DEC.compare(remainingToPay) === 1 ? remainingToPay : OB.DEC.Zero,
        overpayment:
          OB.DEC.compare(remainingToPay) === -1
            ? OB.DEC.sub(OB.DEC.abs(remainingToPay), ticket.change)
            : OB.DEC.Zero,
        isReturn: !isSale,
        isNegative: ticket.isNegative,
        totalAmt: ticket.grossAmount,
        pendingAmt:
          OB.DEC.compare(remainingToPay) === 1 ? remainingToPay : OB.DEC.Zero,
        payments: ticket.payments,
        isReversal
      };
    },

    /**
     * Updates the type of the given ticket.
     *
     * @param {object} ticket - The ticket whose type will be updated
     * @param {object} payload - The calculation payload, which include:
     *             * terminal.terminalType.documentType - Terminal document type for sales
     *             * terminal.terminalType.documentTypeForReturns - Terminal document type for returns
     *             * terminal.organization - Organization of the current terminal
     *             * preferences.salesWithOneLineNegativeAsReturns - OBPOS_SalesWithOneLineNegativeAsReturns preference value
     *
     * @returns {object} The new state of Ticket after type update.
     */
    updateTicketType(ticket, payload) {
      const isCrossStore = OB.App.State.Ticket.Utils.isCrossStore(
        ticket,
        payload
      );
      if (isCrossStore) {
        return ticket;
      }

      const newTicket = { ...ticket };
      const isSale = OB.App.State.Ticket.Utils.isSale(ticket, payload);
      newTicket.orderType = isSale ? 0 : 1;
      newTicket.documentType = isSale
        ? payload.terminal.terminalType.documentType
        : payload.terminal.terminalType.documentTypeForReturns;

      return newTicket;
    },

    /**
     * Generates the corresponding payment for the given ticket.
     *
     * @param {object} ticket - The ticket where payment will be added
     * @param {object} payload - The calculation payload, which include:
     *             * payment - The payment that will be added to the ticket
     *             * terminal.id - Terminal id
     *             * payments - Terminal payment types
     *
     * @returns {Ticket} The new state of Ticket after payment generation.
     */
    addPayment(ticket, payload) {
      const newTicket = { ...ticket };

      const terminalPayment = payload.payments.find(
        paymentType => paymentType.payment.searchKey === payload.payment.kind
      );
      const precision = terminalPayment
        ? terminalPayment.obposPosprecision
        : OB.DEC.getScale();

      const paymentIndex = newTicket.payments.findIndex(
        payment =>
          payment.kind === payload.payment.kind &&
          !payment.isPrePayment &&
          !payment.reversedPaymentId &&
          !payload.payment.reversedPaymentId &&
          (!payload.payment.paymentData ||
            payload.payment.paymentData.mergeable ||
            (payment.paymentData &&
              payload.payment.paymentData &&
              payment.paymentData.groupingCriteria &&
              payload.payment.paymentData.groupingCriteria &&
              payment.paymentData.groupingCriteria ===
                payload.payment.paymentData.groupingCriteria))
      );

      if (paymentIndex >= 0) {
        newTicket.payments = newTicket.payments.map((payment, index) => {
          if (index !== paymentIndex) {
            return payment;
          }

          const newPayment = { ...payment };
          newPayment.oBPOSPOSTerminal = payload.terminal.id;
          const newAmount = OB.DEC.add(
            OB.DEC.mul(
              payload.payment.amount,
              newPayment.signChanged && newPayment.amount < 0 ? -1 : 1,
              precision
            ),
            newPayment.amount,
            precision
          );
          newPayment.amount = newAmount;
          newPayment.origAmount =
            newPayment.rate && newPayment.rate !== '1'
              ? OB.DEC.div(newAmount, newPayment.mulrate)
              : newAmount;
          newPayment.paid = newPayment.origAmount;
          newPayment.precision = precision;
          newPayment.paymentRoundingLine = payload.payment.paymentRoundingLine
            ? {
                ...payload.payment.paymentRoundingLine,
                roundedPaymentId: newPayment.id
              }
            : undefined;

          return newPayment;
        });
      } else {
        const newPayment = { ...payload.payment };
        newPayment.date = new Date();
        newPayment.id = OB.App.UUID.generate();
        newPayment.oBPOSPOSTerminal = payload.terminal.id;
        newPayment.orderGross = newTicket.grossAmount;
        newPayment.isPaid = newTicket.isPaid;
        newPayment.isReturnOrder = newTicket.isNegative;
        newPayment.paid = newPayment.origAmount;
        newPayment.precision = precision;
        newPayment.cancelAndReplace =
          (newTicket.doCancelAndReplace && newTicket.replacedordernewTicket) ||
          newTicket.cancelAndReplaceChangePending;
        if (newPayment.reversedPayment) {
          newPayment.reversedPayment.isReversed = true;
        }
        if (newPayment.paymentRoundingLine) {
          newPayment.paymentRoundingLine.roundedPaymentId = newPayment.id;
        }
        if (
          newPayment.openDrawer &&
          (newPayment.allowOpenDrawer || newPayment.isCash)
        ) {
          newTicket.openDrawer = newPayment.openDrawer;
        }
        newTicket.payments = [...newTicket.payments, newPayment];
      }

      const paidAmt = newTicket.payments.reduce((total, p) => {
        if (p.isPrePayment || p.isReversePayment || !newTicket.isNegative) {
          return OB.DEC.add(total, p.origAmount);
        }
        return OB.DEC.sub(total, p.origAmount);
      }, OB.DEC.Zero);
      newTicket.payment = OB.DEC.abs(paidAmt);
      newTicket.paymentWithSign = paidAmt;
      if (newTicket.amountToLayaway != null) {
        newTicket.amountToLayaway = OB.DEC.sub(
          newTicket.amountToLayaway,
          payload.payment.origAmount
        );
      }

      return newTicket;
    }
  });
})();