(function () {

  var Product = Backbone.Model.extend({}),

  Products = Backbone.Collection.extend({
    model: Product,
    url: '../../org.openbravo.service.datasource/Product',
    parse: function (resp) {
      return (resp && resp.response && resp.response.data) || [];
    }
  });

  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};

  // expose Products collection
  OB.Model.Products = Products;
}())