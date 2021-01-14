/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
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
          name: 'productCategoryBrowseGeneric_idx',
          properties: [
            { property: 'productCategory' },
            { property: 'generic_product_id', isNullable: true }
          ]
        }),
        new OB.App.Class.Index({
          name: 'bestsellerBrowseGeneric_idx',
          properties: [
            { property: 'bestseller', isBoolean: true },
            { property: 'generic_product_id', isNullable: true }
          ]
        }),
        new OB.App.Class.Index({
          name: 'productCategoryBrowse_idx',
          properties: [{ property: 'productCategory' }]
        }),
        new OB.App.Class.Index({
          name: 'bestsellerBrowse_idx',
          properties: [{ property: 'bestseller', isBoolean: true }]
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
        }),
        new OB.App.Class.Index({
          name: 'productIsGeneric_idx',
          properties: [{ property: 'isGeneric', isBoolean: true }]
        }),
        new OB.App.Class.Index({
          name: 'productServicesFilter_idx',
          properties: [
            { property: 'isGeneric', isBoolean: true },
            { property: 'productType', isNullable: true },
            {
              property: 'isLinkedToProduct',
              isBoolean: true
            },
            { property: 'proposalType', isNullable: true }
          ]
        }),
        new OB.App.Class.Index({
          name: 'productServicesFilter2_idx',
          properties: [
            { property: 'isGeneric', isBoolean: true },
            { property: 'productType', isNullable: true },
            {
              property: 'isLinkedToProduct',
              isBoolean: true
            },
            { property: 'availableForMultiline', isBoolean: true },
            { property: 'proposalType', isNullable: true }
          ]
        }),
        new OB.App.Class.Index({
          name: 'productIncludeProducts_idx',
          properties: [
            { property: 'includeProducts', isBoolean: true, isNullable: true }
          ]
        }),
        new OB.App.Class.Index({
          name: 'productIncludeProductCategories_idx',
          properties: [
            {
              property: 'includeProductCategories',
              isBoolean: true,
              isNullable: true
            }
          ]
        }),
        new OB.App.Class.Index({
          name: 'productHasServicesFilter_idx',
          properties: [
            { property: 'productType', isNullable: true },
            {
              property: 'isLinkedToProduct',
              isBoolean: true
            },
            {
              property: 'obrdmIsdeliveryservice',
              isBoolean: true
            }
          ]
        })
      ];
      this.searchProperties = [
        '_identifier',
        'uPCEAN',
        'searchkey',
        'bestseller',
        'productCategory',
        'isGeneric',
        'characteristicDescriptionSearch',
        'listPrice',
        'ispack',
        'generic_product_id'
      ];
    }

    getName() {
      return 'Product';
    }

    isRemote() {
      return OB.App.Security.hasPermission('OBPOS_remote.product');
    }
  }
  OB.App.MasterdataController.registerModel(Product);
})();
