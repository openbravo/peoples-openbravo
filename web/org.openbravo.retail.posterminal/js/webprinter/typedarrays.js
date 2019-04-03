/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Promise */

(function () {

  window.OB = window.OB || {};

  OB.ARRAYS = {
    printArray: function (printChunk, size, data) {
      var i, result;

      result = Promise.resolve();
      for (i = 0; i < data.length; i += size) {
        result = result.then(printChunk(data.slice(i, i + size)));
      }
      return result;
    }
  };
}());