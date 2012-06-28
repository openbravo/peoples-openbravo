/*global Backbone */

(function () {

  var ProductCategory = Backbone.Model.extend({
    modelName: 'ProductCategory',
    tableName: 'm_product_category',
    entityName: 'ProductCategory',
    source: 'org.openbravo.retail.posterminal.master.Category',
    properties: [
     'id',
     'searchKey',
     'name',
     'obposImage',
     '_identifier',
     '_idx'
    ],
    propertyMap: {
     'id': 'm_product_category_id',
     'searchKey': 'value',
     'name': 'name',
     'obposImage': 'em_obpos_image_id',
     '_identifier': '_identifier',
     '_idx': '_idx'
    },
    createStatement: 'CREATE TABLE IF NOT EXISTS m_product_category (m_product_category_id TEXT PRIMARY KEY , value TEXT , name TEXT , em_obpos_image_id TEXT , _identifier TEXT , _idx NUMERIC)',
    dropStatement: 'DROP TABLE IF EXISTS m_product_category',
    insertStatement: 'INSERT INTO m_product_category(m_product_category_id, value, name, em_obpos_image_id, _identifier, _idx)  VALUES (?, ?, ?, ?, ?, ?)',
    updateStatement: ''
  });

  var ProductCategoryList = Backbone.Collection.extend({
    model: ProductCategory
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.ProductCategory = ProductCategory;
  window.OB.Collection.ProductCategoryList = ProductCategoryList;
}());