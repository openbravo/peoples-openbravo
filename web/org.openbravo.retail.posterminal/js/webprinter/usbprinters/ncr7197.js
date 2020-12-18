/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  var ESCPOSNCR = function() {
    OB.ESCPOS.Standard.call(this);

    this.transImage = function(imagedata) {
      var line = new Uint8Array();
      var result = [];
      var i, j, p, d;

      var width = (imagedata.width + 7) / 8;
      var height = imagedata.height;

      // Raw data
      for (i = 0; i < height; i++) {
        result.push(0x1b, 0x4b, width & 255, width >> 8);
        for (j = 0; j < imagedata.width; j = j + 8) {
          p = 0x00;
          for (d = 0; d < 8; d++) {
            p = p << 1;
            if (this.isBlack(imagedata, j + d, i)) {
              p = p | 0x01;
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
