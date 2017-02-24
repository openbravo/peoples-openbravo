/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB*/

(function () {

  OB.UTIL.prepaymentRules = {};
  OB.UTIL.prepaymentRules['default'] = {
    execute: function (receipt, callback) {
      var prepaymentAmount = receipt.get('lines').reduce(function (memo, line) {
        if (line.get('obposCanbedelivered')) {
          return memo + line.get('gross');
        } else {
          return memo;
        }
      }, 0);
      callback(prepaymentAmount);
    }
  };

}());