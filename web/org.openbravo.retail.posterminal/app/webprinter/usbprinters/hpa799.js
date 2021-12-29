/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/* eslint-disable no-bitwise */

(() => {
  function ESCPOSHP() {
    OB.ESCPOS.Standard.call(this);
    this.transImage = imagedata => {
      let line = new Uint8Array();
      const result = [];
      let p;

      const { width, height } = imagedata;

      // Raw data
      for (let i = 0; i < height; i += 8) {
        result.push(0x1b, 0x4b, width & 255, width >> 8);
        for (let j = 0; j < imagedata.width; j += 1) {
          p = 0x00;
          for (let d = 0; d < 8; d += 1) {
            p <<= 1;
            if (this.isBlack(imagedata, j, i + d)) {
              p |= 0x01;
            }
          }
          result.push(p);
        }
        result.push(0x0d, 0x0a);
      }

      line = OB.ARRAYS.append(line, this.CENTER_JUSTIFICATION);
      line = OB.ARRAYS.append(line, new Uint8Array(result));
      line = OB.ARRAYS.append(line, this.LEFT_JUSTIFICATION);
      return line;
    };
  }

  OB.PRINTERTYPES.GENERICUSB.register({
    name: 'HP A799',
    vendorId: 0x05d9,
    productId: 0xa795,
    ESCPOS: ESCPOSHP
  });
})();
