/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  window.OB = window.OB || {};

  OB.ARRAYS = {
    printArray: function(printChunk, size, data) {
      var i, result;

      result = Promise.resolve();
      for (i = 0; i < data.length; i += size) {
        result = result.then(printChunk(data.slice(i, i + size)));
      }
      return result;
    },
    append: function(a1, a2) {
      var tmp = new Uint8Array(a1.length + a2.length);
      tmp.set(a1, 0);
      tmp.set(a2, a1.byteLength);
      return tmp;
    }
  };
})();
