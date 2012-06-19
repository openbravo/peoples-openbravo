/*global B, Backbone, localStorage */

(function () {

  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};

  // Sales.OrderLine Model
  OB.MODEL.DayCash = Backbone.Model.extend({
	_id: 'modeldaycash',
    defaults : {
      paymentmethod: null,
      expected: '122',
      counted: OB.DEC.Zero,
      paymethods: null,
      step: 0
    }
  });

}());