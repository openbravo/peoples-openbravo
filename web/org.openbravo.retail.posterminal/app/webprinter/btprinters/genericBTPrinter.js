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
    name: 'Generic Bluetooth Receipt Printer',
    genericName: 'BlueTooth Printer',
    services: ['e7810a71-73ae-499d-8c15-faa9aef0c3f2'],
    characteristic: 'bef8d6c9-9c21-4c9e-b632-bd58c1009f9f',
    ESCPOS: OB.ESCPOS.Standard
  });
})();
