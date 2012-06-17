/*global _,$,Backbone,test,ok,expect,module,console */

module('Offline');

function errorCallback() {
  console.error(arguments);
}

var clientId = '23C59575B9CF467C9620760EB255B389';
var orgId = 'E443A31992CB4635AFCAEABE7183CE85';
var dsProducts = new OB.DS.DataSource(new OB.DS.Query(OB.Model.Product, clientId, orgId));

asyncTest('Load products', function () {
  expect(1);

  dsProducts.on('ready', function () {
    ok(this.cache, this.cache.length + ' products loaded');
    start();
  });

  dsProducts.load();
});