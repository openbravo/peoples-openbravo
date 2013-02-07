/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B,$ */

(function () {

  OB = window.OB || {};
  OB.DEC = window.OB.DEC || {};

  var scale = 2;
  var roundingmode = BigDecimal.prototype.ROUND_HALF_UP;

  var toBigDecimal = function (a) {
      return new BigDecimal(a.toString());
      };

  var toNumber = function (big) {
      return parseFloat(big.setScale(scale, roundingmode).toString(), 10);
      };

  OB.DEC.Zero = toNumber(BigDecimal.prototype.ZERO);
  OB.DEC.One = toNumber(BigDecimal.prototype.ONE);
  //scale is now public and will be set when the currency data is retrieved
  OB.DEC.scale = scale;

  OB.DEC.isNumber = function (a) {
    return typeof (a) === 'number' && !isNaN(a);
  };

  OB.DEC.add = function (a, b) {
    return toNumber(toBigDecimal(a).add(toBigDecimal(b)));
  };

  OB.DEC.sub = function (a, b) {
    return toNumber(toBigDecimal(a).subtract(toBigDecimal(b)));
  };

  OB.DEC.mul = function (a, b) {
    return toNumber(toBigDecimal(a).multiply(toBigDecimal(b)));
  };

  OB.DEC.div = function (a, b) {
    return toNumber(toBigDecimal(a).divide(toBigDecimal(b), scale, roundingmode));
  };

  OB.DEC.compare = function (a) {
    return toBigDecimal(a).compareTo(BigDecimal.prototype.ZERO);
  };

  OB.DEC.number = function (jsnumber) {
    return jsnumber; // toNumber(toBigDecimal(jsnumber));
  };

  OB.DEC.setContext = function (s, r) {
    scale = s;
    roundingmode = r;
  };

  OB.DEC.toBigDecimal = function (a) {
    return toBigDecimal(a);
  };

  OB.DEC.toNumber = function (a) {
    return toNumber(a);
  };

}());