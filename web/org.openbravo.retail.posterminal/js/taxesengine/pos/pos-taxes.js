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
    translateTicket: function(receipt) {
      const newTicket = {};
      newTicket.id = receipt.get('id');
      newTicket.date = new Date();
      newTicket.priceIncludesTax = receipt.get('priceIncludesTax');
      newTicket.isCashVat = OB.MobileApp.model.get('terminal').cashVat;
      newTicket.businessPartner = {};
      newTicket.businessPartner.id = receipt.get('bp').id;
      newTicket.businessPartner.taxCategory = receipt
        .get('bp')
        .get('taxCategory');
      newTicket.businessPartner.taxExempt = receipt.get('bp').get('taxExempt');

      newTicket.lines = [];
      receipt.get('lines').forEach(line => {
        const newLine = {};
        newLine.id = line.get('id');
        newLine.country =
          line.get('obrdmDeliveryMode') === 'HomeDelivery'
            ? receipt.get('bp').get('shipCountryId') ||
              receipt
                .get('bp')
                .get('locationModel')
                .get('countryId')
            : line.get('organization').country;
        newLine.region =
          line.get('obrdmDeliveryMode') === 'HomeDelivery'
            ? receipt.get('bp').get('shipRegionId') ||
              receipt
                .get('bp')
                .get('locationModel')
                .get('regionId')
            : line.get('organization').region;
        newLine.amount = newTicket.priceIncludesTax
          ? line.has('discountedGross')
            ? line.get('discountedGross')
            : line.get('gross')
          : line.has('discountedNet')
          ? line.get('discountedNet')
          : line.get('net');
        newLine.quantity = line.get('qty');
        newLine.taxExempt = line.get('originalTaxExempt');
        newLine.product = {};
        newLine.product.id = line.get('product').id;
        newLine.product.taxCategory = line.has('originalTaxCategory')
          ? line.get('originalTaxCategory')
          : line.get('product').has('modifiedTaxCategory')
          ? line.get('product').get('modifiedTaxCategory')
          : line.get('product').get('taxCategory');

        if (line.get('product').has('productBOM')) {
          newLine.bomLines = [];
          line
            .get('product')
            .get('productBOM')
            .forEach(bomLine => {
              const newBomLine = {};
              newBomLine.id = bomLine.id;
              newBomLine.amount = bomLine.bomamount;
              newBomLine.quantity = bomLine.bomquantity;
              newBomLine.product = {};
              newBomLine.product.id = bomLine.bomproduct;
              newBomLine.product.taxCategory = bomLine.bomtaxcategory;
              newLine.bomLines.push(newBomLine);
            });
        }

        newTicket.lines.push(newLine);
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
      const calculateLineRate = taxArray => {
        if (!taxArray) {
          return null;
        }

        return OB.DEC.toNumber(
          taxArray.reduce(
            (total, tax) =>
              total.multiply(
                BigDecimal.prototype.ONE.add(
                  new BigDecimal(String(tax.tax.rate)).divide(
                    new BigDecimal('100'),
                    20,
                    BigDecimal.prototype.ROUND_HALF_UP
                  )
                )
              ),
            BigDecimal.prototype.ONE
          )
        );
      };

      taxes.header.taxes = translateTaxes(taxes.header.taxes);
      taxes.lines.forEach(line => {
        line.lineRate = calculateLineRate(line.taxes);
        line.taxes = translateTaxes(line.taxes);
      });
      return taxes;
    },

    /**
     * Finds the list of taxes that apply to given receipt, line and tax category.
     * The list will be sorted by applicable region, country, date and default.
     * @param {Object} receipt - The receipt that taxes will apply to.
     * @param {Object} line - The receipt line that taxes will apply to.
     * @param {string} taxCategory - The tax category that must apply to given receipt line.
     */
    calculateTaxes(receipt) {
      if (!OB.Taxes.Pos.ruleImpls) {
        throw 'Local tax cache is not yet initialized, execute: OB.Taxes.Pos.initCache()';
      }

      const ticket = OB.Taxes.Pos.translateTicket(receipt);
      const result = OB.Taxes.applyTaxes(ticket, OB.Taxes.Pos.ruleImpls);
      const taxes = OB.Taxes.Pos.translateTaxes(result);

      return taxes;
    },

    /**
     * Reads tax masterdata models from database and creates different caches to use them:
     *   OB.Taxes.Pos.ruleImpls: array with the result of doing a left join between TaxRate and TaxZone models.
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
      data.ruleImpls = taxRates.flatMap(taxRate =>
        taxZones.some(taxZone => taxZone.taxRateId === taxRate.id)
          ? taxZones
              .filter(taxZone => taxZone.taxRateId === taxRate.id)
              .map(taxZone => ({ ...taxZone, ...taxRate }))
          : { ...taxRate }
      );

      // FIXME: order by default desc and by name asc
      data.taxCategory = await OB.App.MasterdataModels.TaxCategory.find(
        new OB.App.Class.Criteria()
          .orderBy(['default', 'name'], 'desc')
          .limit(10000)
          .build()
      );

      // FIXME: order by default desc and by name asc
      data.taxCategoryBOM = await OB.App.MasterdataModels.TaxCategoryBOM.find(
        new OB.App.Class.Criteria()
          .orderBy(['default', 'name'], 'desc')
          .limit(10000)
          .build()
      );

      return data;
    }
  };
})();
