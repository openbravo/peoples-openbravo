/*global Backbone */

(function () {
  var Product = Backbone.Model.extend({
    modelName: 'Product',
    tableName: 'm_product',
    entityName: 'Product'
  });

  var Products = Backbone.Collection.extend({
    model: Product
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};

  window.OB.Model.Product = Product;
  window.OB.Collection.Products = Products;
}());