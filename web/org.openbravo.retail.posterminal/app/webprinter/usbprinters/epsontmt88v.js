/*
 ************************************************************************************
 * Copyright (C) 2019-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  function ESCPOSEPSON() {
    OB.ESCPOS.Standard.call(this);
    this.encoderText = new OB.ESCPOS.Latin1Encoder(this.encoderText);
    this.CODE_TABLE = new Uint8Array([0x1b, 0x74, 0x10]);
  }
  OB.PRINTERTYPES.GENERICUSB.register({
    name: 'EPSON TM T88V',
    vendorId: 0x04b8,
    productId: 0x0202,
    ESCPOS: ESCPOSEPSON
  });
})();
