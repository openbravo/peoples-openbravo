/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Promise, Uint8Array  */

(function () {

  var ESCPOSStar = function () {
      OB.ESCPOS.Standard.call(this);

      this.CHAR_SIZE_0 = new Uint8Array([0x1B, 0x69, 0x00, 0x00]);
      this.CHAR_SIZE_1 = new Uint8Array([0x1B, 0x69, 0x01, 0x00]);
      this.CHAR_SIZE_2 = new Uint8Array([0x1B, 0x69, 0x00, 0x01]);
      this.CHAR_SIZE_3 = new Uint8Array([0x1B, 0x69, 0x01, 0x01]);

      this.BOLD_SET = new Uint8Array([0x1B, 0x45]);
      this.BOLD_RESET = new Uint8Array([0x1B, 0x46]);

      this.DRAWER_OPEN = new Uint8Array([0x1C]);

      this.PARTIAL_CUT_1 = new Uint8Array([0x1B, 0x64, 0x30]);
      };

  OB.PRINTERTYPES.GENERICUSB.register({
    name: 'Star TSP 100',
    vendorId: 0x0519,
    productId: 0x0003,
    ESCPOS: ESCPOSStar
  });

}());