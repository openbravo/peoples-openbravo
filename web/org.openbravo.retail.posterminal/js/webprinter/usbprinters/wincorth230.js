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

  var ESCPOSWincor = function () {
      OB.ESCPOS.Standard.call(this);

      this.PARTIAL_CUT_1 = new Uint8Array([0x1D, 0x56, 0x41, 0x30]);
      };

  OB.PRINTERTYPES.GENERICUSB.register({
    name: 'Wincor Nixdorf TH230+',
    vendorId: 0x0AA7,
    productId: 0x0304,
    ESCPOS: ESCPOSWincor
  });

}());