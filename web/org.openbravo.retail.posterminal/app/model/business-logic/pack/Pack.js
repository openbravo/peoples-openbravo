/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the Pack class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */
(function PackDefinition() {
  /**
   * Implements the logic to handle an standard product pack.
   */
  class Pack extends OB.App.Class.ProductPack {
    async getProducts() {
      const discount = OB.Discounts.Pos.ruleImpls.find(
        d => d.id === this.product.id
      );

      this.checkIsExpired(discount);

      const productRetrievals = discount.products.map(async discountProduct => {
        const product = await OB.App.MasterdataModels.Product.withId(
          discountProduct.product.id
        );
        return {
          product,
          qty: discountProduct.obdiscQty,
          belongsToPack: true
        };
      });

      const pack = await Promise.all(productRetrievals);
      return pack;
    }

    // eslint-disable-next-line class-methods-use-this
    checkIsExpired(discount) {
      if (discount.endingDate && discount.endingDate.length > 0) {
        const objDate = new Date(discount.endingDate);
        const now = new Date();
        const nowWithoutTime = new Date(now.toISOString().split('T')[0]);
        if (nowWithoutTime > objDate) {
          throw new OB.App.Class.ErrorMessage(
            'OBPOS_PackExpired_header',
            'OBPOS_PackExpired_body',
            [
              // eslint-disable-next-line no-underscore-dangle
              discount._identifier,
              objDate.toLocaleDateString()
            ]
          );
        }
      }
    }
  }

  OB.App.ProductPackProvider.registerPack(
    'BE5D42E554644B6AA262CCB097753951',
    Pack
  );
})();
