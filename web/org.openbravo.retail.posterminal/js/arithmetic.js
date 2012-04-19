/*global define,$ */

define([], function (B) {
  
  OB = window.OB || {};
  OB.DEC = window.OB.DEC || {};  
  
  var scale = 2;
  var roundingmode = BigDecimal.prototype.ROUND_HALF_EVEN;
  
  var toBigDecimal = function (a) {
    return new BigDecimal(a.toString());
  };
  
  var toNumber = function (big) {
    return parseFloat(big.setScale(scale, roundingmode).toString());
  };
    
  OB.DEC.Zero = toNumber(BigDecimal.prototype.ZERO);
  OB.DEC.One = toNumber(BigDecimal.prototype.ONE);
    
  OB.DEC.isNumber = function (a) {
    return typeof(a) === 'number' && !isNaN(a);
  };
  
  OB.DEC.add = function (a, b) {
    return toNumber(toBigDecimal(a).add(toBigDecimal(b)));
  };
  
  OB.DEC.sub = function (a, b) {
    return toNumber(toBigDecimal(a).subtract(toBigDecimal(b)));
  };  
  
  OB.DEC.mul = function (a, b) {
    return toNumber(toBigDecimal(a).multiply(toBigDecimal(b)));;
  }; 
  
  OB.DEC.div = function (a, b) {
    return toNumber(toBigDecimal(a).divide(toBigDecimal(b)));;
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
  
});
  