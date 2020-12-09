/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  var CODE128Encoder = function() {
    this.encodechar = function(c) {
      switch (c) {
        case '/':
          return 0x2f;
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
          return 0x4a;
        case 'K':
          return 0x4b;
        case 'L':
          return 0x4c;
        case 'M':
          return 0x4d;
        case 'N':
          return 0x4e;
        case 'O':
          return 0x4f;
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
          return 0x5a;
        case '[':
          return 0x5b;
        case '\\':
          return 0x5c;
        case ']':
          return 0x5d;
        case '^':
          return 0x5e;
        case '_':
          return 0x5f;
        case "'":
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
          return 0x6a;
        case 'k':
          return 0x6b;
        case 'l':
          return 0x6c;
        case 'm':
          return 0x6d;
        case 'n':
          return 0x6e;
        case 'o':
          return 0x6f;
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
          return 0x7a;
        case '{':
          return 0x7b;
        case '|':
          return 0x7c;
        case '}':
          return 0x7d;
        case '~':
          return 0x7e;
        case ':':
          return 0x3a;
        case ';':
          return 0x3b;
        case '?':
          return 0x3f;
        case '.':
          return 0x2e;
        case '=':
          return 0x3d;
        case '-':
          return 0x2d;
        case '<':
          return 0x3c;
        case '>':
          return 0x3e;
        case '*':
          return 0x2a;
        case '+':
          return 0x2b;
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
    };
    this.encode = function(txt) {
      if (txt) {
        var result = [];
        var i;
        for (i = 0; i < txt.length; i++) {
          result.push(this.encodechar(txt.charAt(i)));
        }
        return new Uint8Array(result);
      } else {
        return new Uint8Array();
      }
    };
  };

  var Base = function() {
    this.encoderText = new TextEncoder('utf-8');
    this.NEW_LINE = new Uint8Array([0x0d, 0x0a]);
    this.PARTIAL_CUT_1 = new Uint8Array();

    this.CHAR_SIZE_0 = new Uint8Array([0x1d, 0x21, 0x00]);
    this.CHAR_SIZE_1 = new Uint8Array([0x1d, 0x21, 0x01]);
    this.CHAR_SIZE_2 = new Uint8Array([0x1d, 0x21, 0x30]);
    this.CHAR_SIZE_3 = new Uint8Array([0x1d, 0x21, 0x31]);

    this.BOLD_SET = new Uint8Array([0x1b, 0x45, 0x01]);
    this.BOLD_RESET = new Uint8Array([0x1b, 0x45, 0x00]);
    this.UNDERLINE_SET = new Uint8Array([0x1b, 0x2d, 0x01]);
    this.UNDERLINE_RESET = new Uint8Array([0x1b, 0x2d, 0x00]);

    this.CENTER_JUSTIFICATION = new Uint8Array([0x1b, 0x61, 0x01]);
    this.LEFT_JUSTIFICATION = new Uint8Array([0x1b, 0x61, 0x00]);
    this.RIGHT_JUSTIFICATION = new Uint8Array([0x1b, 0x61, 0x02]);

    this.DRAWER_OPEN = new Uint8Array();

    this.transEAN13 = function(code, position) {
      return new Uint8Array();
    };

    this.transCODE128 = function(code, position) {
      return new Uint8Array();
    };

    this.transQR = function(code, quality, size) {
      return new Uint8Array();
    };

    this.transImage = function(imagedata) {
      return new Uint8Array();
    };
  };

  var Standard = function() {
    Base.call(this);

    this.DRAWER_OPEN = new Uint8Array([0x1b, 0x70, 0x00, 0x32, -0x06]);

    this.PARTIAL_CUT_1 = new Uint8Array([0x1b, 0x69]);

    this.IMAGE_HEADER = new Uint8Array([0x1d, 0x76, 0x30, 0x03]);

    this.BAR_HEIGHT = new Uint8Array([0x1d, 0x68, 0x40]);
    this.BAR_WIDTH3 = new Uint8Array([0x1d, 0x77, 0x03]);
    this.BAR_WIDTH2 = new Uint8Array([0x1d, 0x77, 0x02]);
    this.BAR_WIDTH1 = new Uint8Array([0x1d, 0x77, 0x01]);
    this.BAR_POSITIONDOWN = new Uint8Array([0x1d, 0x48, 0x02]);
    this.BAR_POSITIONNONE = new Uint8Array([0x1d, 0x48, 0x00]);
    this.BAR_HRIFONT1 = new Uint8Array([0x1d, 0x66, 0x01]);
    this.BAR_CODE02 = new Uint8Array([0x1d, 0x6b, 0x02]);
    this.BAR_CODE128 = new Uint8Array([0x1d, 0x6b, 0x49]);
    this.BAR_CODE128TYPE = new Uint8Array([0x7b, 0x42]);
    this.encoderEAN13 = new TextEncoder('utf-8');
    this.encoderCODE128 = new CODE128Encoder();

    this.QR_INIT = new Uint8Array([0x1d, 0x28, 0x6b]);
    this.QR_CODE = new Uint8Array([0x31, 0x50, 0x30]);
    this.QR_QUALITY = new Uint8Array([
      0x1d,
      0x28,
      0x6b,
      0x03,
      0x00,
      0x31,
      0x45
    ]);
    this.QR_QUALITY_MAP = {
      L: new Uint8Array([0x30]),
      M: new Uint8Array([0x31]),
      H: new Uint8Array([0x32]),
      Q: new Uint8Array([0x33])
    };
    this.QR_SIZE = new Uint8Array([0x1d, 0x28, 0x6b, 0x03, 0x00, 0x31, 0x43]);
    this.QR_SIZE_MAP = {
      S: new Uint8Array([0x04]),
      M: new Uint8Array([0x08]),
      L: new Uint8Array([0x0c]),
      XL: new Uint8Array([0x10])
    };
    this.QR_PRINT = new Uint8Array([
      0x1d,
      0x28,
      0x6b,
      0x03,
      0x00,
      0x31,
      0x51,
      0x30
    ]);
    this.encoderQR = new TextEncoder('utf-8');

    this.isBlack = function(imagedata, x, y) {
      if (x < 0 || x >= imagedata.width || y < 0 || y >= imagedata.height) {
        return false;
      }
      var index = y * imagedata.width * 4 + x * 4;
      var luminosity = 0;
      luminosity += 0.3 * imagedata.data[index]; // RED luminosity
      luminosity += 0.59 * imagedata.data[index + 1]; // GREEN luminosity
      luminosity += 0.11 * imagedata.data[index + 2]; // BLUE luminosity
      luminosity = 1 - luminosity / 255;
      luminosity *= imagedata.data[index + 3] / 255; // ALPHA factor
      return luminosity >= 0.5;
    };

    this.transImage = function(imagedata) {
      var line = new Uint8Array();
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
            if (this.isBlack(imagedata, j + d, i)) {
              p = p | 0x01;
            }
          }
          result.push(p);
        }
      }

      line = OB.ARRAYS.append(line, this.CENTER_JUSTIFICATION);
      line = OB.ARRAYS.append(line, this.IMAGE_HEADER);
      line = OB.ARRAYS.append(line, new Uint8Array(result));
      line = OB.ARRAYS.append(line, this.LEFT_JUSTIFICATION);
      return line;
    };

    this.transBarcodeHeader = function(code, position) {
      var line = new Uint8Array();
      line = OB.ARRAYS.append(line, this.CENTER_JUSTIFICATION);
      line = OB.ARRAYS.append(line, this.NEW_LINE);

      line = OB.ARRAYS.append(line, this.BAR_HEIGHT);
      if (position === 'none') {
        line = OB.ARRAYS.append(line, this.BAR_POSITIONNONE);
      } else {
        line = OB.ARRAYS.append(line, this.BAR_POSITIONDOWN);
      }

      if (code.length > 14) {
        line = OB.ARRAYS.append(line, this.BAR_WIDTH1);
      } else if (code.length > 8) {
        line = OB.ARRAYS.append(line, this.BAR_WIDTH2);
      } else {
        line = OB.ARRAYS.append(line, this.BAR_WIDTH3);
      }

      line = OB.ARRAYS.append(line, this.BAR_HRIFONT1);

      return line;
    };

    this.transBarcodeFooter = function(code, position) {
      var line = new Uint8Array();
      line = OB.ARRAYS.append(line, this.NEW_LINE);
      line = OB.ARRAYS.append(line, this.LEFT_JUSTIFICATION);
      return line;
    };

    this.transEAN13 = function(code, position) {
      var line = new Uint8Array();
      var barcode = code.substring(0, 12);

      line = OB.ARRAYS.append(line, this.transBarcodeHeader(code, position));

      line = OB.ARRAYS.append(line, this.BAR_CODE02);
      line = OB.ARRAYS.append(line, this.encoderEAN13.encode(barcode));
      line = OB.ARRAYS.append(line, new Uint8Array([0x00]));

      line = OB.ARRAYS.append(line, this.transBarcodeFooter(code, position));
      return line;
    };

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
      line = OB.ARRAYS.append(line, new Uint8Array([0x00]));

      line = OB.ARRAYS.append(line, this.transBarcodeFooter(code, position));
      return line;
    };

    this.transQR = function(code, quality, size) {
      var line = new Uint8Array();
      var qrcode = this.encoderQR.encode(code);
      var codeLENGTH = new Uint8Array([
        (qrcode.length + 3) & 255,
        (qrcode.length + 3) >> 8
      ]);

      line = OB.ARRAYS.append(line, this.CENTER_JUSTIFICATION);
      line = OB.ARRAYS.append(line, this.QR_INIT);
      line = OB.ARRAYS.append(line, codeLENGTH);
      line = OB.ARRAYS.append(line, this.QR_CODE);
      line = OB.ARRAYS.append(line, qrcode);

      line = OB.ARRAYS.append(line, this.QR_QUALITY);
      line = OB.ARRAYS.append(line, this.QR_QUALITY_MAP[quality]);

      line = OB.ARRAYS.append(line, this.QR_SIZE);
      line = OB.ARRAYS.append(line, this.QR_SIZE_MAP[size]);

      line = OB.ARRAYS.append(line, this.QR_PRINT);
      line = OB.ARRAYS.append(line, this.LEFT_JUSTIFICATION);
      return new Uint8Array(line);
    };
  };

  var StandardImageAlt = function() {
    Standard.call(this);
    this.transImage = function(imagedata) {
      var line = new Uint8Array();
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
            if (this.isBlack(imagedata, j + d, i)) {
              p = p | 0x01;
            }
          }
          result.push(p);
        }
      }

      line = OB.ARRAYS.append(line, this.CENTER_JUSTIFICATION);
      line = OB.ARRAYS.append(line, this.IMAGE_HEADER);
      line = OB.ARRAYS.append(line, new Uint8Array(result));
      line = OB.ARRAYS.append(line, this.LEFT_JUSTIFICATION);
      return line;
    };
  };

  window.OB = window.OB || {};
  OB.ESCPOS = {
    // Basic ESCPOS codes with limited functionality. No images, No cut paper, No drawer.
    Base: Base,
    // Full featured. Standard EPSON ESCPOS codes.
    Standard: Standard,
    // Full featured. Standard EPSON ESCPOS codes with alternative image method.
    StandardImageAlt: StandardImageAlt,
    // Singleton for Standard EPSON ESCPOS codes
    standardinst: new Standard(),
    CODE128Encoder: CODE128Encoder
  };
})();
