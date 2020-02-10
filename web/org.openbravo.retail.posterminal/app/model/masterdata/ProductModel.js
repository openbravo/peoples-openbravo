/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
(function ProductDefinition() {
  class Product extends OB.App.Class.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Class.Index({
          name: 'productCategoryBrowse_idx',
          properties: [
            { property: 'productCategory' },
            { property: 'generic_product_id', isNullable: true }
          ]
        }),
        new OB.App.Class.Index({
          name: 'bestsellerBrowse_idx',
          properties: [
            { property: 'bestseller', isBoolean: true },
            { property: 'generic_product_id', isNullable: true }
          ]
        }),
        new OB.App.Class.Index({
          name: 'productCategorySearch_idx',
          properties: [
            { property: 'productCategory' },
            { property: 'isGeneric', isBoolean: true }
          ]
        }),
        new OB.App.Class.Index({
          name: 'productCategoryGenericSearch_idx',
          properties: [
            { property: 'isGeneric', isBoolean: true },
            { property: 'generic_product_id', isNullable: true },
            { property: 'productCategory' }
          ]
        }),
        new OB.App.Class.Index({
          name: 'bestsellerGenericSearch_idx',
          properties: [
            { property: 'bestseller', isBoolean: true },
            { property: 'isGeneric', isBoolean: true },
            { property: 'generic_product_id', isNullable: true }
          ]
        }),
        new OB.App.Class.Index({
          name: 'productAndProposalType_idx',
          properties: [
            { property: 'productType' },
            { property: 'proposalType' }
          ]
        }),
        new OB.App.Class.Index({
          name: 'productUPC_idx',
          properties: [{ property: 'uPCEAN' }]
        })
      ];
    }
  }
  OB.App.MasterdataController.registerModel(Product);
})();
