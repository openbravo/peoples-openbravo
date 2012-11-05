/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, _ , Backbone, localStorage */

(function () {

  OB = window.OB || {};
  OB.Model = window.OB.Model || {};
  OB.Collection = window.OB.Collection || {};

  OB.Model.PaymentMethod = Backbone.Model.extend({
    defaults: {
      id: null,
      name: null,
      expected: OB.DEC.Zero,
      counted: OB.DEC.Zero
    }
  });

  //DayCash.PaymentMethodCol Model.
  OB.Collection.PaymentMethodList = Backbone.Collection.extend({
    model: OB.Model.PaymentMethod,
    serializeToJSON: function () {
      var jsonpayment = this.toJSON();

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

  OB.Model.DayCash = Backbone.Model.extend({
    defaults: {
      totalExpected: OB.DEC.Zero,
      totalCounted: OB.DEC.Zero,
      totalDifference: OB.DEC.Zero,
      step: 0,
      allowedStep: 0
    },

    initialize: function () {
      this.set('paymentmethods', new OB.Model.PaymentMethodList());
    }
  });
}());