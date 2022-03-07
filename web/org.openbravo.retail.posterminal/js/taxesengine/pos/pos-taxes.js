/*
 ************************************************************************************
 * Copyright (C) 2020-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.Taxes = OB.Taxes || {};
  OB.Taxes.Pos = OB.Taxes.Pos || {};

  /**
   * Finds the list of taxes that apply to given receipt.
   * @param {Object} receipt - The receipt that taxes will apply to.
   */
  OB.Taxes.Pos.calculateTaxes = receipt => {
    if (!OB.Taxes.Pos.ruleImpls) {
      throw 'Local tax cache is not yet initialized, execute: OB.Taxes.Pos.initCache()';
    }

    const ticket = {
      ...receipt.toJSON(),
      businessPartner: receipt.get('bp').toJSON(),
      lines: receipt.get('lines').map(line => {
        return {
          ...line.toJSON(),
          product: line.get('product').toJSON()
        };
      })
    };
    return OB.Taxes.Pos.applyTaxes(ticket, OB.Taxes.Pos.ruleImpls);
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

    try {
      const data = await OB.Taxes.Pos.loadData();
      Object.assign(OB.Taxes.Pos, data);
    } catch (e) {
      OB.App.View.DialogUIHandler.askConfirmation({
        title: '$OBMOBC_LblWarning',
        message: `$${e.message}`,
        hideCancel: true
      });
    }

    OB.UTIL.ProcessController.finish('taxCacheInitialization', execution);
    callback();
    delete OB.Taxes.Pos.isCalculatingCache;
  };
})();
