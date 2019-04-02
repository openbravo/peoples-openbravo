/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
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
    UNDERLINE_RESET: new Uint8Array([0x1B, 0x2D, 0x00]),

    CENTER_JUSTIFICATION: new Uint8Array([0x1B, 0x61, 0x01]),
    LEFT_JUSTIFICATION: new Uint8Array([0x1B, 0x61, 0x00]),
    RIGHT_JUSTIFICATION: new Uint8Array([0x1B, 0x61, 0x02]),

    BAR_HEIGHT: new Uint8Array([0x1D, 0x68, 0x40]),
    BAR_WIDTH3: new Uint8Array([0x1D, 0x77, 0x03]),
    BAR_WIDTH2: new Uint8Array([0x1D, 0x77, 0x02]),
    BAR_WIDTH1: new Uint8Array([0x1D, 0x77, 0x01]),
    BAR_POSITIONDOWN: new Uint8Array([0x1D, 0x48, 0x02]),
    BAR_POSITIONNONE: new Uint8Array([0x1D, 0x48, 0x00]),
    BAR_HRIFONT1: new Uint8Array([0x1D, 0x66, 0x01]),
    BAR_CODE02: new Uint8Array([0x1D, 0x6B, 0x02]),
    BAR_CODE128: new Uint8Array([0x1D, 0x6B, 0x49]),
    BAR_CODE128TYPE: new Uint8Array([0x7B, 0x42]),

    IMAGE_HEADER: new Uint8Array([0x1D, 0x76, 0x30, 0x03]),

    PARTIAL_CUT_1: new Uint8Array([0x1B, 0x69]),

    transCode128: function (txt) {

      function transCode128Char(c) {
        switch (c) {
        case '/':
          return 0x2F;
        case '0':
          return 0x30;
        case '1':
          return 0x31;
        case '2':
          return 0x32;
        case '3':
          return 0x33;
        case '4':
          return 0x34;
        case '5':
          return 0x35;
        case '6':
          return 0x36;
        case '7':
          return 0x37;
        case '8':
          return 0x38;
        case '9':
          return 0x39;
        case 'A':
          return 0x41;
        case 'B':
          return 0x42;
        case 'C':
          return 0x43;
        case 'D':
          return 0x44;
        case 'E':
          return 0x45;
        case 'F':
          return 0x46;
        case 'G':
          return 0x47;
        case 'H':
          return 0x48;
        case 'I':
          return 0x49;
        case 'J':
          return 0x4A;
        case 'K':
          return 0x4B;
        case 'L':
          return 0x4C;
        case 'M':
          return 0x4D;
        case 'N':
          return 0x4E;
        case 'O':
          return 0x4F;
        case 'P':
          return 0x50;
        case 'Q':
          return 0x51;
        case 'R':
          return 0x52;
        case 'S':
          return 0x53;
        case 'T':
          return 0x54;
        case 'U':
          return 0x55;
        case 'V':
          return 0x56;
        case 'W':
          return 0x57;
        case 'X':
          return 0x58;
        case 'Y':
          return 0x59;
        case 'Z':
          return 0x5A;
        case '[':
          return 0x5B;
        case '\\':
          return 0x5C;
        case ']':
          return 0x5D;
        case '^':
          return 0x5E;
        case '_':
          return 0x5F;
        case '\'':
          return 0x60;
        case 'a':
          return 0x61;
        case 'b':
          return 0x62;
        case 'c':
          return 0x63;
        case 'd':
          return 0x64;
        case 'e':
          return 0x65;
        case 'f':
          return 0x66;
        case 'g':
          return 0x67;
        case 'h':
          return 0x68;
        case 'i':
          return 0x69;
        case 'j':
          return 0x6A;
        case 'k':
          return 0x6B;
        case 'l':
          return 0x6C;
        case 'm':
          return 0x6D;
        case 'n':
          return 0x6E;
        case 'o':
          return 0x6F;
        case 'p':
          return 0x70;
        case 'q':
          return 0x71;
        case 'r':
          return 0x72;
        case 's':
          return 0x73;
        case 't':
          return 0x74;
        case 'u':
          return 0x75;
        case 'v':
          return 0x76;
        case 'w':
          return 0x77;
        case 'x':
          return 0x78;
        case 'y':
          return 0x79;
        case 'z':
          return 0x7A;
        case '{':
          return 0x7B;
        case '|':
          return 0x7C;
        case '}':
          return 0x7D;
        case '~':
          return 0x7E;
        case ':':
          return 0x3A;
        case ';':
          return 0x3B;
        case '?':
          return 0x3F;
        case '.':
          return 0x2E;
        case '=':
          return 0x3D;
        case '-':
          return 0x2D;
        case '<':
          return 0x3C;
        case '>':
          return 0x3E;
        case '*':
          return 0x2A;
        case '+':
          return 0x2B;
        case '!':
          return 0x21;
        case '%':
          return 0x25;
        case '&':
          return 0x26;
        case '#':
          return 0x23;
        default:
          return 0x30;
        }
      }

      if (txt) {
        var result = [];
        var i;
        for (i = 0; i < txt.length; i++) {
          result.push(transCode128Char(txt.charAt(i)));
        }
        return new Uint8Array(result);
      } else {
        return new Uint8Array();
      }
    },

    transImage: function (imagedata) {

      function isBlack(x, y) {
        if (x < 0 || x >= imagedata.width || y < 0 || y >= imagedata.height) {
          return false;
        }
        var index = y * imagedata.width * 4 + x * 4;
        var luminosity = 0;
        luminosity += 0.30 * imagedata.data[index]; // RED luminosity
        luminosity += 0.59 * imagedata.data[index + 1]; // GREEN luminosity
        luminosity += 0.11 * imagedata.data[index + 2]; // BLUE luminosity
        luminosity = 1 - luminosity / 255;
        luminosity *= imagedata.data[index + 3] / 255; // ALPHA factor
        return luminosity >= 0.5;
      }

      var result = [];
      var i, j, p, d;

      var width = (imagedata.width + 7) / 8;
      var height = imagedata.height;

      result.push(width & 255);
      result.push(width >> 8);
      result.push(height & 255);
      result.push(height >> 8);

      // Raw data
      for (i = 0; i < imagedata.height; i++) {
        for (j = 0; j < imagedata.width; j = j + 8) {
          p = 0x00;
          for (d = 0; d < 8; d++) {
            p = p << 1;
            if (isBlack(j + d, i)) {
              p = p | 0x01;
            }
          }
          result.push(p);
        }
      }

      return new Uint8Array(result);
    }
  };
}());