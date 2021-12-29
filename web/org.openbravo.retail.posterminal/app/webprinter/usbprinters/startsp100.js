/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  function ESCPOSStar() {
    OB.ESCPOS.StandardImageAlt.call(this);

    this.CHAR_SIZE_0 = new Uint8Array([0x1b, 0x69, 0x00, 0x00]);
    this.CHAR_SIZE_1 = new Uint8Array([0x1b, 0x69, 0x01, 0x00]);
    this.CHAR_SIZE_2 = new Uint8Array([0x1b, 0x69, 0x00, 0x01]);
    this.CHAR_SIZE_3 = new Uint8Array([0x1b, 0x69, 0x01, 0x01]);

    this.BOLD_SET = new Uint8Array([0x1b, 0x45]);
    this.BOLD_RESET = new Uint8Array([0x1b, 0x46]);

    this.CENTER_JUSTIFICATION = new Uint8Array([0x1b, 0x1d, 0x61, 0x01]);
    this.LEFT_JUSTIFICATION = new Uint8Array([0x1b, 0x1d, 0x61, 0x00]);

    this.DRAWER_OPEN = new Uint8Array([0x1c]);

    this.PARTIAL_CUT_1 = new Uint8Array([0x1b, 0x64, 0x30]);

    this.BAR_EAN13 = new Uint8Array([0x1b, 0x62, 0x03]);
    this.BAR_CODE128 = new Uint8Array([0x1b, 0x62, 0x06]);
    this.BAR_POSITIONNONE = new Uint8Array([0x01]);
    this.BAR_POSITIONDOWN = new Uint8Array([0x02]);
    this.BAR_DOTS = new Uint8Array([0x02]);
    this.BAR_HEIGHT = new Uint8Array([0x50]);
    this.BAR_END = new Uint8Array([0x1e]);

    this.transBarcodeParameters = (code, position) => {
      let line = new Uint8Array();
      if (position === 'none') {
        line = OB.ARRAYS.append(line, this.BAR_POSITIONNONE);
      } else {
        line = OB.ARRAYS.append(line, this.BAR_POSITIONDOWN);
      }
      line = OB.ARRAYS.append(line, this.BAR_DOTS);
      line = OB.ARRAYS.append(line, this.BAR_HEIGHT);

      return line;
    };

    this.transEAN13 = (code, position) => {
      let line = new Uint8Array();
      const barcode = code.substring(0, 12);

      line = OB.ARRAYS.append(line, this.CENTER_JUSTIFICATION);
      line = OB.ARRAYS.append(line, this.BAR_EAN13);
      line = OB.ARRAYS.append(
        line,
        this.transBarcodeParameters(code, position)
      );
      line = OB.ARRAYS.append(line, this.encoderEAN13.encode(barcode));
      line = OB.ARRAYS.append(line, this.BAR_END);
      line = OB.ARRAYS.append(line, this.LEFT_JUSTIFICATION);
      return line;
    };

    this.transCODE128 = (code, position) => {
      let line = new Uint8Array();

      line = OB.ARRAYS.append(line, this.CENTER_JUSTIFICATION);
      line = OB.ARRAYS.append(line, this.BAR_CODE128);
      line = OB.ARRAYS.append(
        line,
        this.transBarcodeParameters(code, position)
      );
      line = OB.ARRAYS.append(line, this.encoderCODE128.encode(code));
      line = OB.ARRAYS.append(line, this.BAR_END);
      line = OB.ARRAYS.append(line, this.LEFT_JUSTIFICATION);
      return line;
    };
  }

  OB.PRINTERTYPES.GENERICUSB.register({
    name: 'Star TSP 100',
    vendorId: 0x0519,
    productId: 0x0003,
    ESCPOS: ESCPOSStar
  });
})();
