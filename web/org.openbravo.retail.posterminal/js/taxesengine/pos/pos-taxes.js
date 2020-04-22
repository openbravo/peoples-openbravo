/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/*global OB */

(function() {
  OB.Taxes = OB.Taxes || {};
  OB.Taxes.Pos = {
    isTaxRecalculationNecessary: function(ticket) {
      return (
        ticket.priceIncludesTax &&
        ticket.lines.find(line => line.recalculateTax)
      );
    },

    getTicketForTaxRecalculation: function(ticket, taxes) {
      const newTicket = { ...ticket };
      newTicket.lines = newTicket.lines.map(line => {
        const lineTax = taxes.lines.find(lineTax => lineTax.id === line.id);
        line.amount =
          line.recalculateTax && line.lineRate
            ? OB.DEC.mul(
                OB.DEC.mul(
                  OB.DEC.div(lineTax.grossPrice, line.lineRate),
                  lineTax.taxRate
                ),
                line.qty
              )
            : line.amount;
        return line;
      });
      return newTicket;
    },

    translateReceipt: function(receipt) {
      return {
        id: receipt.get('id'),
        orderDate: receipt.get('orderDate'),
        priceIncludesTax: receipt.get('priceIncludesTax'),
        cashVAT: receipt.get('cashVAT'),
        businessPartner: {
          id: receipt.get('bp').id,
          taxCategory: receipt.get('bp').get('taxCategory'),
          taxExempt: receipt.get('bp').get('taxExempt'),
          shipCountryId: receipt.get('bp').get('shipCountryId'),
          shipRegionId: receipt.get('bp').get('shipRegionId'),
          shipLocId: receipt.get('bp').get('shipLocId'),
          locationModel: {
            countryId: receipt
              .get('bp')
              .get('locationModel')
              .get('countryId'),
            regionId: receipt
              .get('bp')
              .get('locationModel')
              .get('regionId')
          }
        },
        lines: receipt.get('lines').map(line => {
          return {
            id: line.get('id'),
            recalculateTax: line.get('recalculateTax'),
            lineRate: line.get('lineRate'),
            obrdmDeliveryMode: line.get('obrdmDeliveryMode'),
            discountedGross: line.get('discountedGross'),
            gross: line.get('gross'),
            discountedNet: line.get('discountedNet'),
            net: line.get('net'),
            qty: line.get('qty'),
            originalTaxCategory: line.get('originalTaxCategory'),
            originalTaxExempt: line.get('originalTaxExempt'),
            organization: {
              country: line.get('organization').country,
              region: line.get('organization').region
            },
            product: {
              id: line.get('product').id,
              taxCategory: line.get('product').get('taxCategory')
            },
            bomLines: line.get('product').get('productBOM')
          };
        })
      };
    },

    translateTicket: function(ticket) {
      const newTicket = { ...ticket };
      newTicket.date = newTicket.orderDate;
      newTicket.businessPartner.businessPartnerTaxCategory =
        newTicket.businessPartner.taxCategory;

      newTicket.lines.forEach(newLine => {
        newLine.country =
          newLine.obrdmDeliveryMode === 'HomeDelivery'
            ? newTicket.businessPartner.shipCountryId ||
              newTicket.businessPartner.shipLocId
              ? newTicket.businessPartner.locationModel.countryId
              : null
            : newLine.organization.country;
        newLine.region =
          newLine.obrdmDeliveryMode === 'HomeDelivery'
            ? newTicket.businessPartner.shipRegionId ||
              newTicket.businessPartner.shipLocId
              ? newTicket.businessPartner.locationModel.regionId
              : null
            : newLine.organization.region;
        newLine.amount = newTicket.priceIncludesTax
          ? newLine.discountedGross || newLine.discountedGross === 0
            ? newLine.discountedGross
            : newLine.gross
          : newLine.discountedNet || newLine.discountedNet === 0
          ? newLine.discountedNet
          : newLine.net;
        newLine.taxExempt = newLine.originalTaxExempt;
        newLine.product.taxCategory = newLine.originalTaxCategory
          ? newLine.originalTaxCategory
          : newLine.product.taxCategory;
      });

      return newTicket;
    },

    translateTaxes: function(taxes) {
      const translateTaxes = taxArray => {
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
          .reduce((obj, item) => ((obj[[item['id']]] = item), obj), {});
      };

      taxes.header.taxes = translateTaxes(taxes.header.taxes);
      taxes.lines.forEach(line => {
        line.taxes = translateTaxes(line.taxes);
      });
      return taxes;
    },

    /**
     * Finds the list of taxes that apply to given receipt.
     * @param {Object} receipt - The receipt that taxes will apply to.
     */
    calculateTaxes(receipt) {
      if (!OB.Taxes.Pos.ruleImpls) {
        throw 'Local tax cache is not yet initialized, execute: OB.Taxes.Pos.initCache()';
      }

      const ticket = this.translateReceipt(receipt);
      const result = this.applyTaxes(ticket, this.ruleImpls);
      return this.translateTaxes(result);
    },

    /**
     * Finds the list of given tax rules that apply to given ticket.
     * @param {Object} ticket - The ticket that taxes will apply to.
     * @param {Object[]} rules - Array with TaxRate model including TaxZone information.
     */
    applyTaxes(ticket, rules) {
      const ticketForTaxEngine = this.translateTicket(ticket);
      let taxes = OB.Taxes.applyTaxes(ticketForTaxEngine, rules);

      if (this.isTaxRecalculationNecessary(ticketForTaxEngine)) {
        const ticketForTaxRecalculation = this.getTicketForTaxRecalculation(
          ticketForTaxEngine,
          taxes
        );
        taxes = OB.Taxes.applyTaxes(ticketForTaxRecalculation, rules);
      }

      return taxes;
    },

    /**
     * Reads tax masterdata models from database and creates different caches to use them:
     *   OB.Taxes.Pos.ruleImpls: array with TaxRate model including TaxZone information.
     *   OB.Taxes.Pos.taxCategory: array with TaxCategory model.
     *   OB.Taxes.Pos.taxCategoryBOM: array with TaxCategoryBOM model.
     * Tax masterdata models should be read from database only here. Wherever tax data is needed, any of these caches should be used.
     */
    initCache: async function(callback) {
      if (OB.Taxes.Pos.isCalculatingCache) {
        return callback();
      }
      OB.Taxes.Pos.isCalculatingCache = true;
      const execution = OB.UTIL.ProcessController.start(
        'taxCacheInitialization'
      );

      const data = await OB.Taxes.Pos.loadData();
      Object.assign(OB.Taxes.Pos, data);

      OB.UTIL.ProcessController.finish('taxCacheInitialization', execution);
      callback();
      delete OB.Taxes.Pos.isCalculatingCache;
    },

    /**
     * Reads the tax masterdata model information from database.
     * This information is used to initialize the tax caches.
     * @return {Object} The tax masterdata model information.
     * @see {@link OB.Taxes.Pos.initCache}
     */
    loadData: async function() {
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
        taxRate.taxZones = taxZonesByTaxRateId[taxRate.id];
        return taxRate;
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
    }
  };
})();
