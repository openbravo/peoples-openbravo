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
          discountedGrossAmount:
            line.get('discountedGross') || line.get('discountedGross') === 0
              ? line.get('discountedGross')
              : line.get('gross'),
          discountedNetAmount:
            line.get('discountedNet') || line.get('discountedNet') === 0
              ? line.get('discountedNet')
              : line.get('net'),
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

    const data = await OB.Taxes.Pos.loadData();
    Object.assign(OB.Taxes.Pos, data);

    OB.UTIL.ProcessController.finish('taxCacheInitialization', execution);
    callback();
    delete OB.Taxes.Pos.isCalculatingCache;
  };
})();
