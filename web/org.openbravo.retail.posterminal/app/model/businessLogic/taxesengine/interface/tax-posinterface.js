/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  OB.Taxes = OB.Taxes || {};
  OB.Taxes.Pos = OB.Taxes.Pos || {};

  const translateTicket = ticket => {
    const newTicket = { ...ticket };
    newTicket.date = ticket.orderDate;
    newTicket.businessPartner.businessPartnerTaxCategory =
      newTicket.businessPartner.taxCategory;

    newTicket.lines = ticket.lines.map(line => {
      const newLine = { ...line };
      if (line.obrdmDeliveryMode === 'HomeDelivery') {
        newLine.country =
          ticket.businessPartner.shipCountryId ||
          ticket.businessPartner.shipLocId
            ? ticket.businessPartner.locationModel.countryId
            : null;
        newLine.region =
          ticket.businessPartner.shipRegionId ||
          ticket.businessPartner.shipLocId
            ? ticket.businessPartner.locationModel.regionId
            : null;
      } else {
        newLine.country = line.organization.country;
        newLine.region = line.organization.region;
      }
      newLine.amount = ticket.priceIncludesTax
        ? line.discountedGrossAmount
        : line.discountedNetAmount;
      newLine.taxExempt = line.originalTaxExempt;
      newLine.product.taxCategory = line.originalTaxCategory
        ? line.originalTaxCategory
        : line.product.taxCategory;
      return newLine;
    });

    return newTicket;
  };

  const isTaxRecalculationNecessary = ticket => {
    return (
      ticket.priceIncludesTax && ticket.lines.find(line => line.recalculateTax)
    );
  };

  const getTicketForTaxRecalculation = (ticket, taxes) => {
    const newTicket = { ...ticket };

    newTicket.lines = ticket.lines.map(line => {
      const newLine = { ...line };
      if (line.recalculateTax && line.lineRate) {
        const lineTax = taxes.lines.find(lt => lt.id === line.id);
        newLine.amount = OB.DEC.mul(
          OB.DEC.mul(
            OB.DEC.div(lineTax.grossPrice, line.lineRate),
            lineTax.taxRate
          ),
          line.qty
        );
      }
      return newLine;
    });

    return newTicket;
  };

  const translateTaxes = taxes => {
    const newTaxes = { ...taxes };

    const translateTaxArray = taxArray => {
      if (!taxArray) {
        return {};
      }

      return taxArray
        .map(tax => ({
          id: tax.tax.id,
          net: tax.base,
          amount: tax.amount,
          name: tax.tax.name,
          docTaxAmount: tax.tax.docTaxAmount,
          rate: tax.tax.rate,
          taxBase: tax.tax.taxBase,
          cascade: tax.tax.cascade,
          lineNo: tax.tax.lineNo
        }))
        .reduce((obj, item) => {
          const newObj = { ...obj };
          newObj[[item.id]] = item;
          return newObj;
        }, {});
    };

    newTaxes.header.taxes = translateTaxArray(taxes.header.taxes);
    newTaxes.lines = taxes.lines.map(line => {
      const newLine = { ...line };
      newLine.taxes = translateTaxArray(line.taxes);
      return newLine;
    });

    return newTaxes;
  };

  /**
   * Finds the list of given tax rules that apply to given ticket.
   * @param {Object} ticket - The ticket that taxes will apply to.
   * @param {Object[]} rules - Array with TaxRate model including TaxZone information.
   */
  OB.Taxes.Pos.applyTaxes = (ticket, rules) => {
    const ticketForTaxEngine = translateTicket(ticket);
    let taxes = OB.Taxes.applyTaxes(ticketForTaxEngine, rules);

    if (isTaxRecalculationNecessary(ticketForTaxEngine)) {
      const ticketForTaxRecalculation = getTicketForTaxRecalculation(
        ticketForTaxEngine,
        taxes
      );
      taxes = OB.Taxes.applyTaxes(ticketForTaxRecalculation, rules);
    }

    return translateTaxes(taxes);
  };

  /**
   * Reads the tax masterdata model information from database.
   * This information is used to initialize the tax caches.
   * @return {Object} The tax masterdata model information.
   * @see {@link OB.Taxes.Pos.initCache}
   */
  OB.Taxes.Pos.loadData = async () => {
    const data = {};

    const taxRates = await OB.App.MasterdataModels.TaxRate.find(
      new OB.App.Class.Criteria().limit(10000).build()
    );
    const taxZones = await OB.App.MasterdataModels.TaxZone.find(
      new OB.App.Class.Criteria().limit(10000).build()
    );

    const taxZonesByTaxRateId = OB.App.ArrayUtils.groupBy(
      taxZones,
      'taxRateId'
    );
    data.ruleImpls = taxRates.map(taxRate => {
      const newTaxRate = { ...taxRate };
      newTaxRate.taxZones = taxZonesByTaxRateId[taxRate.id];
      return newTaxRate;
    });

    data.taxCategory = await OB.App.MasterdataModels.TaxCategory.find(
      new OB.App.Class.Criteria()
        .orderBy(['default', 'name'], ['desc', 'asc'])
        .limit(10000)
        .build()
    );

    data.taxCategoryBOM = await OB.App.MasterdataModels.TaxCategoryBOM.find(
      new OB.App.Class.Criteria()
        .orderBy(['default', 'name'], ['desc', 'asc'])
        .limit(10000)
        .build()
    );

    return data;
  };
})();
