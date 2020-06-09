/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the ProductPackProvider class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */
(function ProductPackProviderDefinition() {
  /**
   * Used to retrieve the ProductPack instance related to a product (if any).
   */
  class ProductPackProvider {
    constructor() {
      this.packs = {};
    }

    /**
     * Registers a ProductPack class
     * @param {string} id - The id that identifies the ProductPack
     * @param {Class<ProductPack>} Pack - The ProductPack class to be registered
     */
    registerPack(id, Pack) {
      this.packs[id] = Pack;
    }

    /**
     * Given a product pack retrieves an instance of its corresponding ProductPack
     * @param {object} product - A product
     * @return {ProductPack} - The ProductPack related to the provided product or undefined if not found
     */
    getPack(product) {
      if (!product.ispack) {
        return undefined;
      }
      const Pack = this.packs[product.productCategory];
      if (!Pack) {
        return undefined;
      }
      return new Pack(product);
    }
  }

  OB.App.ProductPackProvider = new ProductPackProvider();
})();
