/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

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

asyncTest('Load models - WebSQL', function () {
  var queue = {},
      triggerNext = false;

  _.each(_.keys(OB.Model), function (m) {
    var model = OB.Model[m];
    var ds = new OB.DS.DataSource(new OB.DS.Request(model, '23C59575B9CF467C9620760EB255B389', 'E443A31992CB4635AFCAEABE7183CE85'));

    ds.on('ready', function () {

      queue[ds.request.source] = true;

      //reduce
      triggerNext = _.reduce(queue, function (memo, val) {
        return memo && val;
      }, true);

      if (triggerNext) {
        start();
      }
    });
    ds.load();
    //source
    queue[ds.request.source] = false;
  });

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

  //using simplified API, defaults to equal
  OB.Dal.find(OB.Model.TaxRate, {
    'taxSearchKey': 'IVA18'
  }, success, errorCallback);
});

asyncTest('Query - find one using contains', function () {
  expect(2);

  function success(collection) {
    ok(collection, 'Collection is present');
    ok(collection.length > 0, 'Collection at 0: ' + collection.at(0).get('name'));
    console.log(arguments);
    start();
  }

  //using simplified API, defaults to equal
  OB.Dal.find(OB.Model.TaxRate, {
    'name': {
      operator: OB.Dal.CONTAINS,
      value: '18'
    }
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

asyncTest('Query - save - insert', function () {
  var randomRate = Math.floor(Math.random() * (60 - 2)) + 1,
      rateObj = new OB.Model.TaxRate();

  rateObj.set('rate', randomRate);

  expect(4);

  function found(coll) {
    ok(coll, 'Collection is present');
    ok(coll.length > 0, 'Record found');
    ok(coll.at(0).get('rate') === randomRate, 'Random rate: ' + randomRate);
    console.log(coll);
    start();
  }

  function saveSuccess(tx) {
    ok(tx, 'Transaction is present');
    // using operator and value
    OB.Dal.find(OB.Model.TaxRate, {
      'rate': {
        operator: OB.Dal.EQ,
        value: randomRate
      }
    }, found, errorCallback);
  }


  OB.Dal.save(rateObj, saveSuccess, errorCallback);
});