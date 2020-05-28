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
