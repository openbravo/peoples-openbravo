/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/* eslint-disable no-bitwise */

(() => {
  const ESCPOSNCR = () => {
    OB.ESCPOS.Standard.call(this);

    this.transImage = imagedata => {
      let line = new Uint8Array();
      const result = [];
      let p;

      const width = (imagedata.width + 7) / 8;
      const { height } = imagedata;

      // Raw data
      for (let i = 0; i < height; i += 1) {
        result.push(0x1b, 0x4b, width & 255, width >> 8);
        for (let j = 0; j < imagedata.width; j += 8) {
          p = 0x00;
          for (let d = 0; d < 8; d += 1) {
            p <<= 1;
            if (this.isBlack(imagedata, j + d, i)) {
              p |= 0x01;
            }
          }
          result.push(p);
        }
        result.push(0x0d, 0x0a);
      }

      line = OB.ARRAYS.append(line, this.CENTER_JUSTIFICATION);
      line = OB.ARRAYS.append(line, this.IMAGE_HEADER);
      line = OB.ARRAYS.append(line, new Uint8Array(result));
      line = OB.ARRAYS.append(line, this.LEFT_JUSTIFICATION);
      return line;
    };
  };

  OB.PRINTERTYPES.GENERICUSB.register({
    name: 'NCR 7197',
    vendorId: 0x0404,
    productId: 0x0312,
    ESCPOS: ESCPOSNCR
  });
})();
