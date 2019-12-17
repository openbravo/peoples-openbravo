/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  class ProductCategoryTree extends OB.App.MasterdataModel {
    constructor() {
      super();
      this.indices = [
        new OB.App.Index({
          name: 'productcategorytree_parentId_idx',
          properties: [{ property: 'parentId' }]
        }),
        new OB.App.Index({
          name: 'productcategorytree_id_idx',
          properties: [{ property: 'id' }]
        }),
        new OB.App.Index({
          name: 'productcategorytree_crossstore_idx',
          properties: [{ property: 'crossStore' }]
        })
      ];
    }
  }
  OB.App.MasterdataController.registerModel(ProductCategoryTree);

  var ProductCategoryAndTree = OB.Data.ExtensibleModel.extend({
    modelName: 'ProductCategoryAndTree',
    tableName: 'm_product_category_tree_new',
    entityName: 'ProductCategoryAndTree',
    source: 'org.openbravo.retail.posterminal.master.ProductCategoryAndTree',
    dataLimit: OB.Dal.DATALIMIT,
    includeTerminalDate: true,
    indexDBModel: ProductCategoryTree.prototype.getName()
  });

  ProductCategoryAndTree.addProperties([
    {
      name: 'id',
      column: 'm_product_category_id',
      primaryKey: true,
      type: 'TEXT'
    },
    {
      name: 'searchKey',
      column: 'value',
      type: 'TEXT'
    },
    {
      name: 'name',
      column: 'name',
      type: 'TEXT'
    },
    {
      name: 'img',
      column: 'ad_image_id',
      type: 'TEXT'
    },
    {
      name: '_identifier',
      column: '_identifier',
      type: 'TEXT'
    },
    {
      name: 'summaryLevel',
      column: 'isSummary',
      type: 'TEXT'
    },
    {
      name: 'realCategory',
      column: 'real_category',
      type: 'TEXT'
    },
    {
      name: 'crossStore',
      column: 'crossStore',
      type: 'BOOL'
    },
    {
      name: 'treeNodeId',
      column: 'treenode_id',
      type: 'TEXT'
    },
    {
      name: 'categoryId',
      column: 'category_id',
      type: 'TEXT'
    },
    {
      name: 'parentId',
      column: 'parent_id',
      type: 'TEXT'
    },
    {
      name: 'seqNo',
      column: 'seqno',
      type: 'NUMERIC'
    },
    {
      name: 'childs',
      column: 'childs'
    }
  ]);

  OB.Data.Registry.registerModel(ProductCategoryAndTree);
})();
