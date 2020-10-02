/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _ */

(function() {
  OB.UTIL = window.OB.UTIL || {};

  OB.UTIL.closeCashAddPaymentWithMovement = function(
    paymentWithMovement,
    values
  ) {
    _.each(values, function(value) {
      var searchKey = value.get('searchKey');
      var item = _.find(paymentWithMovement, function(p) {
        return p.searchKey === searchKey;
      });
      if (!item && value.get('paymentMethodId')) {
        paymentWithMovement.push({
          searchKey: searchKey
        });
        var paymentMethod = _.find(OB.MobileApp.model.get('payments'), function(
          p
        ) {
          return p.payment.id === value.get('paymentMethodId');
        });
        if (paymentMethod) {
          searchKey = paymentMethod.payment.searchKey;
          item = _.find(paymentWithMovement, function(p) {
            return p.searchKey === searchKey;
          });
        }
      }
      if (!item && value.get('amount') !== 0) {
        paymentWithMovement.push({
          searchKey: searchKey
        });
      }
    });
  };

  OB.UTIL.closeCashAddPaymentWithSummaryMovement = function(
    paymentWithMovement,
    values
  ) {
    _.each(values, function(value) {
      var item = _.find(paymentWithMovement, function(p) {
        return p.searchKey === value.get('searchKey');
      });
      if (!item && value.get('value') !== 0) {
        paymentWithMovement.push({
          searchKey: value.get('searchKey')
        });
      }
    });
  };

  OB.UTIL.closeCashGetPaymentWithMovement = function(
    paymentWithMovement,
    values
  ) {
    var filtered = _.filter(values, function(value) {
      var item = _.find(paymentWithMovement, function(p) {
        return p.searchKey === value.get('searchKey');
      });
      return item !== undefined;
    });
    return filtered;
  };
})();
