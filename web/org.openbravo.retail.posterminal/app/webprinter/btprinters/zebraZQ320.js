/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  OB.PRINTERTYPES.GENERICBT.register({
    name: 'Zebra ZQ 320 Printer',
    genericName: 'XXZFJ172400372',
    services: ['38eb4a80-c570-11e3-9507-0002a5d5c51b'],
    characteristic: '38eb4a82-c570-11e3-9507-0002a5d5c51b',
    ESCPOS: OB.ESCPOS.Standard
  });
})();
