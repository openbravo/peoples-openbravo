/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  var ProductCategoryTree = OB.Data.ExtensibleModel.extend({
    modelName: 'ProductCategoryTree',
    tableName: 'm_product_category_tree',
    entityName: 'ProductCategoryTree',
    source: 'org.openbravo.retail.posterminal.master.CategoryTree',
    dataLimit: OB.Dal.DATALIMIT,
    includeTerminalDate: true,
    createBestSellerCategory: function () {
      this.set('id', 'OBPOS_bestsellercategory');
      this.set('categoryId', 'OBPOS_bestsellercategory');
      this.set('parentId', '0');
    }
  });

  ProductCategoryTree.addProperties([{
    name: 'id',
    column: 'id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'categoryId',
    column: 'category_id',
    type: 'TEXT'
  }, {
    name: 'parentId',
    column: 'parent_id',
    type: 'TEXT'
  }, {
    name: 'seqNo',
    column: 'seqno',
    type: 'NUMERIC'
  }]);

  OB.Data.Registry.registerModel(ProductCategoryTree);
}());