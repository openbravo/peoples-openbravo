/*
 ************************************************************************************
 * Copyright (C) 2017-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.UTIL = window.OB.UTIL || {};
  OB.UTIL.AttributeUtils = OB.UTIL.AttributeUtils || {};

  OB.UTIL.AttributeUtils.generateDescriptionBasedOnJson = function(
    jsonStringAttValues
  ) {
    var key,
      attributesList = [],
      jsonAttValues = JSON.parse(jsonStringAttValues),
      attSetInstanceDescription = '',
      standardAttributes = ['lot', 'serialno', 'guaranteedate'],
      objToReturn = {
        keyValue: []
      };

    for (key in jsonAttValues) {
      if (
        jsonAttValues.hasOwnProperty(key) &&
        standardAttributes.indexOf(key) === -1
      ) {
        attributesList.push(jsonAttValues[key]);
      }
    }
    attributesList.sort(function(a, b) {
      return a.name.localeCompare(b.name);
    });

    _.each(standardAttributes, function(attribute) {
      if (
        jsonAttValues.hasOwnProperty(attribute) &&
        jsonAttValues[attribute].label
      ) {
        attributesList.push(jsonAttValues[attribute]);
      }
    });

    _.each(attributesList, function(attribute) {
      if (
        attribute &&
        attribute.value &&
        _.isString(attribute.value) &&
        attribute.value.length > 0
      ) {
        objToReturn.keyValue.push(
          (attribute.label ? attribute.label : attribute.name) +
            ': ' +
            attribute.value
        );
        attSetInstanceDescription += '_' + attribute.value;
      }
    });

    if (objToReturn.keyValue.length > 0) {
      objToReturn.description = attSetInstanceDescription.substring(1);
    }
    return objToReturn;
  };
})();
