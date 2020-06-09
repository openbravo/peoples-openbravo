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
    constructor(product) {
      this.product = product;
    }

    /**
     * Processes the pack in order to retrieve the products that forms it
     * @return {Object[]} product - An array of the products that belongs to the pack
     * @throws {Error} - If an error happends during the pack processing. The thrown error may contain:
     *             * title: a short decription of the error
     *             * message: the error message
     *             * messageParams: the dynamic message parameters (if any)
     */
    async process() {
      const data = await this.getExternalData();
      return this.getProducts(data);
    }

    /**
     * To be overriden when it is necessary to retrieve external data to calculate the pack contents.
     * For example, when requesting data to the user.
     * @return {Object} data - An array of the products that belongs to the pack
     */
    // eslint-disable-next-line class-methods-use-this
    async getExternalData() {
      return null;
    }

    /**
     * @param data - Data that may be used to calculate the products to be retrieved
     * @return {Object[]} product - An array of the products that belongs to the pack
     */
    // eslint-disable-next-line no-unused-vars
    async getProducts(data) {
      throw new Error(
        `getProducts() function is not implemented in abstract ${this.constructor.name}`
      );
    }
  }

  OB.App.Class.ProductPack = ProductPack;
})();
