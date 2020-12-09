/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
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

  function getImageData(data) {
    return new Promise(function(resolve, reject) {
      var img = new Image();
      img.src = data.image;
      img.onload = function() {
        var canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        var ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, img.width, img.height);
        img.style.display = 'none'; /*This inline style is permited*/
        data.imagedata = ctx.getImageData(0, 0, canvas.width, canvas.height);
        resolve(data);
      };
      img.onerror = reject;
    });
  }

  var WEBPrinter = function(printertype, images) {
    this.webdevice = new printertype.WebDevice(printertype);
    this.images = images || {};
    this.escpos = null;
  };

  WEBPrinter.prototype.connected = function() {
    return this.webdevice.connected();
  };

  WEBPrinter.prototype.request = function() {
    return this.webdevice.request().then(
      function(deviceinfo) {
        this.escpos = deviceinfo.ESCPOS
          ? new deviceinfo.ESCPOS()
          : OB.ESCPOS.standardinst;
      }.bind(this)
    );
  };

  WEBPrinter.prototype.print = function(doc) {
    var parser = new DOMParser();
    var dom = parser.parseFromString(doc, 'application/xml');

    if (dom.documentElement.nodeName === 'parsererror') {
      return Promise.reject('Error while parsing XML template.');
    }

    return this.processDOM(dom);
  };

  WEBPrinter.prototype.processDOM = function(dom) {
    var result = Promise.resolve();
    var printerdocs;

    Array.from(dom.children).forEach(
      function(el) {
        if (el.nodeName === 'output') {
          result = result
            .then(
              function() {
                return this.processOutput(el);
              }.bind(this)
            )
            .then(
              function(output) {
                printerdocs = output;
              }.bind(this)
            );
        }
      }.bind(this)
    );

    return result.then(
      function() {
        if (printerdocs && printerdocs.length) {
          return this.webdevice.print(printerdocs);
        } else {
          return Promise.resolve(); // Nothing printed
        }
      }.bind(this)
    );
  };

  WEBPrinter.prototype.processOutput = function(dom) {
    var result = Promise.resolve();
    var printerdocs = new Uint8Array();
    Array.from(dom.children).forEach(
      function(el) {
        if (el.nodeName === 'ticket') {
          result = result
            .then(
              function() {
                return this.processTicket(el);
              }.bind(this)
            )
            .then(
              function(output) {
                printerdocs = OB.ARRAYS.append(printerdocs, output);
              }.bind(this)
            );
        } else if (el.nodeName === 'opendrawer') {
          result = result.then(
            function() {
              printerdocs = OB.ARRAYS.append(
                printerdocs,
                this.escpos.DRAWER_OPEN
              );
            }.bind(this)
          );
        }
      }.bind(this)
    );
    return result.then(
      function() {
        return printerdocs;
      }.bind(this)
    );
  };

  WEBPrinter.prototype.processTicket = function(dom) {
    var result = Promise.resolve();
    var printerdoc = new Uint8Array();
    Array.from(dom.children).forEach(
      function(el) {
        if (el.nodeName === 'line') {
          result = result
            .then(
              function() {
                return this.processLine(el);
              }.bind(this)
            )
            .then(
              function(output) {
                printerdoc = OB.ARRAYS.append(printerdoc, output);
                printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.NEW_LINE);
              }.bind(this)
            );
        } else if (el.nodeName === 'barcode') {
          result = result
            .then(
              function() {
                return this.processBarcode(el);
              }.bind(this)
            )
            .then(
              function(output) {
                printerdoc = OB.ARRAYS.append(printerdoc, output);
              }.bind(this)
            );
        } else if (el.nodeName === 'qr') {
          result = result
            .then(
              function() {
                return this.processQR(el);
              }.bind(this)
            )
            .then(
              function(output) {
                printerdoc = OB.ARRAYS.append(printerdoc, output);
              }.bind(this)
            );
        } else if (el.nodeName === 'image') {
          result = result
            .then(
              function() {
                return this.processImage(el);
              }.bind(this)
            )
            .then(
              function(output) {
                printerdoc = OB.ARRAYS.append(printerdoc, output);
              }.bind(this)
            );
        }
      }.bind(this)
    );

    return result.then(
      function() {
        printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.NEW_LINE);
        printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.NEW_LINE);
        printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.NEW_LINE);
        printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.NEW_LINE);
        printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.PARTIAL_CUT_1);
        return printerdoc;
      }.bind(this)
    );
  };

  WEBPrinter.prototype.processLine = function(dom) {
    var line = new Uint8Array();
    var fontsize = dom.getAttribute('size');
    var i, el;

    if (fontsize === '1') {
      line = OB.ARRAYS.append(line, this.escpos.CHAR_SIZE_1);
    } else if (fontsize === '2') {
      line = OB.ARRAYS.append(line, this.escpos.CHAR_SIZE_2);
    } else if (fontsize === '3') {
      line = OB.ARRAYS.append(line, this.escpos.CHAR_SIZE_3);
    } else {
      line = OB.ARRAYS.append(line, this.escpos.CHAR_SIZE_0);
    }

    for (i = 0; i < dom.children.length; i++) {
      el = dom.children[i];
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
          line = OB.ARRAYS.append(line, this.escpos.BOLD_SET);
        }
        if (uderline === 'true') {
          line = OB.ARRAYS.append(line, this.escpos.UNDERLINE_SET);
        }
        line = OB.ARRAYS.append(line, this.escpos.encoderText.encode(txt));
        if (bold === 'true') {
          line = OB.ARRAYS.append(line, this.escpos.BOLD_RESET);
        }
        if (uderline === 'true') {
          line = OB.ARRAYS.append(line, this.escpos.UNDERLINE_RESET);
        }
      } else if (el.nodeName === 'barcode') {
        // Barcodes should be a ticket tag level
        // but in case an barcode tag is wrongly included in a line tag lets print it
        return this.processBarcode(el);
      } else if (el.nodeName === 'qr') {
        // QR codes should be a ticket tag level
        // but in case an qr tag is wrongly included in a line tag lets print it
        return this.processQR(el);
      } else if (el.nodeName === 'image') {
        // Images should be a ticket tag level
        // but in case an image tag is wrongly included in a line tag lets print it
        return this.processImage(el);
      }
    }

    return Promise.resolve(line);
  };

  WEBPrinter.prototype.processBarcode = function(el) {
    var type = el.getAttribute('type');
    var position = el.getAttribute('position');
    var code = el.textContent;

    if (type === 'EAN13') {
      return Promise.resolve(this.escpos.transEAN13(code, position));
    } else if (type === 'CODE128') {
      return Promise.resolve(this.escpos.transCODE128(code, position));
    } else {
      // Unknown barcode type
      var line = new Uint8Array();
      line = OB.ARRAYS.append(line, this.escpos.CENTER_JUSTIFICATION);
      line = OB.ARRAYS.append(line, this.escpos.NEW_LINE);
      line = OB.ARRAYS.append(line, this.escpos.encoderText.encode(type));
      line = OB.ARRAYS.append(line, this.escpos.encoderText.encode(': '));
      line = OB.ARRAYS.append(line, this.escpos.encoderText.encode(code));
      line = OB.ARRAYS.append(line, this.escpos.NEW_LINE);
      line = OB.ARRAYS.append(line, this.escpos.LEFT_JUSTIFICATION);
      return Promise.resolve(line);
    }
  };

  WEBPrinter.prototype.processQR = function(el) {
    var quality = el.getAttribute('quality') || 'L';
    var size = el.getAttribute('size') || 'M';
    var code = el.textContent;
    return Promise.resolve(this.escpos.transQR(code, quality, size));
  };

  WEBPrinter.prototype.processImage = function(el) {
    var image = el.textContent;
    var imageurl = this.images[image] || image;

    return getImageData({
      image: imageurl
    })
      .then(
        function(result) {
          return this.escpos.transImage(result.imagedata);
        }.bind(this)
      )
      .catch(
        function(error) {
          // Log the error and continue printing the receipt
          OB.warn(
            "Cannot load receipt image '" + image + "' -> '" + imageurl + "'"
          );
          return new Uint8Array();
        }.bind(this)
      );
  };

  window.OB = window.OB || {};
  OB.WEBPrinter = WEBPrinter;
})();
