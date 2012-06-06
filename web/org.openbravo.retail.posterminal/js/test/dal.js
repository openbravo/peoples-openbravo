/*global _,$,Backbone,test,ok,expect,module,console */

module('Dal');

function errorCallback() {
  console.error(arguments);
}

test('Basic requirements', function () {
  expect(3);
  ok(_, 'Underscode is present');
  ok($, 'jQuery is present');
  ok(Backbone, 'Backbone is present');
});

test('API function availability', function () {
  expect(3);
  ok(OB.Dal.get, 'get function is available');
  ok(OB.Dal.save, 'save function is available');
  ok(OB.Dal.find, 'find function is available');
});

asyncTest('Query - Get all', function () {
  expect(2);

  function success(collection) {
    ok(collection, 'Collection is present');
    ok(collection && collection.length > 0, 'Total rows: ' + collection.length);
    console.log(arguments);
    start();
  }

  OB.Dal.find(OB.Model.TaxRate, null, success, errorCallback);
});

asyncTest('Query - Get one', function () {
  expect(2);

  function success(model) {
    ok(model, 'Model is present');
    ok(model && model.get, 'Model name: ' + model.get('name'));
    console.log(model);
    start();
  }

  OB.Dal.get(OB.Model.TaxRate, 'D61CD889CF2E42A7B46C935ACA0538FF', success, errorCallback);
});

asyncTest('Query - find one', function () {
  expect(2);

  function success(collection) {
    ok(collection, 'Collection is present');
    ok(collection.length > 0, 'Collection at 0: ' + collection.at(0).get('name'));
    console.log(arguments);
    start();
  }

  OB.Dal.find(OB.Model.TaxRate, {
    name: 'Servicios prestados exentos'
  }, success, errorCallback);
});

asyncTest('Query - save - update', function () {
  expect(1);

  function saveSuccess() {
    console.log(arguments);
    start();
  }

  function success(model) {
    ok(model, 'Model is present');
    model.attributes.name = model.attributes.name + ' --- test';
    OB.Dal.save(model, saveSuccess, errorCallback);
  }

  OB.Dal.get(OB.Model.TaxRate, 'D61CD889CF2E42A7B46C935ACA0538FF', success, errorCallback);
});