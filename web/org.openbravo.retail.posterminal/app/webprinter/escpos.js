/*
 ************************************************************************************
 * Copyright (C) 2018-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/* eslint-disable no-bitwise */

(() => {
  function CODE128Encoder() {
    this.encodechar = c => {
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
    this.encode = txt => {
      if (txt) {
        const result = [];
        for (let i = 0; i < txt.length; i += 1) {
          result.push(this.encodechar(txt.charAt(i)));
        }
        return new Uint8Array(result);
      }
      return new Uint8Array();
    };
  }
  class Latin1Encoder {
    constructor(encoder) {
      this.originalEncoderText = encoder;
    }

    encodechar(c) {
      switch (c) {
        // 8
        case '\u20AC': // €
          return 0x80;
        case '\u201A': // ‚
          return 0x82;
        case '\u0192': // ƒ
          return 0x83;
        case '\u201E': // „
          return 0x84;
        case '\u2026': // …
          return 0x85;
        case '\u2020': // †
          return 0x86;
        case '\u2021': // ‡
          return 0x87;
        case '\u02C6': // ˆ
          return 0x88;
        case '\u2030': // ‰
          return 0x89;
        case '\u0160': // Š
          return 0x8a;
        case '\u2039': // ‹
          return 0x8b;
        case '\u0152': // Œ
          return 0x8c;
        case '\u017D': // Ž
          return 0x8e;
        // 9
        case '\u2018': // ‘
          return 0x91;
        case '\u2019': // ’
          return 0x92;
        case '\u201C': // “
          return 0x93;
        case '\u201D': // ”
          return 0x94;
        case '\u2022': // •
          return 0x95;
        case '\u2013': // –
          return 0x96;
        case '\u2014': // —
          return 0x97;
        case '\u02DC': // ˜
          return 0x98;
        case '\u2122': // ™
          return 0x99;
        case '\u0161': // š
          return 0x9a;
        case '\u203A': // ›
          return 0x9b;
        case '\u0153': // œ
          return 0x9c;
        case '\u017E': // ž
          return 0x9e;
        case '\u0178': // Ÿ
          return 0x9f;
        // a
        case '\u00A1': // ¡
          return 0xa1;
        case '\u00A2': // ¢
          return 0xa2;
        case '\u00A3': // £
          return 0xa3;
        case '\u00A4': // ¤
          return 0xa4;
        case '\u00A5': // ¥
          return 0xa5;
        case '\u00A6': // ¦
          return 0xa6;
        case '\u00A7': // §
          return 0xa7;
        case '\u00A8': // ¨
          return 0xa8;
        case '\u00A9': // ©
          return 0xa9;
        case '\u00AA': // ª
          return 0xaa;
        case '\u00AB': // «
          return 0xab;
        case '\u00AC': // ¬
          return 0xac;
        case '\u00AE': // ®
          return 0xae;
        case '\u00AF': // ¯
          return 0xaf;
        // b
        case '\u00B0': // °
          return 0xb0;
        case '\u00B1': // ±
          return 0xb1;
        case '\u00B2': // ²
          return 0xb2;
        case '\u00B3': // ³
          return 0xb3;
        case '\u00B4': // ´
          return 0xb4;
        case '\u00B5': // µ
          return 0xb5;
        case '\u00B6': // ¶
          return 0xb6;
        case '\u00B7': // ·
          return 0xb7;
        case '\u00B8': // ¸
          return 0xb8;
        case '\u00B9': // ¹
          return 0xb9;
        case '\u00BA': // º
          return 0xba;
        case '\u00BB': // »
          return 0xbb;
        case '\u00BC': // ¼
          return 0xbc;
        case '\u00BD': // ½
          return 0xbd;
        case '\u00BE': // ¾
          return 0xbe;
        case '\u00BF': // ¿
          return 0xbf;
        // c
        case '\u00C0': // À
          return 0xc0;
        case '\u00C1': // Á
          return 0xc1;
        case '\u00C2': // Â
          return 0xc2;
        case '\u00C3': // Ã
          return 0xc3;
        case '\u00C4': // Ä
          return 0xc4;
        case '\u00C5': // Å
          return 0xc5;
        case '\u00C6': // Æ
          return 0xc6;
        case '\u00C7': // Ç
          return 0xc7;
        case '\u00C8': // È
          return 0xc8;
        case '\u00C9': // É
          return 0xc9;
        case '\u00CA': // Ê
          return 0xca;
        case '\u00CB': // Ë
          return 0xcb;
        case '\u00CC': // Ì
          return 0xcc;
        case '\u00CD': // Í
          return 0xcd;
        case '\u00CE': // Î
          return 0xce;
        case '\u00CF': // Ï
          return 0xcf;
        // d
        case '\u00D0': // Ð
          return 0xd0;
        case '\u00D1': // Ñ
          return 0xd1;
        case '\u00D2': // Ò
          return 0xd2;
        case '\u00D3': // Ó
          return 0xd3;
        case '\u00D4': // Ô
          return 0xd4;
        case '\u00D5': // Õ
          return 0xd5;
        case '\u00D6': // Ö
          return 0xd6;
        case '\u00D7': // ×
          return 0xd7;
        case '\u00D8': // Ø
          return 0xd8;
        case '\u00D9': // Ù
          return 0xd9;
        case '\u00DA': // Ú
          return 0xda;
        case '\u00DB': // Û
          return 0xdb;
        case '\u00DC': // Ü
          return 0xdc;
        case '\u00DD': // Ý
          return 0xdd;
        case '\u00DE': // Þ
          return 0xde;
        case '\u00DF': // ß
          return 0xdf;
        // e
        case '\u00E0': // à
          return 0xe0;
        case '\u00E1': // á
          return 0xe1;
        case '\u00E2': // â
          return 0xe2;
        case '\u00E3': // ã
          return 0xe3;
        case '\u00E4': // ä
          return 0xe4;
        case '\u00E5': // å
          return 0xe5;
        case '\u00E6': // æ
          return 0xe6;
        case '\u00E7': // ç
          return 0xe7;
        case '\u00E8': // è
          return 0xe8;
        case '\u00E9': // é
          return 0xe9;
        case '\u00EA': // ê
          return 0xea;
        case '\u00EB': // ë
          return 0xeb;
        case '\u00EC': // ì
          return 0xec;
        case '\u00ED': // í
          return 0xed;
        case '\u00EE': // î
          return 0xee;
        case '\u00EF': // ï
          return 0xef;
        // f
        case '\u00F0': // ð
          return 0xf0;
        case '\u00F1': // ñ
          return 0xf1;
        case '\u00F2': // ò
          return 0xf2;
        case '\u00F3': // ó
          return 0xf3;
        case '\u00F4': // ô
          return 0xf4;
        case '\u00F5': // õ
          return 0xf5;
        case '\u00F6': // õ
          return 0xf6;
        case '\u00F7': // ÷
          return 0xf7;
        case '\u00F8': // ø
          return 0xf8;
        case '\u00F9': // ù
          return 0xf9;
        case '\u00FA': // ú
          return 0xfa;
        case '\u00FB': // û
          return 0xfb;
        case '\u00FC': // ü
          return 0xfc;
        case '\u00FD': // ý
          return 0xfd;
        case '\u00FE': // þ
          return 0xfe;
        case '\u00FF': // ÿ
          return 0xff;
        default:
          return this.originalEncoderText.encode(c);
      }
    }

    encode(txt) {
      if (txt) {
        const result = [];
        for (let i = 0; i < txt.length; i += 1) {
          result.push(this.encodechar(txt.charAt(i)));
        }
        return new Uint8Array(result);
      }
      return new Uint8Array();
    }
  }

  function Base() {
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

    this.transEAN13 = () => new Uint8Array();

    this.transCODE128 = () => new Uint8Array();

    this.transQR = () => new Uint8Array();

    this.transImage = () => new Uint8Array();
  }

  function Standard() {
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
    this.CODE_TABLE = new Uint8Array();
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

    this.isBlack = (imagedata, x, y) => {
      if (x < 0 || x >= imagedata.width || y < 0 || y >= imagedata.height) {
        return false;
      }
      const index = y * imagedata.width * 4 + x * 4;
      let luminosity = 0;
      luminosity += 0.3 * imagedata.data[index]; // RED luminosity
      luminosity += 0.59 * imagedata.data[index + 1]; // GREEN luminosity
      luminosity += 0.11 * imagedata.data[index + 2]; // BLUE luminosity
      luminosity = 1 - luminosity / 255;
      luminosity *= imagedata.data[index + 3] / 255; // ALPHA factor
      return luminosity >= 0.5;
    };

    this.transImage = imagedata => {
      let line = new Uint8Array();
      const result = [];
      let p;

      const width = (imagedata.width + 7) / 8;
      const { height } = imagedata;

      result.push(width & 255);
      result.push(width >> 8);
      result.push(height & 255);
      result.push(height >> 8);

      // Raw data
      for (let i = 0; i < imagedata.height; i += 1) {
        for (let j = 0; j < imagedata.width; j += 8) {
          p = 0x00;
          for (let d = 0; d < 8; d += 1) {
            p <<= 1;
            if (this.isBlack(imagedata, j + d, i)) {
              p |= 0x01;
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

    this.transBarcodeHeader = (code, position) => {
      let line = new Uint8Array();
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

    this.transBarcodeFooter = () => {
      let line = new Uint8Array();
      line = OB.ARRAYS.append(line, this.NEW_LINE);
      line = OB.ARRAYS.append(line, this.LEFT_JUSTIFICATION);
      return line;
    };

    this.transEAN13 = (code, position) => {
      let line = new Uint8Array();
      const barcode = code.substring(0, 12);

      line = OB.ARRAYS.append(line, this.transBarcodeHeader(code, position));

      line = OB.ARRAYS.append(line, this.BAR_CODE02);
      line = OB.ARRAYS.append(line, this.encoderEAN13.encode(barcode));
      line = OB.ARRAYS.append(line, new Uint8Array([0x00]));

      line = OB.ARRAYS.append(line, this.transBarcodeFooter(code, position));
      return line;
    };

    this.transCODE128 = (code, position) => {
      let line = new Uint8Array();
      line = OB.ARRAYS.append(line, this.transBarcodeHeader(code, position));

      line = OB.ARRAYS.append(line, this.BAR_CODE128);
      const barcode128 = this.encoderCODE128.encode(code);
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

    this.transQR = (code, quality, size) => {
      let line = new Uint8Array();
      const qrcode = this.encoderQR.encode(code);
      const codeLENGTH = new Uint8Array([
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
  }

  function StandardImageAlt() {
    Standard.call(this);
    this.transImage = imagedata => {
      let line = new Uint8Array();
      const result = [];
      let p;

      const width = (imagedata.width + 7) / 8;
      const { height } = imagedata;

      result.push(width & 255);
      result.push(width >> 8);
      result.push(height & 255);
      result.push(height >> 8);

      // Raw data
      for (let i = 0; i < imagedata.height; i += 1) {
        for (let j = 0; j < imagedata.width; j += 8) {
          p = 0x00;
          for (let d = 0; d < 8; d += 1) {
            p <<= 1;
            if (this.isBlack(imagedata, j + d, i)) {
              p |= 0x01;
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
  }

  OB.ESCPOS = {
    // Basic ESCPOS codes with limited functionality. No images, No cut paper, No drawer.
    Base,
    // Full featured. Standard EPSON ESCPOS codes.
    Standard,
    // Full featured. Standard EPSON ESCPOS codes with alternative image method.
    StandardImageAlt,
    // Singleton for Standard EPSON ESCPOS codes
    standardinst: new Standard(),
    CODE128Encoder,
    Latin1Encoder
  };
})();
