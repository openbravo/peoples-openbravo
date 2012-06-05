/*global _,$,Backbone,test,ok,expect,module */

module('Dal');

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
  ok(OB.Dal.query, 'query function is available');
});