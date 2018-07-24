/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Promise, Uint8Array  */

(function () {

  window.OB = window.OB || {};

  OB.ESCPOS = {
    NEW_LINE: new Uint8Array([0x0D, 0x0A]),

    CHAR_SIZE_0: new Uint8Array([0x1D, 0x21, 0x00]),
    CHAR_SIZE_1: new Uint8Array([0x1D, 0x21, 0x01]),
    CHAR_SIZE_2: new Uint8Array([0x1D, 0x21, 0x30]),
    CHAR_SIZE_3: new Uint8Array([0x1D, 0x21, 0x31]),

    BOLD_SET: new Uint8Array([0x1B, 0x45, 0x01]),
    BOLD_RESET: new Uint8Array([0x1B, 0x45, 0x00]),
    UNDERLINE_SET: new Uint8Array([0x1B, 0x2D, 0x01]),
    UNDERLINE_RESET: new Uint8Array([0x1B, 0x2D, 0x00])
  };
}());