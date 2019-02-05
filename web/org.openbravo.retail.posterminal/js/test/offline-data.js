/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global _,Backbone,asyncTest,ok,expect,module,start,equals*/

module('Offline');

var dsProducts = new OB.DS.DataSource(new OB.DS.Request(OB.Model.Product));

asyncTest('Load and cache products - WebSQL', function () {
  expect(2);

  function found(collection) {
    ok(collection, 'Collection is present');
    equals(collection.length, dsProducts.cache.length, collection.length + ' products cached');
    start();
  }

  function findAll() {
    OB.Dal.find(OB.Model.Product, null, found, function () {
      OB.error(arguments);
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