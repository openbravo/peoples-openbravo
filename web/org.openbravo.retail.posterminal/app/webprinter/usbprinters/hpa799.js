/*
 ************************************************************************************
 * Copyright (C) 2021-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/* eslint-disable no-bitwise */

(() => {
  function ESCPOSHP() {
    OB.ESCPOS.Standard.call(this);
    this.transImage = imagedata => {
      let line = new Uint8Array();
      const result = [];
      let p;

      const { width, height } = imagedata;

      // Raw data
      for (let i = 0; i < height; i += 8) {
        result.push(0x1b, 0x4b, width & 255, width >> 8);
        for (let j = 0; j < imagedata.width; j += 1) {
          p = 0x00;
          for (let d = 0; d < 8; d += 1) {
            p <<= 1;
            if (this.isBlack(imagedata, j, i + d)) {
              p |= 0x01;
            }
          }
          result.push(p);
        }
        result.push(0x0d, 0x0a);
      }

      line = OB.ARRAYS.append(line, this.CENTER_JUSTIFICATION);
      line = OB.ARRAYS.append(line, new Uint8Array(result));
      line = OB.ARRAYS.append(line, this.LEFT_JUSTIFICATION);
      return line;
    };
    class EncoderHP {
      constructor(encoder) {
        this.originalEncoderText = encoder;
      }

      encodechar(c) {
        switch (c) {
          // 8
          case '€':
            return 0x80;
          case '‚':
            return 0x82;
          case 'ƒ':
            return 0x83;
          case '„':
            return 0x84;
          case '…':
            return 0x85;
          case '†':
            return 0x86;
          case '‡':
            return 0x87;
          case 'ˆ':
            return 0x88;
          case '‰':
            return 0x89;
          case 'Š':
            return 0x8a;
          case '‹':
            return 0x8b;
          case 'Œ':
            return 0x8c;
          case 'Ž':
            return 0x8e;
          // 9
          case '‘':
            return 0x91;
          case '’':
            return 0x92;
          case '“':
            return 0x93;
          case '”':
            return 0x94;
          case '•':
            return 0x95;
          case '–':
            return 0x96;
          case '—':
            return 0x97;
          case '˜':
            return 0x98;
          case '™':
            return 0x99;
          case 'š':
            return 0x9a;
          case '›':
            return 0x9b;
          case 'œ':
            return 0x9c;
          case 'ž':
            return 0x9e;
          case 'Ÿ':
            return 0x9f;
          // a
          case '¡':
            return 0xa1;
          case '¢':
            return 0xa2;
          case '£':
            return 0xa3;
          case '¤':
            return 0xa4;
          case '¥':
            return 0xa5;
          case '¦':
            return 0xa6;
          case '§':
            return 0xa7;
          case '¨':
            return 0xa8;
          case '©':
            return 0xa9;
          case 'ª':
            return 0xaa;
          case '«':
            return 0xab;
          case '¬':
            return 0xac;
          case '®':
            return 0xae;
          case '¯':
            return 0xaf;
          // b
          case '°':
            return 0xb0;
          case '±':
            return 0xb1;
          case '²':
            return 0xb2;
          case '³':
            return 0xb3;
          case '´':
            return 0xb4;
          case 'µ':
            return 0xb5;
          case '¶':
            return 0xb6;
          case '·':
            return 0xb7;
          case '¸':
            return 0xb8;
          case '¹':
            return 0xb9;
          case 'º':
            return 0xba;
          case '»':
            return 0xbb;
          case '¼':
            return 0xbc;
          case '½':
            return 0xbd;
          case '¾':
            return 0xbe;
          case '¿':
            return 0xbf;
          // c
          case 'À':
            return 0xc0;
          case 'Á':
            return 0xc1;
          case 'Â':
            return 0xc2;
          case 'Ã':
            return 0xc3;
          case 'Ä':
            return 0xc4;
          case 'Å':
            return 0xc5;
          case 'Æ':
            return 0xc6;
          case 'Ç':
            return 0xc7;
          case 'È':
            return 0xc8;
          case 'É':
            return 0xc9;
          case 'Ê':
            return 0xca;
          case 'Ë':
            return 0xcb;
          case 'Ì':
            return 0xcc;
          case 'Í':
            return 0xcd;
          case 'Î':
            return 0xce;
          case 'Ï':
            return 0xcf;
          // d
          case 'Ð':
            return 0xd0;
          case 'Ñ':
            return 0xd1;
          case 'Ò':
            return 0xd2;
          case 'Ó':
            return 0xd3;
          case 'Ô':
            return 0xd4;
          case 'Õ':
            return 0xd5;
          case 'Ö':
            return 0xd6;
          case '×':
            return 0xd7;
          case 'Ø':
            return 0xd8;
          case 'Ù':
            return 0xd9;
          case 'Ú':
            return 0xda;
          case 'Û':
            return 0xdb;
          case 'Ü':
            return 0xdc;
          case 'Ý':
            return 0xdd;
          case 'Þ':
            return 0xde;
          case 'ß':
            return 0xdf;
          // e
          case 'à':
            return 0xe0;
          case 'á':
            return 0xe1;
          case 'â':
            return 0xe2;
          case 'ã':
            return 0xe3;
          case 'ä':
            return 0xe4;
          case 'å':
            return 0xe5;
          case 'æ':
            return 0xe6;
          case 'ç':
            return 0xe7;
          case 'è':
            return 0xe8;
          case 'é':
            return 0xe9;
          case 'ê':
            return 0xea;
          case 'ë':
            return 0xeb;
          case 'ì':
            return 0xec;
          case 'í':
            return 0xed;
          case 'î':
            return 0xee;
          case 'ï':
            return 0xef;
          // f
          case 'ð':
            return 0xf0;
          case 'ñ':
            return 0xf1;
          case 'ò':
            return 0xf2;
          case 'ó':
            return 0xf3;
          case 'ô':
            return 0xf4;
          case 'õ':
            return 0xf5;
          case 'ö':
            return 0xf6;
          case '÷':
            return 0xf7;
          case 'ø':
            return 0xf8;
          case 'ù':
            return 0xf9;
          case 'ú':
            return 0xfa;
          case 'û':
            return 0xfb;
          case 'ü':
            return 0xfc;
          case 'ý':
            return 0xfd;
          case 'þ':
            return 0xfe;
          case 'ÿ':
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

    function CODE128EncoderHP() {
      OB.ESCPOS.CODE128Encoder.call(this);
      this.encodechar = c => {
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
    }

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

      if (code.length > 12) {
        line = OB.ARRAYS.append(line, this.BAR_WIDTH2);
      }

      line = OB.ARRAYS.append(line, this.BAR_HRIFONT1);

      return line;
    };

    this.NEW_LINE = new Uint8Array([0x0d, 0x0a]);
    this.CHAR_SIZE_0 = new Uint8Array([0x1d, 0x21, 0x00]);
    this.CHAR_SIZE_1 = new Uint8Array([0x1d, 0x21, 0x01]);
    this.CHAR_SIZE_2 = new Uint8Array([0x1d, 0x21, 0x11]);
    this.CHAR_SIZE_3 = new Uint8Array([0x1d, 0x21, 0x21]);
    this.BOLD_SET = new Uint8Array([0x1b, 0x45, 0x01]);
    this.BOLD_RESET = new Uint8Array([0x1b, 0x45, 0x00]);
    this.UNDERLINE_SET = new Uint8Array([0x1b, 0x2d, 0x01]);
    this.UNDERLINE_RESET = new Uint8Array([0x1b, 0x2d, 0x00]);
    this.DRAWER_OPEN = new Uint8Array([0x1b, 0x70, 0x00, 0x32, -0x06]);
    this.PARTIAL_CUT_1 = new Uint8Array([0x1d, 0x56, 0x41, 0x30]);
    this.IMAGE_HEADER = new Uint8Array([0x1b, 0x4b]);
    this.BAR_CODE128TYPE = new Uint8Array([0x68]);
    this.encoderText = new EncoderHP(this.encoderText);
    this.encoderCODE128 = new CODE128EncoderHP();
    this.CODE_TABLE = new Uint8Array([0x1b, 0x74, 0x08]);
    this.SELECT_PRINTER = new Uint8Array([0x1b, 0x3d, 0x02]);
  }

  OB.PRINTERTYPES.GENERICUSB.register({
    name: 'HP A799',
    vendorId: 0x05d9,
    productId: 0xa795,
    ESCPOS: ESCPOSHP
  });
})();
