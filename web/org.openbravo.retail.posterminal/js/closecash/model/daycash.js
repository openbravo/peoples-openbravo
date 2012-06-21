/*global B, _ , Backbone, localStorage */

(function () {

  OB = window.OB || {};
  OB.MODEL = window.OB.MODEL || {};

  OB.MODEL.PaymentMethod = Backbone.Model.extend({
		_id: 'paymentmethod',
	    defaults : {
	      id: null,
	      name: null,
	      expected: OB.DEC.Zero,
	      counted: OB.DEC.Zero
	    }
	  });
//DayCash.PaymentMethodCol Model.
  OB.MODEL.PaymentMethodCol = Backbone.Collection.extend({
    model: OB.MODEL.PaymentMethod,
    serializeToJSON: function () {
        var jsonpayment = JSON.parse(JSON.stringify(this.toJSON()));

        // remove not needed members
        delete jsonpayment.undo;

        _.forEach(jsonpayment, function (item) {
          item.difference = item.counted - item.expected;
          item.paymentTypeId = item.id;

          delete item.id;
          delete item.name;
          delete item.counted;
          delete item._id;
        });
        return jsonpayment;
      }
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