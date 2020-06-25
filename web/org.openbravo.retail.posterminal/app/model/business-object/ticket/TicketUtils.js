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
  /**
   * Internal helper that allows to apply changes in a ticket in a pure way
   */
  class TicketHandler {
    constructor(ticket) {
      this.ticket = { ...ticket };
      this.ticket.lines = ticket.lines.map(l => {
        return { ...l };
      });
    }

    getTicket() {
      return this.ticket;
    }

    createLine(product, qty) {
      const newLine = {
        id: OB.App.UUID.generate(),
        product,
        organization: this.getLineOrganization(product),
        warehouse: this.getLineWarehouse(),
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

    // eslint-disable-next-line class-methods-use-this
    getLineWarehouse() {
      const warehouses = OB.App.TerminalProperty.get('warehouses');
      return {
        id: warehouses[0].warehouseid,
        warehousename: warehouses[0].warehousename
      };
    }

    setDeliveryMode(line) {
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
      const serviceLines = this.getServiceLines();
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
      return this.ticket.hasServices && this.ticket.isEditable;
    }

    getServiceLines() {
      return this.ticket.lines.filter(
        l =>
          l.relatedLines && l.relatedLines.length > 0 && !l.originalOrderLineId
      );
    }

    getSiblingServicesLines(productId, orderlineId) {
      return this.getServiceLines().filter(
        l =>
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
      this.calculateDiscounts(settings.discountRules, settings.bpSets);
      this.calculateTaxes(settings.taxRules);
      this.setIsNegative();
      this.setTotalQuantity(settings.qtyScale);
    }

    calculateDiscounts(discountRules, bpSets) {
      const { priceIncludesTax } = this.ticket;

      const discountsResult = OB.Discounts.Pos.applyDiscounts(
        this.ticket,
        discountRules,
        bpSets
      );

      // sets initial line amounts and prices and applies the discount calculation result into the ticket
      this.ticket.lines = this.ticket.lines.map(line => {
        const newLine = { ...line };
        if (priceIncludesTax) {
          newLine.grossUnitPrice = line.baseGrossUnitPrice;
          newLine.grossUnitAmount = OB.DEC.mul(
            line.qty,
            line.baseGrossUnitPrice
          );
        } else {
          newLine.netUnitPrice = line.baseNetUnitPrice;
          newLine.netUnitAmount = OB.DEC.mul(line.qty, line.baseNetUnitPrice);
        }

        if (line.skipApplyPromotions) {
          return newLine;
        }

        const discounts = discountsResult.lines.find(l => l.id === line.id);
        newLine.promotions = discounts ? discounts.discounts : [];

        if (!discounts) {
          return newLine;
        }

        if (priceIncludesTax) {
          newLine.grossUnitPrice = discounts.grossUnitPrice;
          newLine.grossUnitAmount = discounts.grossUnitAmount;
        } else {
          newLine.netUnitPrice = discounts.netUnitPrice;
          newLine.netUnitAmount = discounts.netUnitAmount;
        }
        return newLine;
      });
    }

    calculateTaxes(taxRules) {
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

    setIsNegative() {
      // TODO: replace this with the complete implementation of "adjustPayment"
      const loadedFromBackend = this.ticket.isLayaway || this.ticket.isPaid;
      const { grossAmount } = this.ticket;
      if (loadedFromBackend) {
        this.ticket.isNegative = OB.DEC.compare(grossAmount) === -1;
      } else {
        let processedPaymentsAmount = this.ticket.payments
          .filter(p => p.isPrePayment)
          .reduce((t, p) => OB.DEC.add(t, p.origAmount), OB.DEC.Zero);

        processedPaymentsAmount = OB.DEC.add(
          processedPaymentsAmount,
          this.ticket.nettingPayment ? this.ticket.nettingPayment : OB.DEC.Zero
        );
        if (OB.DEC.compare(grossAmount) === -1) {
          this.ticket.isNegative = processedPaymentsAmount >= grossAmount;
        } else {
          this.ticket.isNegative = processedPaymentsAmount > grossAmount;
        }
      }
    }
  }

  OB.App.StateAPI.Ticket.registerUtilityFunctions({
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
    }
  });
})();
