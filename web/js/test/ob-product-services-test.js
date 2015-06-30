/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

/*global QUnit */

QUnit.module('org.openbravo');

QUnit.test('Product Service js has been loaded', function () {
  QUnit.expect(1);
  QUnit.ok(OB.ProductServices, 'OB.ProductServices is present');
});

QUnit.test('Service Product Not Price Rule Based', function (assert) {

  QUnit.expect(2);

  var done = assert.async(),
      done2 = assert.async();
  // Transport Product
  var serviceProductId = '73AADD53EBA94C1EAC8B472A261F02AA';
  var record = {
    "id": "2471059F0EBE4D3892A145351F6523FC",
    "orderDate": "2015-06-22"
  };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    record: record,
    serviceProductId: serviceProductId
  }, {}, function (response, data, request) {
    QUnit.strictEqual(data.amount, 0, 'Amount returned is 0');
    done();
  });

  record = {
    "id": "F4DD56E866D04231B67AEB8CB6076779",
    "orderDate": "2015-06-22"
  };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    record: record,
    serviceProductId: serviceProductId
  }, {}, function (response, data, request) {
    QUnit.strictEqual(data.amount, 0, 'Amount returned is 0');
    done2();
  });
});

QUnit.test('Service Product Price Rule Based, Service Price Rule of type "Percentage"', function (assert) {

  QUnit.expect(2);
  var done = assert.async(),
      done2 = assert.async();
  // Insurance Product
  var serviceProductId = 'DE0F34D6A6F64E23BB03144CC5E0A4C0';
  var record = {
    "id": "2471059F0EBE4D3892A145351F6523FC",
    "orderDate": "2015-06-22"
  };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    record: record,
    serviceProductId: serviceProductId
  }, {}, function (response, data, request) {
    QUnit.strictEqual(data.amount, 3.00, 'Amount returned is 3.00');
    done();
  });

  record = {
    "id": "F4DD56E866D04231B67AEB8CB6076779",
    "orderDate": "2015-06-22"
  };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    record: record,
    serviceProductId: serviceProductId
  }, {}, function (response, data, request) {
    QUnit.strictEqual(data.amount, 105.00, 'Amount returned is 105.00');
    done2();
  });
});

QUnit.test('Service Product Price Rule Based, Service Price Rule of type "Ranges"', function (assert) {

  QUnit.expect(3);

  var done = assert.async(),
      done2 = assert.async(),
      done3 = assert.async();
  // Warranty Product
  var serviceProductId = 'D67EF9E66FF447E88176DF0C054A9D3F';
  var record1 = {
    "id": "2471059F0EBE4D3892A145351F6523FC",
    "orderDate": "2015-06-22"
  };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    record: record1,
    serviceProductId: serviceProductId
  }, {}, function (response, data, request) {
    QUnit.strictEqual(data.amount, 4.00, 'Amount returned is 4.00');
    done();
  });

  var record2 = {
    "id": "F4DD56E866D04231B67AEB8CB6076779",
    "orderDate": "2015-06-22"
  };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    record: record2,
    serviceProductId: serviceProductId
  }, {}, function (response, data, request) {
    QUnit.strictEqual(data.amount, 105.00, 'Amount returned is 105.00');
    done2();
  });

  var record3 = {
    "id": "0B2B25DF595447A5AC190634FB157F08",
    "orderDate": "2015-06-22"
  };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    record: record3,
    serviceProductId: serviceProductId
  }, {}, function (response, data, request) {
    QUnit.strictEqual(data.amount, 200.00, 'Amount returned is 200.00');
    done3();
  });
});

QUnit.test('Services missing configuration data', function (assert) {

  QUnit.expect(3);

  var done = assert.async(),
      done2 = assert.async(),
      done3 = assert.async();
  // Insurance Product
  var serviceProductId1 = 'DE0F34D6A6F64E23BB03144CC5E0A4C0',
      record1 = {
      "id": "8B450BB0A4844715A74F94041EAC774B",
      "orderDate": "2015-05-25"
      };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    record: record1,
    serviceProductId: serviceProductId1
  }, {}, function (response, data, request) {
    QUnit.strictEqual(data.message.text, 'No active and valid service price rule version found for Service Product: Insurance. Product: Final good A, Date: 25-05-2015', 'No active and valid service price rule version found for Service Product: Insurance. Product: Final good A, Date: 25-05-2015');
    done();
  });

  // Warranty Product
  var serviceProductId2 = 'D67EF9E66FF447E88176DF0C054A9D3F',
      record2 = {
      "id": "8B450BB0A4844715A74F94041EAC774B",
      "orderDate": "2015-05-25"
      };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    record: record2,
    serviceProductId: serviceProductId2
  }, {}, function (response, data, request) {
    QUnit.strictEqual(data.message.text, 'No active and valid price rule range found. Service Price Rule: Low Ranges , Amount Up To: 200', 'No active and valid price rule range found. Service Price Rule: Low Ranges , Amount Up To: 200');
    done2();
  });

  var record3 = {
    "id": "69BFBAFE6A9A46948DCE101D0520A4BF",
    "orderDate": "2015-05-25"
  };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    record: record3,
    serviceProductId: serviceProductId2
  }, {}, function (response, data, request) {
    QUnit.strictEqual(data.message.text, 'No active and valid price list version found for Service Product: Warranty. Product: Final good B, Date: 25-05-2015', 'No active and valid price list version found for Service Product: Warranty. Product: Final good B, Date: 25-05-2015');
    done3();
  });
});

QUnit.test('Services using Price List Including Taxes', function (assert) {

  QUnit.expect(1);

  var done = assert.async();
  // Warranty Product
  var serviceProductId = 'D67EF9E66FF447E88176DF0C054A9D3F',
      record = {
      "id": "E335455C37D3422D99298B6ADD26BBC9",
      "orderDate": "2015-06-26"
      };

  OB.RemoteCallManager.call('org.openbravo.common.actionhandler.ServiceRelatedLinePriceActionHandler', {
    record: record,
    serviceProductId: serviceProductId
  }, {}, function (response, data, request) {
    QUnit.strictEqual(data.amount, 6.00, 'Amount returned is 6.00');
    done();
  });
});