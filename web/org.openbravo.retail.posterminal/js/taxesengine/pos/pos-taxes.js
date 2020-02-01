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
      let newTicket = {};
      newTicket.id = receipt.get('id');
      newTicket.date = new Date();
      newTicket.country = OB.MobileApp.model.get(
        'terminal'
      ).organizationCountryId;
      newTicket.region = OB.MobileApp.model.get(
        'terminal'
      ).organizationRegionId;
      newTicket.priceIncludesTax = receipt.get('priceIncludesTax');
      newTicket.isCashVat = OB.MobileApp.model.get('terminal').cashVat;
      newTicket.businessPartner = {};
      newTicket.businessPartner.id = receipt.get('bp').id;
      newTicket.businessPartner.taxCategory = receipt
        .get('bp')
        .get('taxCategory');
      newTicket.businessPartner.taxExempt = receipt.get('bp').get('taxExempt');
      newTicket.businessPartner.address = {};
      newTicket.businessPartner.address.id = receipt.get('bp').get('shipLocId');
      newTicket.businessPartner.address.country =
        receipt.get('bp').get('shipCountryId') ||
        receipt
          .get('bp')
          .get('locationModel')
          .get('countryId');
      newTicket.businessPartner.address.region =
        receipt.get('bp').get('shipRegionId') ||
        receipt
          .get('bp')
          .get('locationModel')
          .get('regionId');

      newTicket.lines = [];
      receipt.get('lines').forEach(line => {
        let newLine = {};
        newLine.id = line.get('id');
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
        newLine.product.isBom = OB.Taxes.Pos.taxCategoryBOM.find(
          taxCategory => taxCategory.id === newLine.product.taxCategory
        )
          ? true
          : false;
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

        return taxArray.reduce(
          (total, tax) =>
            OB.DEC.mul(
              total,
              OB.DEC.add(OB.DEC.One, OB.DEC.div(tax.tax.rate, 100))
            ),
          OB.DEC.One
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
      const result = OB.Taxes.calculateTaxes(ticket, OB.Taxes.Pos.ruleImpls);
      const taxes = OB.Taxes.Pos.translateTaxes(result);

      return taxes;
    },

    /**
     * Reads tax masterdata models from database and creates different caches to use them:
     *   OB.Taxes.Pos.ruleImpls: array with the result of doing a left join between TaxRate and TaxZone models.
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

      const taxRates = await OB.App.MasterdataModels.TaxRate.find();
      const taxZones = await OB.App.MasterdataModels.TaxZone.find();

      OB.Taxes.Pos.ruleImpls = taxRates.flatMap(taxRate =>
        taxZones.some(taxZone => taxZone.taxRateId === taxRate.id)
          ? taxZones
              .filter(taxZone => taxZone.taxRateId === taxRate.id)
              .map(taxZone => ({ ...taxZone, ...taxRate }))
          : { ...taxRate }
      );

      OB.Taxes.Pos.taxCategoryBOM = await OB.App.MasterdataModels.TaxCategoryBOM.find();

      OB.UTIL.HookManager.executeHooks(
        'OBPOS_FindTaxRate',
        {
          taxes: OB.Taxes.Pos.ruleImpls
        },
        () => {
          OB.UTIL.ProcessController.finish('taxCacheInitialization', execution);
          callback();
          delete OB.Taxes.Pos.isCalculatingCache;
        }
      );
    }
  };
})();
