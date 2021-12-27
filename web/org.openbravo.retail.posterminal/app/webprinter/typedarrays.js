/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  OB.ARRAYS = {
    printArray: (printChunk, size, data) => {
      let result;

      result = Promise.resolve();
      for (let i = 0; i < data.length; i += size) {
        result = result.then(printChunk(data.slice(i, i + size)));
      }
      return result;
    },
    append: (a1, a2) => {
      const tmp = new Uint8Array(a1.length + a2.length);
      tmp.set(a1, 0);
      tmp.set(a2, a1.byteLength);
      return tmp;
    }
  };
})();
