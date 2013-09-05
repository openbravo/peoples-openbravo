/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  var ProductCategory = OB.Data.ExtensibleModel.extend({
    modelName: 'ProductCategory',
    tableName: 'm_product_category',
    entityName: 'ProductCategory',
    source: 'org.openbravo.retail.posterminal.master.Category',
    dataLimit: 300,
    includeTerminalDate: true,
    createBestSellerCategory: function () {
      this.set('id', 'OBPOS_bestsellercategory');
      this.set('searchKey', 'bestseller');
      this.set('name', OB.I18N.getLabel('OBPOS_bestSellerCategory'));
      this.set('img', 'iconBestSellers');
      this.set('_identifier', this.get('name'));
    }
  });

  ProductCategory.addProperties([{
    name: 'id',
    column: 'm_product_category_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'searchKey',
    column: 'value',
    type: 'TEXT'
  }, {
    name: 'name',
    column: 'name',
    type: 'TEXT'
  }, {
    name: 'img',
    column: 'ad_image_id',
    type: 'TEXT'
  }, {
    name: '_identifier',
    column: '_identifier',
    type: 'TEXT'
  }]);

  OB.Data.Registry.registerModel(ProductCategory);
}());