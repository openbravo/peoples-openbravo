/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function ProductPriceDefinition() {
  class ProductPrice extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'pricelist_product_idx',
          properties: [
            { property: 'm_pricelist_id' },
            { property: 'm_product_id' }
          ]
        })
      ];
      this.setPaginationById(true);
    }
  }
  OB.App.MasterdataController.registerModel(ProductPrice);
})();
