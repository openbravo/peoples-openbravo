/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  OB.PRINTERTYPES.GENERICUSB.register({
    name: 'EPSON TM T20II',
    vendorId: 0x04b8,
    productId: 0x0e15,
    ESCPOS: OB.ESCPOS.Standard
  });
})();
