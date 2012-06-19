/*global _,$,Backbone,test,ok,expect,module,console */

module('Offline');

function errorCallback() {
  console.error(arguments);
}

var clientId = '23C59575B9CF467C9620760EB255B389';
var orgId = 'E443A31992CB4635AFCAEABE7183CE85';
var dsProducts = new OB.DS.DataSource(new OB.DS.Query(OB.Model.Product, clientId, orgId));

asyncTest('Load and cache products - WebSQL', function () {
  expect(2);

  function found(collection) {
    ok(collection, 'Collection is present');
    equals(collection.length, dsProducts.cache.length, collection.length + ' products cached');
    start();
  }

  function findAll() {
    OB.Dal.find(OB.Model.Product, null, found, function () {
      window.console.error(arguments);
    });
  }

  dsProducts.on('ready', function () {
    findAll();
  });

  dsProducts.load();
});

asyncTest('Cache product using localStorage', function () {
  var productList, ProductList = OB.Collection.ProductList.extend({
    localStorage: new Backbone.LocalStorage('OBPOS_Product')
  });

  expect(1);

  productList = new ProductList();

  _.each(dsProducts.cache, function (product) {
    productList.create(product);
  });

  equals(productList.length, dsProducts.cache.length, productList.length + ' products cached');

  start();
});