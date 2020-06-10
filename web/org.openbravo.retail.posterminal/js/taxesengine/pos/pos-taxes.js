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
  OB.Taxes.Pos = OB.Taxes.Pos || {};

  const translateReceipt = receipt => {
    return {
      id: receipt.get('id'),
      orderDate: receipt.get('orderDate'),
      priceIncludesTax: receipt.get('priceIncludesTax'),
      cashVAT: receipt.get('cashVAT'),
      country: receipt.get('country'),
      region: receipt.get('region'),
      businessPartner: {
        id: receipt.get('bp').id,
        taxCategory: receipt.get('bp').get('taxCategory'),
        taxExempt: receipt.get('bp').get('taxExempt')
      },
      lines: receipt.get('lines').map(line => {
        return {
          id: line.get('id'),
          lineRate: line.get('lineRate'),
          country: line.get('country'),
          region: line.get('region'),
          grossUnitAmount:
            line.get('discountedGross') || line.get('discountedGross') === 0
              ? line.get('discountedGross')
              : line.get('gross'),
          netUnitAmount:
            line.get('discountedNet') || line.get('discountedNet') === 0
              ? line.get('discountedNet')
              : line.get('net'),
          qty: line.get('qty'),
          taxExempt: line.get('taxExempt'),
          product: {
            id: line.get('product').id,
            taxCategory: line.get('product').get('taxCategory'),
            productBOM: line.get('product').get('productBOM')
          }
        };
      })
    };
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
   * Finds the list of taxes that apply to given receipt.
   * @param {Object} receipt - The receipt that taxes will apply to.
   */
  OB.Taxes.Pos.calculateTaxes = receipt => {
    if (!OB.Taxes.Pos.ruleImpls) {
      throw 'Local tax cache is not yet initialized, execute: OB.Taxes.Pos.initCache()';
    }

    const ticket = translateReceipt(receipt);
    const taxes = OB.Taxes.applyTaxes(ticket, OB.Taxes.Pos.ruleImpls);
    return translateTaxes(taxes);
  };

  /**
   * Reads tax masterdata models from database and creates different caches to use them:
   *   OB.Taxes.Pos.ruleImpls: array with TaxRate model including TaxZone information.
   *   OB.Taxes.Pos.taxCategory: array with TaxCategory model.
   *   OB.Taxes.Pos.taxCategoryBOM: array with TaxCategoryBOM model.
   * Tax masterdata models should be read from database only here. Wherever tax data is needed, any of these caches should be used.
   */
  OB.Taxes.Pos.initCache = async callback => {
    if (OB.Taxes.Pos.isCalculatingCache) {
      return callback();
    }
    OB.Taxes.Pos.isCalculatingCache = true;
    const execution = OB.UTIL.ProcessController.start('taxCacheInitialization');

    const data = await OB.Taxes.Pos.loadData();
    Object.assign(OB.Taxes.Pos, data);

    OB.UTIL.ProcessController.finish('taxCacheInitialization', execution);
    callback();
    delete OB.Taxes.Pos.isCalculatingCache;
  };
})();
