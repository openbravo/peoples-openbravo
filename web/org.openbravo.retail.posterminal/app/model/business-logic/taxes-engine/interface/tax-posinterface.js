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
   * Finds the list of taxes that apply to given ticket.
   *
   * @param {object} ticket - The ticket whose taxes will be calculated
   * @param {object[]} rules - List of tax rules taken into account for tax calculation
   * @returns The ticket with the result of the taxes calculation
   */
  OB.Taxes.Pos.applyTaxes = (ticket, rules) => {
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
      newTaxes.taxes = translateTaxArray(taxes.taxes);
      newTaxes.lines = taxes.lines.map(line => {
        const newLine = { ...line };
        newLine.taxes = translateTaxArray(line.taxes);
        newLine.bomLinesTaxes = line.bomLinesTaxes;
        return newLine;
      });
      return newTaxes;
    };

    const taxes = OB.Taxes.applyTaxes(ticket, rules);
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
