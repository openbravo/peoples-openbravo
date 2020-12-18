/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  var WincorCODE128Encoder = function() {
    OB.ESCPOS.CODE128Encoder.call(this);
    this.encodechar = function(c) {
      switch (c) {
        case '/':
          return 0x0f;
        case '0':
          return 0x10;
        case '1':
          return 0x11;
        case '2':
          return 0x12;
        case '3':
          return 0x13;
        case '4':
          return 0x14;
        case '5':
          return 0x15;
        case '6':
          return 0x16;
        case '7':
          return 0x17;
        case '8':
          return 0x18;
        case '9':
          return 0x19;
        case 'A':
          return 0x21;
        case 'B':
          return 0x22;
        case 'C':
          return 0x23;
        case 'D':
          return 0x24;
        case 'E':
          return 0x25;
        case 'F':
          return 0x26;
        case 'G':
          return 0x27;
        case 'H':
          return 0x28;
        case 'I':
          return 0x29;
        case 'J':
          return 0x2a;
        case 'K':
          return 0x2b;
        case 'L':
          return 0x2c;
        case 'M':
          return 0x2d;
        case 'N':
          return 0x2e;
        case 'O':
          return 0x2f;
        case 'P':
          return 0x30;
        case 'Q':
          return 0x31;
        case 'R':
          return 0x32;
        case 'S':
          return 0x33;
        case 'T':
          return 0x34;
        case 'U':
          return 0x35;
        case 'V':
          return 0x36;
        case 'W':
          return 0x37;
        case 'X':
          return 0x38;
        case 'Y':
          return 0x39;
        case 'Z':
          return 0x3a;
        case '[':
          return 0x3b;
        case '\\':
          return 0x3c;
        case ']':
          return 0x3d;
        case '^':
          return 0x3e;
        case '_':
          return 0x3f;
        case "'":
          return 0x40;
        case 'a':
          return 0x41;
        case 'b':
          return 0x42;
        case 'c':
          return 0x43;
        case 'd':
          return 0x44;
        case 'e':
          return 0x45;
        case 'f':
          return 0x46;
        case 'g':
          return 0x47;
        case 'h':
          return 0x48;
        case 'i':
          return 0x49;
        case 'j':
          return 0x4a;
        case 'k':
          return 0x4b;
        case 'l':
          return 0x4c;
        case 'm':
          return 0x4d;
        case 'n':
          return 0x4e;
        case 'o':
          return 0x4f;
        case 'p':
          return 0x50;
        case 'q':
          return 0x51;
        case 'r':
          return 0x52;
        case 's':
          return 0x53;
        case 't':
          return 0x54;
        case 'u':
          return 0x55;
        case 'v':
          return 0x56;
        case 'w':
          return 0x57;
        case 'x':
          return 0x58;
        case 'y':
          return 0x59;
        case 'z':
          return 0x5a;
        case '{':
          return 0x5b;
        case '|':
          return 0x5c;
        case '}':
          return 0x5d;
        case '~':
          return 0x5e;
        default:
          return 0x00;
      }
    };
  };

  var ESCPOSWincor = function() {
    OB.ESCPOS.StandardImageAlt.call(this);

    this.PARTIAL_CUT_1 = new Uint8Array([0x1d, 0x56, 0x41, 0x30]);
    this.BAR_CODE128TYPE = new Uint8Array([0x68]);
    this.encoderCODE128 = new WincorCODE128Encoder();

    this.transCODE128 = function(code, position) {
      var line = new Uint8Array();
      line = OB.ARRAYS.append(line, this.transBarcodeHeader(code, position));

      line = OB.ARRAYS.append(line, this.BAR_CODE128);
      var barcode128 = this.encoderCODE128.encode(code);
      line = OB.ARRAYS.append(
        line,
        new Uint8Array([this.BAR_CODE128TYPE.length + barcode128.length])
      );
      line = OB.ARRAYS.append(line, this.BAR_CODE128TYPE);
      line = OB.ARRAYS.append(line, barcode128);

      line = OB.ARRAYS.append(line, this.transBarcodeFooter(code, position));
      return line;
    };
  };

  OB.PRINTERTYPES.GENERICUSB.register({
    name: 'Wincor Nixdorf TH230+',
    vendorId: 0x0aa7,
    productId: 0x0304,
    ESCPOS: ESCPOSWincor
  });
})();
