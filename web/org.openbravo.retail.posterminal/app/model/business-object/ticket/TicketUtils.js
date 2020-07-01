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
  const isCash = paymentType => {
    const paymentNames = OB.App.TerminalProperty.get('paymentnames');
    if (!paymentNames[paymentType]) {
      return false;
    }
    return paymentNames[paymentType].paymentMethod.iscash;
  };

  // gets the precision of a given payment
  const getPrecision = payment => {
    const terminalpayment = OB.App.TerminalProperty.get('paymentnames')[
      payment.kind
    ];
    return terminalpayment
      ? terminalpayment.obposPosprecision
      : OB.DEC.getScale();
  };

  // gets the information of the main warehouse of the terminal
  const getTerminalWarehouse = () => {
    const warehouses = OB.App.TerminalProperty.get('warehouses');
    return {
      id: warehouses[0].warehouseid,
      warehousename: warehouses[0].warehousename
    };
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

    createLine(product, qty) {
      const newLine = {
        id: OB.App.UUID.generate(),
        product,
        organization: this.getLineOrganization(product),
        warehouse: getTerminalWarehouse(),
        uOM: product.uOM,
        qty: OB.DEC.number(qty, product.uOMstandardPrecision),
        priceIncludesTax: this.ticket.priceIncludesTax,
        isEditable: true,
        isDeletable: true
      };
      if (newLine.priceIncludesTax) {
        newLine.grossListPrice = OB.DEC.number(product.listPrice);
        newLine.baseGrossUnitPrice = OB.DEC.number(product.standardPrice);
      } else {
        newLine.netListPrice = OB.DEC.number(product.listPrice);
        newLine.baseNetUnitPrice = OB.DEC.number(product.standardPrice);
      }
      this.setDeliveryMode(newLine);
      this.ticket.lines = this.ticket.lines.concat(newLine);
      return newLine;
    }

    getLineOrganization(product) {
      if (product.crossStore) {
        const store = OB.App.TerminalProperty.get('store').find(
          s => s.id === this.ticket.organization
        );
        return {
          id: store.id,
          orgName: store.name,
          country: store.country,
          region: store.region
        };
      }
      const terminal = OB.App.TerminalProperty.get('terminal');
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

    updateServicesInformation() {
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
            const newLine = this.createLine(service, newqtyminus);
            newLine.relatedLines = rln;
            newLine.groupService = newLine.product.groupProduct;
            serviceLine.relatedLines = newRlp;
            serviceLine.qty = newqtyplus;
          } else if (newqtyminus) {
            if (deferredLines.length > 0) {
              const qty = service.quantityRule === 'PP' ? deferredQty : 1;
              const newLine = this.createLine(service, qty);
              newLine.relatedLines = deferredLines;
              newLine.qty = qty;
            }
            serviceLine.relatedLines = rln;
            newqtyminus = this.adjustNotGroupedServices(
              serviceLine,
              newqtyminus
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
                  newqtyplus
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
          const newLine = this.createLine(service, newqtyplus);
          newLine.relatedLines = rlp;
          serviceLine.relatedLines = rln;
          serviceLine.qty = newqtyminus;
        } else if (newqtyplus) {
          serviceLine.relatedLines = rlp;
          newqtyplus = this.adjustNotGroupedServices(serviceLine, newqtyplus);
          serviceLine.qty = newqtyplus;
        } else if (newqtyminus) {
          serviceLine.relatedLines = rln;
          newqtyminus = this.adjustNotGroupedServices(serviceLine, newqtyminus);
          serviceLine.qty = newqtyminus;
        } else if (deferredLines.length === 0 && !serviceLine.obposIsDeleted) {
          this.deleteLine(serviceLine.id);
        }
        this.adjustNotDeferredRelatedLines(serviceLine);
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

    adjustNotGroupedServices(line, qty) {
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
            const newLine = this.createLine(notGroupedProduct, qtyLineServ);
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

    adjustNotDeferredRelatedLines(serviceLine) {
      const notDeferredLines = serviceLine.relatedLines.filter(
        rl => rl.deferred === false
      );
      if (!serviceLine.groupService && notDeferredLines.length > 1) {
        notDeferredLines.forEach(rl => {
          const ticketLine = this.ticket.lines.find(
            l => l.id === rl.orderlineId
          );
          const newLine = this.createLine(serviceLine.product, ticketLine.qty);
          newLine.relatedLines = [rl];
          newLine.groupService = false;
        });
        this.deleteLine(serviceLine.id);
      }
    }

    calculateTotals(settings) {
      this.calculateDiscountsAndTaxes(
        settings.discountRules,
        settings.bpSets,
        settings.taxRules
      );
      this.adjustPayment();
      this.setTotalQuantity(settings.qtyScale);
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

    adjustPayment() {
      const loadedFromBackend = this.ticket.isLayaway || this.ticket.isPaid;

      // set the payments origAmount property
      this.ticket.payments = this.ticket.payments.map(p => {
        const newPayment = this.calculateOrigAmount(p);
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
          (t, p) => OB.DEC.add(t, p.origAmount, getPrecision(p)),
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
      const { defaultCash, nonDefaultCash, noCash } = this.getCashInfo();

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
        p =>
          p.kind === OB.App.TerminalProperty.get('paymentcash') ||
          isCash(p.kind)
      );
      let cashPaid;
      if (cashPayment) {
        let payment;
        const precision = getPrecision(cashPayment);
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

    calculateOrigAmount(payment) {
      const precision = getPrecision(payment);
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
              this.getDifferenceRemovingSpecificPayment(payment),
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
    getDifferenceRemovingSpecificPayment(payment) {
      const precision = getPrecision(payment);
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

    getCashInfo() {
      const loadedFromBackend = this.ticket.isLayaway || this.ticket.isPaid;
      return this.ticket.payments
        .filter(p => !p.isPrePayment)
        .reduce(
          (c, p) => {
            let property;
            if (p.kind === OB.App.TerminalProperty.get('paymentcash')) {
              // the default cash method
              property = 'defaultCash';
            } else if (isCash(p.kind)) {
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
    isReturn(ticket, payload) {
      if (!ticket.lines) {
        return false;
      }

      const negativeLines = ticket.lines.filter(line => line.qty < 0).length;
      return (
        negativeLines === ticket.lines.length ||
        (negativeLines > 0 &&
          payload.preferences.salesWithOneLineNegativeAsReturns)
      );
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
     * Checks whether a ticket belongs to a different store.
     *
     * @param {object} ticket - The ticket whose will be checked
     * @param {object} payload - The calculation payload, which include:
     *             * terminal.organization - Organization of the current terminal
     *
     * @returns {boolean} true in case the ticket is cross store, false otherwise.
     */
    isCrossStoreTicket(ticket, payload) {
      if (!ticket.organization || !payload.terminal.organization) {
        return false;
      }

      return ticket.organization !== payload.terminal.organization;
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
      const isReturn = OB.App.State.Ticket.Utils.isReturn(ticket, payload);
      const isReversal = ticket.payments.some(
        payment => payment.reversedPaymentId
      );
      const paymentsAmount = ticket.payments.reduce((total, payment) => {
        if (
          ticket.cancelAndReplaceChangePending ||
          (!ticket.isLayaway &&
            !ticket.isPaid &&
            ticket.isNegative &&
            !payment.isReversePayment)
        ) {
          return OB.DEC.sub(total, payment.origAmount);
        }
        return OB.DEC.add(total, payment.origAmount);
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
          isReturn,
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
        isReturn,
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
     *             * terminal.documentTypeForSales - Terminal document type for sales
     *             * terminal.documentTypeForReturns - Terminal document type for returns
     *             * terminal.organization - Organization of the current terminal
     *             * preferences.salesWithOneLineNegativeAsReturns - OBPOS_SalesWithOneLineNegativeAsReturns preference value
     *
     * @returns {object} The new state of Ticket after type update.
     */
    updateTicketType(ticket, payload) {
      const isCrossStoreTicket = OB.App.State.Ticket.Utils.isCrossStoreTicket(
        ticket,
        payload
      );
      if (isCrossStoreTicket) {
        return ticket;
      }

      const newTicket = { ...ticket };
      const isReturn = OB.App.State.Ticket.Utils.isReturn(ticket, payload);
      newTicket.orderType = isReturn ? 1 : 0;
      newTicket.documentType = isReturn
        ? payload.terminal.documentTypeForReturns
        : payload.terminal.documentTypeForSales;

      return newTicket;
    },

    /**
     * Computes the totals of a given ticket which include: discounts, taxes and other calculated fields.
     *
     * @param {object} ticket - The ticket whose totals will be calculated
     * @param {object} settings - The calculation settings, which include:
     *             * discountRules - The discount rules to be considered
     *             * taxRules - The tax rules to be considered
     *             * bpSets - The businessPartner sets
     *             * qtyScale - The scale of the ticket quantity (qty)
     * @returns The ticket with the result of the totals calculation
     */
    calculateTotals(ticket, settings) {
      const handler = new TicketHandler(ticket);
      handler.calculateTotals(settings);
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
     * @param {object} product - The product of the new line
     * @param {number} qty - The quantity of the new line
     * @returns {object} - An object with the following properties:
     *                   * newTicket: a new ticket result of adding the new line into the provided ticket
     *                   * newLine: the newly created line
     */
    createLine(ticket, product, qty) {
      const handler = new TicketHandler(ticket);
      const newLine = handler.createLine(product, qty);
      return { newTicket: handler.getTicket(), newLine };
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
     * @returns {object} - A new ticket with the services information properly updated
     */
    updateServicesInformation(ticket) {
      const handler = new TicketHandler(ticket);
      handler.updateServicesInformation();
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
     * Generates the corresponding shipment for the given ticket.
     *
     * @param {object} ticket - The ticket whose shipment will be generated
     * @param {object} settings - The calculation settings, which include:
     *             * terminal.organization - The terminal organization.
     *
     * @returns {object} The ticket with the result of the shipment generation
     */
    generateShipment(ticket, settings) {
      const newTicket = { ...ticket };
      const isFullyPaid =
        ticket.payment >= OB.DEC.abs(ticket.grossAmount) || ticket.payOnCredit;
      const isCrossStoreTicket = OB.App.State.Ticket.Utils.isCrossStoreTicket(
        ticket,
        settings
      );

      newTicket.lines = ticket.lines.map(line => {
        const newLine = { ...line };

        if (isFullyPaid) {
          newLine.obposCanbedelivered = true;
          newLine.obposIspaid = true;
        } else if (line.obposCanbedelivered) {
          newLine.obposIspaid = true;
        }

        if (isCrossStoreTicket && !line.originalOrderLineId) {
          newLine.obposQtytodeliver = line.deliveredQuantity;
        } else if (!line.obposQtytodeliver) {
          if (
            line.product.productType === 'S' &&
            line.product.isLinkedToProduct
          ) {
            if (line.qty > 0) {
              const qtyToDeliver =
                line.product.quantityRule === 'UQ'
                  ? OB.DEC.One
                  : line.relatedLines.reduce((accumulator, relatedLine) => {
                      const orderLine = ticket.lines.find(
                        l => l.id === relatedLine.orderlineId
                      );
                      if (
                        orderLine &&
                        (isFullyPaid || orderLine.obposCanbedelivered)
                      ) {
                        return OB.DEC.add(accumulator, orderLine.qty);
                      }
                      if (relatedLine.obposIspaid) {
                        return OB.DEC.add(
                          accumulator,
                          relatedLine.deliveredQuantity
                        );
                      }
                      return accumulator;
                    }, OB.DEC.Zero);

              newLine.obposQtytodeliver = qtyToDeliver;
              if (qtyToDeliver) {
                newLine.obposCanbedelivered = true;
              }
            } else if (line.qty < 0) {
              newLine.obposQtytodeliver = line.qty;
              newLine.obposCanbedelivered = true;
            }
          } else if (line.obposCanbedelivered) {
            newLine.obposQtytodeliver = line.qty;
          } else {
            newLine.obposQtytodeliver = line.deliveredQuantity;
          }
        }

        return newLine;
      });

      newTicket.generateShipment = newTicket.lines.some(line => {
        const qtyToDeliver = line.obposQtytodeliver
          ? line.obposQtytodeliver
          : line.qty;
        return qtyToDeliver !== line.deliveredQuantity;
      });
      newTicket.deliver = !newTicket.lines.some(line => {
        const qtyToDeliver = line.obposQtytodeliver
          ? line.obposQtytodeliver
          : line.qty;
        return qtyToDeliver !== line.qty;
      });

      return newTicket;
    },

    /**
     * Generates the corresponding delivery for the given ticket.
     *
     * @param {object} ticket - The ticket whose delivery will be generated
     * @param {object} payload - The calculation payload, which include:
     *             * terminal.organization - The terminal organization.
     *
     * @returns {object} The ticket with the result of the delivery generation
     */
    generateDelivery(ticket, payload) {
      const newTicket = { ...ticket };
      const isFullyPaidOrPaidOnCredit =
        OB.App.State.Ticket.Utils.isFullyPaid(ticket) || ticket.payOnCredit;
      const isCrossStoreTicket = OB.App.State.Ticket.Utils.isCrossStoreTicket(
        ticket,
        payload
      );

      newTicket.lines = ticket.lines.map(line => {
        const newLine = { ...line };

        if (isFullyPaidOrPaidOnCredit) {
          newLine.obposCanbedelivered = true;
          newLine.obposIspaid = true;
        } else if (newLine.obposCanbedelivered) {
          newLine.obposIspaid = true;
        }

        if (isCrossStoreTicket && !newLine.originalOrderLineId) {
          newLine.obposQtytodeliver = newLine.deliveredQuantity || OB.DEC.Zero;
          return newLine;
        }

        if (newLine.obposQtytodeliver) {
          return newLine;
        }

        if (
          newLine.product.productType === 'S' &&
          newLine.product.isLinkedToProduct
        ) {
          if (newLine.qty > 0) {
            const qtyToDeliver =
              newLine.product.quantityRule === 'UQ'
                ? OB.DEC.One
                : newLine.relatedLines.reduce((total, relatedLine) => {
                    const orderLine = ticket.lines.find(
                      l => l.id === relatedLine.orderlineId
                    );
                    if (
                      orderLine &&
                      (isFullyPaidOrPaidOnCredit ||
                        orderLine.obposCanbedelivered)
                    ) {
                      return OB.DEC.add(total, orderLine.qty);
                    }
                    if (relatedLine.obposIspaid) {
                      return OB.DEC.add(total, relatedLine.deliveredQuantity);
                    }
                    return total;
                  }, OB.DEC.Zero);

            newLine.obposQtytodeliver = qtyToDeliver;
            if (qtyToDeliver) {
              newLine.obposCanbedelivered = true;
            }
          } else if (newLine.qty < 0) {
            newLine.obposQtytodeliver = newLine.qty;
            newLine.obposCanbedelivered = true;
          }
        } else if (
          newLine.obposCanbedelivered &&
          newLine.obrdmDeliveryMode === 'PickAndCarry'
        ) {
          newLine.obposQtytodeliver = newLine.qty;
        } else {
          newLine.obposQtytodeliver = newLine.deliveredQuantity || OB.DEC.Zero;
        }

        return newLine;
      });

      newTicket.generateShipment = newTicket.lines.some(line => {
        return (
          line.obposQtytodeliver !== (line.deliveredQuantity || OB.DEC.Zero)
        );
      });
      newTicket.deliver = !newTicket.lines.some(line => {
        return line.obposQtytodeliver !== line.qty;
      });

      return newTicket;
    },

    /**
     * Generates the corresponding invoice for the given ticket.
     *
     * @param {object} ticket - The ticket whose invoice will be generated
     * @param {object} payload - The calculation payload, which include:
     *             * discountRules - The discount rules to be considered for discount calculation
     *             * bpSets - The businessPartner sets for discount calculation
     *             * taxRules - The tax rules to be considered for tax calculation
     *
     * @returns {object} The new state of Ticket and DocumentSequence after invoice generation.
     */
    generateInvoice(ticket) {
      const generateInvoice =
        !ticket.obposIsDeleted &&
        (ticket.payOnCredit ||
          (ticket.invoiceTerms === 'I' && ticket.generateInvoice) ||
          (ticket.invoiceTerms === 'O' && ticket.deliver) ||
          (ticket.invoiceTerms === 'D' &&
            (ticket.generateShipment ||
              ticket.lines.find(
                line => line.deliveredQuantity !== line.invoicedQuantity
              ))));

      if (!generateInvoice) {
        return ticket;
      }

      const invoiceLines = ticket.lines.flatMap(line => {
        const originalQty = line.qty;
        const qtyAlreadyInvoiced = line.invoicedQuantity || OB.DEC.Zero;
        const qtyPendingToBeInvoiced = OB.DEC.sub(line.qty, qtyAlreadyInvoiced);

        let qtyToInvoice;
        if (ticket.invoiceTerms === 'D') {
          qtyToInvoice = OB.DEC.sub(line.obposQtytodeliver, qtyAlreadyInvoiced);
        } else if (ticket.invoiceTerms === 'I' || ticket.invoiceTerms === 'O') {
          qtyToInvoice = qtyPendingToBeInvoiced;
        } else {
          qtyToInvoice = OB.DEC.Zero;
        }

        if (
          !qtyToInvoice ||
          (ticket.invoiceTerms !== 'I' &&
            !line.obposCanbedelivered &&
            !line.obposIspaid)
        ) {
          return [];
        }

        const invoiceLine = { ...line };
        invoiceLine.id = OB.App.UUID.generate();
        invoiceLine.qty = qtyToInvoice;
        invoiceLine.orderLineId = line.id;
        invoiceLine.product.ignorePromotions = true;
        invoiceLine.product.img = undefined;

        if (OB.DEC.abs(qtyAlreadyInvoiced) > 0) {
          invoiceLine.promotions = line.promotions.map(promotion => {
            const invoiceLinePromotion = { ...promotion };
            if (OB.DEC.abs(qtyToInvoice) < OB.DEC.abs(qtyPendingToBeInvoiced)) {
              invoiceLinePromotion.amt = OB.DEC.mul(
                invoiceLinePromotion.amt,
                OB.DEC.div(qtyToInvoice, originalQty)
              );
              invoiceLinePromotion.obdiscQtyoffer = qtyToInvoice;
              if (invoiceLinePromotion.actualAmt) {
                invoiceLinePromotion.actualAmt = OB.DEC.mul(
                  invoiceLinePromotion.actualAmt,
                  OB.DEC.div(qtyToInvoice, originalQty)
                );
              }
              if (invoiceLinePromotion.displayedTotalAmount) {
                invoiceLinePromotion.displayedTotalAmount = OB.DEC.mul(
                  invoiceLinePromotion.displayedTotalAmount,
                  OB.DEC.div(qtyToInvoice, originalQty)
                );
              }
              if (invoiceLinePromotion.fullAmt) {
                invoiceLinePromotion.fullAmt = OB.DEC.mul(
                  invoiceLinePromotion.fullAmt,
                  OB.DEC.div(qtyToInvoice, originalQty)
                );
              }
              if (invoiceLinePromotion.qtyOffer) {
                invoiceLinePromotion.qtyOffer = qtyToInvoice;
              }
              if (invoiceLinePromotion.pendingQtyOffer) {
                invoiceLinePromotion.pendingQtyOffer = qtyToInvoice;
              }
            } else {
              invoiceLinePromotion.amt = OB.DEC.sub(
                invoiceLinePromotion.amt,
                OB.DEC.mul(
                  invoiceLinePromotion.amt,
                  OB.DEC.div(qtyAlreadyInvoiced, originalQty)
                )
              );
              invoiceLinePromotion.obdiscQtyoffer = qtyToInvoice;
              if (invoiceLinePromotion.actualAmt) {
                invoiceLinePromotion.actualAmt = OB.DEC.sub(
                  invoiceLinePromotion.actualAmt,
                  OB.DEC.mul(
                    invoiceLinePromotion.actualAmt,
                    OB.DEC.div(qtyAlreadyInvoiced, originalQty)
                  )
                );
              }
              if (invoiceLinePromotion.displayedTotalAmount) {
                invoiceLinePromotion.displayedTotalAmount = OB.DEC.sub(
                  invoiceLinePromotion.displayedTotalAmount,
                  OB.DEC.mul(
                    invoiceLinePromotion.displayedTotalAmount,
                    OB.DEC.div(qtyAlreadyInvoiced, originalQty)
                  )
                );
              }
              if (invoiceLinePromotion.fullAmt) {
                invoiceLinePromotion.fullAmt = OB.DEC.sub(
                  invoiceLinePromotion.fullAmt,
                  OB.DEC.mul(
                    invoiceLinePromotion.fullAmt,
                    OB.DEC.div(qtyAlreadyInvoiced, originalQty)
                  )
                );
              }
              if (invoiceLinePromotion.qtyOffer) {
                invoiceLinePromotion.qtyOffer = qtyToInvoice;
              }
              if (invoiceLinePromotion.pendingQtyOffer) {
                invoiceLinePromotion.pendingQtyOffer = qtyToInvoice;
              }
            }

            return invoiceLinePromotion;
          });
        }

        return invoiceLine;
      });

      if (!invoiceLines.length) {
        return ticket;
      }

      const newTicket = { ...ticket };
      const invoice = { ...ticket };
      invoice.orderId = ticket.id;
      invoice.id = OB.App.UUID.generate();
      invoice.isInvoice = true;
      invoice.documentNo = null;
      invoice.orderDocumentNo = ticket.documentNo;
      invoice.lines = invoiceLines;

      // FIXME: wait CAR fix
      // invoice = OB.App.State.Ticket.Utils.applyDiscountsAndTaxes(
      //   invoice,
      //   payload
      // );
      newTicket.calculatedInvoice = invoice;

      return newTicket;
    },

    /**
     * Completes ticket payment generating change payment if needed and converting payment to negative in case of return.
     *
     * @param {object} ticket - The ticket whose payment will be completed
     * @param {object} payload - The calculation payload, which include:
     *             * terminal.paymentTypes - Terminal payment types
     *             * terminal.multiChange - Terminal multichange configuration
     *             * preferences.splitChange - OBPOS_SplitChange preference value
     *
     * @returns {object} The new state of Ticket after payment completing.
     */
    completePayment(ticket, payload) {
      let newTicket = { ...ticket };

      // Manage change payments if there is change
      if (newTicket.changePayments && newTicket.changePayments.length > 0) {
        const prevChange = newTicket.change;
        const mergeable =
          !payload.terminal.multiChange && !payload.preferences.splitChange;

        newTicket.changePayments.forEach(changePayment => {
          const terminalPayment = payload.terminal.paymentTypes.find(
            paymentType => paymentType.payment.searchKey === changePayment.key
          );

          // Generate change payment
          newTicket = OB.App.State.Ticket.Utils.generatePayment(newTicket, {
            ...payload,
            payment: {
              kind: terminalPayment.payment.searchKey,
              name: terminalPayment.payment.commercialName,
              amount: OB.DEC.sub(
                OB.DEC.Zero,
                changePayment.amount,
                terminalPayment.obposPosprecision
              ),
              amountRounded: OB.DEC.sub(
                OB.DEC.Zero,
                changePayment.amountRounded,
                terminalPayment.obposPosprecision
              ),
              origAmount: OB.DEC.sub(OB.DEC.Zero, changePayment.origAmount),
              origAmountRounded: OB.DEC.sub(
                OB.DEC.Zero,
                OB.DEC.mul(changePayment.amountRounded, terminalPayment.rate)
              ),
              rate: terminalPayment.rate,
              mulrate: terminalPayment.mulrate,
              isocode: terminalPayment.isocode,
              allowOpenDrawer: terminalPayment.paymentMethod.allowopendrawer,
              isCash: terminalPayment.paymentMethod.iscash,
              openDrawer: terminalPayment.paymentMethod.openDrawer,
              printtwice: terminalPayment.paymentMethod.printtwice,
              changePayment: true,
              paymentData: {
                mergeable,
                label: changePayment.label
              }
            }
          });

          // Recalculate payment and paymentWithSign properties
          const paidAmt = newTicket.payments.reduce((total, payment) => {
            if (
              payment.isPrePayment ||
              payment.isReversePayment ||
              !newTicket.isNegative
            ) {
              return OB.DEC.add(total, payment.origAmount);
            }
            return OB.DEC.sub(total, payment.origAmount);
          }, OB.DEC.Zero);
          newTicket.payment = OB.DEC.abs(paidAmt);
          newTicket.paymentWithSign = paidAmt;
          newTicket.change = prevChange;
        });
      }

      // Convert return payments in negative
      if (newTicket.isNegative) {
        newTicket.payments = newTicket.payments.map(payment => {
          const newPayment = { ...payment };

          if (
            payment.isPrePayment ||
            payment.reversedPaymentId ||
            newTicket.isPaid
          ) {
            newPayment.paid = payment.amount;
            return newPayment;
          }

          newPayment.amount = -payment.amount;
          newPayment.origAmount = -payment.origAmount;
          newPayment.paid = -payment.paid;
          newPayment.amountRounded = payment.amountRounded
            ? -payment.amountRounded
            : payment.amountRounded;
          newPayment.origAmountRounded = payment.origAmountRounded
            ? -payment.origAmountRounded
            : payment.origAmountRounded;
          return newPayment;
        });
      }

      return newTicket;
    },

    /**
     * Generates the corresponding payment for the given ticket.
     *
     * @param {object} ticket - The ticket whose payment will be generated
     * @param {object} payload - The calculation payload, which include:
     *             * payment - The payment that will be added to the ticket
     *             * terminal.id - Terminal id
     *             * terminal.paymentTypes - Terminal payment types
     *
     * @returns {object} The new state of Ticket after payment generation.
     */
    generatePayment(ticket, payload) {
      const newTicket = { ...ticket };
      const terminalPayment = payload.terminal.paymentTypes.find(
        paymentType => paymentType.payment.searchKey === payload.payment.kind
      );
      const precision = terminalPayment
        ? terminalPayment.obposPosprecision
        : OB.DEC.getScale();

      const payment = newTicket.payments.find(
        p =>
          p.kind === payload.payment.kind &&
          !p.isPrePayment &&
          !p.reversedPaymentId &&
          !payload.payment.reversedPaymentId &&
          (!payload.payment.paymentData ||
            payload.payment.paymentData.mergeable ||
            (p.paymentData &&
              payload.payment.paymentData &&
              p.paymentData.groupingCriteria &&
              payload.payment.paymentData.groupingCriteria &&
              p.paymentData.groupingCriteria ===
                payload.payment.paymentData.groupingCriteria))
      );

      if (payment) {
        const newAmount = OB.DEC.add(
          OB.DEC.mul(
            payload.payment.amount,
            payment.signChanged && payment.amount < 0 ? -1 : 1,
            precision
          ),
          payment.amount,
          precision
        );
        payment.amount = newAmount;
        payment.origAmount =
          payment.rate && payment.rate !== '1'
            ? OB.DEC.div(newAmount, payment.mulrate)
            : newAmount;
        payment.paymentRoundingLine = payload.payment.paymentRoundingLine
          ? {
              ...payload.payment.paymentRoundingLine,
              roundedPaymentId: payment.id
            }
          : undefined;

        return newTicket;
      }

      const newPayment = { ...payload.payment };
      newPayment.date = new Date();
      newPayment.id = OB.App.UUID.generate();
      newPayment.oBPOSPOSTerminal = payload.terminal.id;
      newPayment.orderGross = newTicket.grossAmount;
      newPayment.isPaid = newTicket.isPaid;
      newPayment.isReturnOrder = newTicket.isNegative;
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

      return newTicket;
    }
  });
})();
