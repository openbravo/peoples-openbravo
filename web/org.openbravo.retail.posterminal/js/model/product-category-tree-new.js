/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  var ProductCategoryAndTree = OB.Data.ExtensibleModel.extend({
    modelName: 'ProductCategoryAndTree',
    tableName: 'm_product_category_tree_new',
    entityName: 'ProductCategoryAndTree',
    source: 'org.openbravo.retail.posterminal.master.ProductCategoryAndTree',
    dataLimit: OB.Dal.DATALIMIT,
    includeTerminalDate: true
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
