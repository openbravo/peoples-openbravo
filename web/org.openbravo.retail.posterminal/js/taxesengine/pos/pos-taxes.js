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
    translateTicket: function(receipt, line, taxCategory) {
      let newTicket = {};
      newTicket.id = receipt.get('id');
      newTicket.date = line.has('originalOrderDate')
        ? new Date(line.get('originalOrderDate'))
        : new Date();
      newTicket.country = OB.MobileApp.model.get(
        'terminal'
      ).organizationCountryId;
      newTicket.region = OB.MobileApp.model.get(
        'terminal'
      ).organizationRegionId;
      newTicket.isCashVat = OB.MobileApp.model.get('terminal').cashVat;
      newTicket.businessPartner = {};
      newTicket.businessPartner.id = receipt.get('bp').id;
      newTicket.businessPartner.taxCategory = receipt
        .get('bp')
        .get('taxCategory');
      newTicket.businessPartner.taxExempt = receipt.get('bp').get('taxExempt');
      newTicket.businessPartner.country =
        receipt.get('bp').get('shipCountryId') ||
        receipt
          .get('bp')
          .get('locationModel')
          .get('countryId');
      newTicket.businessPartner.region =
        receipt.get('bp').get('shipRegionId') ||
        receipt
          .get('bp')
          .get('locationModel')
          .get('regionId');

      newTicket.lines = [];
      let newLine = {};
      newLine.id = line.get('id');
      newLine.taxExempt = line.get('originalTaxExempt');
      newLine.product = {};
      newLine.product.id = line.get('product').id;
      newLine.product.taxCategory = taxCategory;
      newTicket.lines.push(newLine);

      return newTicket;
    },

    calculateTaxes(receipt, line, taxCategory) {
      if (!OB.Taxes.Pos.ruleImpls) {
        throw 'Local tax cache is not yet initialized, execute: OB.Taxes.Pos.initCache()';
      }

      const ticketForEngine = OB.Taxes.Pos.translateTicket(
        receipt,
        line,
        taxCategory
      );
      const result = OB.Taxes.applyTaxes(
        ticketForEngine,
        OB.Taxes.Pos.ruleImpls
      );

      return result.lines[line.get('id')].taxes;
    },

    /**
     * Reads tax masterdata models from database and creates different caches to use them:
     *   OB.Taxes.Pos.ruleImpls: array with taxes including tax zone filter and sorted by validFromDate and default.
     *   OB.Taxes.Pos.taxCategoryBOM: array with bom tax categories.
     * Tax masterdata models should be read from database only here. Wherever discount data is needed, any of these caches should be used.
     */
    initCache: async function(callback) {
      if (OB.Taxes.Pos.isCalculatingCache) {
        return callback();
      }
      OB.Taxes.Pos.isCalculatingCache = true;
      const execution = OB.UTIL.ProcessController.start(
        'taxCacheInitialization'
      );

      const taxRateArrayPromise = await OB.App.MasterdataModels.TaxRate.orderedBy(
        ['validFromDate', 'default'],
        'desc'
      );
      const taxRateArray = taxRateArrayPromise.result;

      const taxZoneArrayPromise = await OB.App.MasterdataModels.TaxZone.orderedBy(
        'taxRateId'
      );
      const taxZoneArray = taxZoneArrayPromise.result;

      OB.Taxes.Pos.ruleImpls = taxRateArray.flatMap(taxRate =>
        taxZoneArray.some(taxZone => taxZone.taxRateId === taxRate.id)
          ? taxZoneArray
              .filter(taxZone => taxZone.taxRateId === taxRate.id)
              .map(taxZone => ({ ...taxZone, ...taxRate }))
          : { ...taxRate }
      );

      const taxCategoryBOMArrayPromise = await OB.App.MasterdataModels.TaxCategoryBOM.find();
      const taxCategoryBOMArray = taxCategoryBOMArrayPromise.result;
      OB.Taxes.Pos.taxCategoryBOM = taxCategoryBOMArray;

      OB.UTIL.ProcessController.finish('taxCacheInitialization', execution);
      callback();
      delete OB.Taxes.Pos.isCalculatingCache;
    }
  };
})();
