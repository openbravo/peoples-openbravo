/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the StockChecker class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

(function StockCheckerDefinition() {
  /**
   * A singleton class used to retrieve information about the product stock.
   */
  class StockChecker {
    /**
     * Checks if there is stock of a particular product
     * @param {object} product - A product object
     * @param {number} qty - The quantity to be checked
     * @param {object} settings - additional information used to get the stock
     */
    // eslint-disable-next-line class-methods-use-this
    async hasStock(product, qty, settings = {}) {
      if (!OB.OBPOSPointOfSale) {
        return true;
      }
      try {
        const hasStock = await OB.OBPOSPointOfSale.StockChecker.checkLineStock(
          product,
          qty,
          settings
        );
        return hasStock;
      } catch (err) {
        throw new Error(
          `Could not check stock for product ${product.id}: ${err}`
        );
      }
    }
  }

  OB.App.StockChecker = new StockChecker();
})();
