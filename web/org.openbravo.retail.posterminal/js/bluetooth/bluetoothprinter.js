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

  function padStart(txt, length) {
    var result = txt.padStart(length);
    if (result.length > length) {
      return result.substring(result.length - length);
    } else {
      return result;
    }
  }

  function padEnd(txt, length) {
    var result = txt.padEnd(length);
    if (result.length > length) {
      return result.substring(0, length);
    } else {
      return result;
    }
  }

  function padCenter(txt, length) {
    var midlength = (length + txt.length) / 2;
    return padEnd(padStart(txt, midlength), length);
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
      }
    }.bind(this));

    return printerdoc;
  };

  OB.BluetoothPrinter.prototype.processLine = function (dom) {
    var line = new Uint8Array();

    Array.from(dom.children).forEach(function (el) {
      if (el.nodeName === 'text') {
        var txt = el.textContent;
        var len = parseInt(el.getAttribute('length'), 10) || txt.length;
        var align = el.getAttribute('align');
        if (align === 'right') {
          txt = padStart(txt, len);
        } else if (align === 'center') {
          txt = padCenter(txt, len);
        } else {
          txt = padEnd(txt, len);
        }
        line = append(line, encoder.encode(txt));
      }
    }.bind(this));

    return line;
  };

}());