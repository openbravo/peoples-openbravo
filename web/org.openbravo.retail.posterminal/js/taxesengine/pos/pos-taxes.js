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
    /**
     * Reads tax masterdata models from database and creates different caches to use them:
     *   OB.Discounts.Pos.ruleImpls: array with taxes including tax zone filter and sorted by validFromDate and default.
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

      const taxRateArrayPromise = await OB.App.MasterdataModels.TaxRate.find(
        new OB.App.Class.Criteria()
          .orderBy(['validFromDate', 'default'], 'desc')
          .build()
      );
      const taxRateArray = taxRateArrayPromise.result;

      const taxZoneArrayPromise = await OB.App.MasterdataModels.TaxZone.find();
      const taxZoneArray = taxZoneArrayPromise.result;

      const taxZoneArrayByTaxRate = OB.App.ArrayUtils.groupBy(
        taxZoneArray,
        'tax'
      );

      taxRateArray.forEach(
        taxRate =>
          (taxRate['taxZones'] = taxZoneArrayByTaxRate[taxRate.id] || [])
      );

      OB.Taxes.Pos.ruleImpls = taxRateArray;

      OB.UTIL.ProcessController.finish('taxCacheInitialization', execution);
      callback();
      delete OB.Taxes.Pos.isCalculatingCache;
    }
  };
})();
