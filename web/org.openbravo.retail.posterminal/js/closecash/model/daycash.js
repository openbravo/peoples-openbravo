/*global B, Backbone, localStorage */

(function () {

  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};

  OB.MODEL.PaymentMethod = Backbone.Model.extend({
		_id: 'paymentmethod',
	    defaults : {
	      id: null,
	      name: null,
	      financialaccount: null,
	      expected: OB.DEC.Zero,
	      counted: OB.DEC.Zero
	    }
	  });
//DayCash.PaymentMethodCol Model.
  OB.MODEL.PaymentMethodCol = Backbone.Collection.extend({
    model: OB.MODEL.PaymentMethod
  });
  OB.MODEL.DayCash = Backbone.Model.extend({
		_id: 'modeldaycash',
	    defaults : {
	      paymentmethods: new OB.MODEL.PaymentMethodCol(),
	      totalExpected: OB.DEC.Zero,
	      totalCounted: OB.DEC.Zero,
	      totalDifference: OB.DEC.Zero,
	      step: 0
	    }
	  });
}());