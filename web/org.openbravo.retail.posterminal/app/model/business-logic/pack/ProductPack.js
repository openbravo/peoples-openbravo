/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the base ProductPack class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */
(function ProductPackDefinition() {
  /**
   * It should be extended to define the logic to handle a product pack
   */
  class ProductPack {
    /**
     * @param {object} product - The product pack information (product, options and attrs)
     * @param {object} discountRules - The available discount rules
     */
    constructor(productInfo, discountRules) {
      this.product = productInfo.product;
      this.options = productInfo.options;
      this.attrs = productInfo.attrs;
      this.discountRules = discountRules;
    }

    /**
     * Processes the pack in order to retrieve the products that forms it
     * @return {Object} products - An array with the information of the products that belong to the pack
     * @throws {TranslatableError} - If an error happens during the pack processing
     */
    async process() {
      const products = await this.getProducts();
      return products.map(p => {
        const options = p.options || {};
        const attrs = p.attrs || {};
        return {
          ...p,
          options: { ...this.options, ...options },
          attrs: { ...this.attrs, ...attrs }
        };
      });
    }

    /**
     * This function is implemented by the subclasses with the logic to retrieve the products of the pack
     * @return {Object[]} - An array of the products that belongs to the pack
     */
    async getProducts() {
      throw new Error(
        `getProducts() function is not implemented in abstract ${this.constructor.name}`
      );
    }
  }

  OB.App.Class.ProductPack = ProductPack;
})();
