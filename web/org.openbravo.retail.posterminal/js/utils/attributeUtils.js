/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, Backbone, Promise */

(function () {

  OB.UTIL = window.OB.UTIL || {};
  OB.UTIL.AttributeUtils = OB.UTIL.AttributeUtils || {};

  OB.UTIL.AttributeUtils.generateDescriptionBasedOnJson = function (jsonStringAttValues) {
    var jsonAttValues = JSON.parse(jsonStringAttValues);
    var objToReturn = {
      keyValue: [],
      description: ''
    };
    var attSetInstanceDescription = '';
    _.each(
    _.keys(jsonAttValues), function (key) {
      var currentValue = jsonAttValues[key];
      var attValueContent = '';
      if (jsonAttValues && currentValue && currentValue.value && _.isString(currentValue.value) && currentValue.value.length > 0) {
        attValueContent += currentValue.label ? currentValue.label : currentValue.name;
        attValueContent += ': ';
        attValueContent += currentValue.value;
        objToReturn.keyValue.push(attValueContent);
        attSetInstanceDescription += currentValue.value + "_";
      }
    }, this);
    if (attSetInstanceDescription && attSetInstanceDescription.length > 1) {
      attSetInstanceDescription = attSetInstanceDescription.substring(0, attSetInstanceDescription.length - 1);
    }
    objToReturn.description = attSetInstanceDescription;
    return objToReturn;
  };
}());