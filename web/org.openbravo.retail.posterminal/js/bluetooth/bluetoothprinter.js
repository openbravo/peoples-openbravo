/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Promise, Uint8Array, TextEncoder, DOMParser  */

(function () {

  window.OB = window.OB || {};

  function padStart(txt, length, character) {
    var result = txt.padStart(length, character);
    if (result.length > length) {
      return result.substring(result.length - length);
    } else {
      return result;
    }
  }

  function padEnd(txt, length, character) {
    var result = txt.padEnd(length, character);
    if (result.length > length) {
      return result.substring(0, length);
    } else {
      return result;
    }
  }

  function padCenter(txt, length, character) {
    var midlength = (length + txt.length) / 2;
    return padEnd(padStart(txt, midlength, character), length, character);
  }

  function append(a1, a2) {
    var tmp = new Uint8Array(a1.length + a2.length);
    tmp.set(a1, 0);
    tmp.set(a2, a1.byteLength);
    return tmp;
  }

  var encoder = new TextEncoder('utf-8');

  OB.BluetoothPrinter = function () {
    this.columns = 32;
    this.bluetooth = new OB.Bluetooth();
  };

  OB.BluetoothPrinter.prototype.request = function () {
    return this.bluetooth.request();
  };

  OB.BluetoothPrinter.prototype.print = function (doc) {

    var parser = new DOMParser();
    var dom = parser.parseFromString(doc, "application/xml");

    if (dom.documentElement.nodeName === "parsererror") {
      return Promise.reject("Error while parsing XML template.");
    }

    try {
      return this.processDOM(dom);
    } catch (ex) {
      return Promise.reject(ex);
    }
  };


  OB.BluetoothPrinter.prototype.processDOM = function (dom) {
    var output;

    Array.from(dom.children).forEach(function (el) {
      if (el.nodeName === 'output') {
        output = this.processOutput(el);
      }
    }.bind(this));

    if (output && output.length) {
      return this.bluetooth.print(output);
    } else {
      return Promise.resolve(); // Nothing printed
    }
  };

  OB.BluetoothPrinter.prototype.processOutput = function (dom) {
    var printerdocs = new Uint8Array();
    Array.from(dom.children).forEach(function (el) {
      if (el.nodeName === 'ticket') {
        printerdocs = append(printerdocs, this.processTicket(el));
      }
    }.bind(this));
    return printerdocs;
  };


  OB.BluetoothPrinter.prototype.processTicket = function (dom) {
    var printerdoc = new Uint8Array();
    Array.from(dom.children).forEach(function (el) {
      if (el.nodeName === 'line') {
        printerdoc = append(printerdoc, this.processLine(el));
        printerdoc = append(printerdoc, OB.ESCPOS.NEW_LINE);
      } else if (el.nodeName === 'barcode') {
        printerdoc = append(printerdoc, this.processBarcode(el));
      }
    }.bind(this));

    return printerdoc;
  };

  OB.BluetoothPrinter.prototype.processLine = function (dom) {
    var line = new Uint8Array();
    var fontsize = dom.getAttribute('size');

    if (fontsize === '1') {
      line = append(line, OB.ESCPOS.CHAR_SIZE_1);
    } else if (fontsize === '2') {
      line = append(line, OB.ESCPOS.CHAR_SIZE_2);
    } else if (fontsize === '3') {
      line = append(line, OB.ESCPOS.CHAR_SIZE_3);
    } else {
      line = append(line, OB.ESCPOS.CHAR_SIZE_0);
    }

    Array.from(dom.children).forEach(function (el) {
      if (el.nodeName === 'text') {
        var txt = el.textContent;
        var len = parseInt(el.getAttribute('length'), 10) || txt.length;
        var align = el.getAttribute('align');
        var bold = el.getAttribute('bold');
        var uderline = el.getAttribute('underline');

        if (align === 'right') {
          txt = padStart(txt, len);
        } else if (align === 'center') {
          txt = padCenter(txt, len);
        } else {
          txt = padEnd(txt, len);
        }

        if (bold === 'true') {
          line = append(line, OB.ESCPOS.BOLD_SET);
        }
        if (uderline === 'true') {
          line = append(line, OB.ESCPOS.UNDERLINE_SET);
        }
        line = append(line, encoder.encode(txt));
        if (bold === 'true') {
          line = append(line, OB.ESCPOS.BOLD_RESET);
        }
        if (uderline === 'true') {
          line = append(line, OB.ESCPOS.UNDERLINE_RESET);
        }

      }
    }.bind(this));

    return line;
  };

  OB.BluetoothPrinter.prototype.processBarcode = function (el) {
    var line = new Uint8Array();
    var position = el.getAttribute('position');
    var type = el.getAttribute('type');
    var barcode = el.textContent;

    line = append(line, OB.ESCPOS.CENTER_JUSTIFICATION);

    line = append(line, OB.ESCPOS.NEW_LINE);
    line = append(line, OB.ESCPOS.BAR_HEIGHT);
    if (position === 'none') {
      line = append(line, OB.ESCPOS.BAR_POSITIONNONE);
    } else {
      line = append(line, OB.ESCPOS.BAR_POSITIONDOWN);
    }

    if (type === 'EAN13') {
      line = append(line, OB.ESCPOS.BAR_WIDTH3);

      line = append(line, OB.ESCPOS.BAR_HRIFONT1);
      line = append(line, OB.ESCPOS.BAR_CODE02);

      barcode = padStart(barcode, 13, '0');
      barcode = barcode.substring(0, 12);
      line = append(line, encoder.encode(barcode));
      line = append(line, new Uint8Array([0x00]));
    } else if (type === 'CODE128') {
      if (barcode.length > 14) {
        line = append(line, OB.ESCPOS.BAR_WIDTH1);
      } else if (barcode.length > 8) {
        line = append(line, OB.ESCPOS.BAR_WIDTH2);
      } else {
        line = append(line, OB.ESCPOS.BAR_WIDTH3);
      }
      line = append(line, OB.ESCPOS.BAR_HRIFONT1);
      line = append(line, OB.ESCPOS.BAR_CODE128);

      var barcode128 = OB.ESCPOS.transCode128(barcode);
      line = append(line, new Uint8Array([barcode128.length + 2]));
      line = append(line, OB.ESCPOS.BAR_CODE128TYPE);
      line = append(line, barcode128);
      line = append(line, new Uint8Array([0x00]));
    } else { // Unknown barcode type
      line = append(line, encoder.encode(type));
      line = append(line, encoder.encode(': '));
      line = append(line, encoder.encode(barcode));
    }

    line = append(line, OB.ESCPOS.NEW_LINE);
    line = append(line, OB.ESCPOS.LEFT_JUSTIFICATION);
    return line;
  };

}());