/*
 ************************************************************************************
 * Copyright (C) 2018-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(() => {
  const padStart = (txt, length, character) => {
    const result = txt.padStart(length, character);
    if (result.length > length) {
      return result.substring(result.length - length);
    }
    return result;
  };

  const padEnd = (txt, length, character) => {
    const result = txt.padEnd(length, character);
    if (result.length > length) {
      return result.substring(0, length);
    }
    return result;
  };

  const padCenter = (txt, length, character) => {
    const midlength = (length + txt.length) / 2;
    return padEnd(padStart(txt, midlength, character), length, character);
  };

  const getImageData = data => {
    return new Promise((resolve, reject) => {
      const img = new Image();
      const rejectTimeOut = setTimeout(() => {
        img.onload = null;
        reject();
      }, 2000);
      img.src = data.image;
      img.onload = () => {
        clearTimeout(rejectTimeOut);
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, img.width, img.height);
        img.style.display = 'none'; // This inline style is permited
        // eslint-disable-next-line no-param-reassign
        data.imagedata = ctx.getImageData(0, 0, canvas.width, canvas.height);
        resolve(data);
      };
      img.onerror = () => {
        clearTimeout(rejectTimeOut);
        reject();
      };
    });
  };

  function WEBPrinter(printertype, images) {
    this.webdevice = new printertype.WebDevice(printertype);
    this.images = images || {};
    this.escpos = null;
  }

  WEBPrinter.prototype.connected = function connected() {
    return this.webdevice.connected();
  };

  WEBPrinter.prototype.request = function request() {
    return this.webdevice.request().then(deviceinfo => {
      this.escpos = deviceinfo.ESCPOS
        ? new deviceinfo.ESCPOS()
        : OB.ESCPOS.standardinst;
    });
  };

  WEBPrinter.prototype.print = function print(doc) {
    const parser = new DOMParser();
    const dom = parser.parseFromString(doc, 'application/xml');

    if (dom.documentElement.nodeName === 'parsererror') {
      return Promise.reject(new Error('Error while parsing XML template.'));
    }

    return this.processDOM(dom);
  };

  WEBPrinter.prototype.processDOM = function processDOM(dom) {
    let result = Promise.resolve();
    let printerdocs;

    Array.from(dom.children).forEach(el => {
      if (el.nodeName === 'output') {
        result = result
          .then(() => this.processOutput(el))
          .then(output => {
            printerdocs = output;
          });
      }
    });

    return result.then(() => {
      if (printerdocs && printerdocs.length) {
        return this.webdevice.print(printerdocs);
      }
      return Promise.resolve(); // Nothing printed
    });
  };

  WEBPrinter.prototype.processOutput = function processOutput(dom) {
    let result = Promise.resolve();
    let printerdocs = new Uint8Array();
    Array.from(dom.children).forEach(el => {
      if (el.nodeName === 'ticket') {
        result = result
          .then(() => this.processTicket(el))
          .then(output => {
            printerdocs = OB.ARRAYS.append(printerdocs, output);
          });
      } else if (el.nodeName === 'opendrawer') {
        result = result.then(() => {
          printerdocs = OB.ARRAYS.append(printerdocs, this.escpos.DRAWER_OPEN);
        });
      }
    });
    return result.then(() => printerdocs);
  };

  WEBPrinter.prototype.processTicket = function processTicket(dom) {
    let result = Promise.resolve();
    let printerdoc = new Uint8Array();
    Array.from(dom.children).forEach(el => {
      if (el.nodeName === 'line') {
        result = result
          .then(() => this.processLine(el))
          .then(output => {
            printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.CODE_TABLE);
            printerdoc = OB.ARRAYS.append(printerdoc, output);
            printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.NEW_LINE);
          });
      } else if (el.nodeName === 'barcode') {
        result = result
          .then(() => this.processBarcode(el))
          .then(output => {
            printerdoc = OB.ARRAYS.append(printerdoc, output);
          });
      } else if (el.nodeName === 'qr') {
        result = result
          .then(() => this.processQR(el))
          .then(output => {
            printerdoc = OB.ARRAYS.append(printerdoc, output);
          });
      } else if (el.nodeName === 'image') {
        result = result
          .then(() => this.processImage(el))
          .then(output => {
            printerdoc = OB.ARRAYS.append(printerdoc, output);
          });
      }
    });

    return result.then(() => {
      printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.NEW_LINE);
      printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.NEW_LINE);
      printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.NEW_LINE);
      printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.NEW_LINE);
      printerdoc = OB.ARRAYS.append(printerdoc, this.escpos.PARTIAL_CUT_1);
      return printerdoc;
    });
  };

  WEBPrinter.prototype.processLine = function processLine(dom) {
    let line = new Uint8Array();
    const fontsize = dom.getAttribute('size');
    let el;

    if (fontsize === '1') {
      line = OB.ARRAYS.append(line, this.escpos.CHAR_SIZE_1);
    } else if (fontsize === '2') {
      line = OB.ARRAYS.append(line, this.escpos.CHAR_SIZE_2);
    } else if (fontsize === '3') {
      line = OB.ARRAYS.append(line, this.escpos.CHAR_SIZE_3);
    } else {
      line = OB.ARRAYS.append(line, this.escpos.CHAR_SIZE_0);
    }

    for (let i = 0; i < dom.children.length; i += 1) {
      el = dom.children[i];
      if (el.nodeName === 'text') {
        let txt = el.textContent;
        const len = parseInt(el.getAttribute('length'), 10) || txt.length;
        const align = el.getAttribute('align');
        const bold = el.getAttribute('bold');
        const uderline = el.getAttribute('underline');

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

  WEBPrinter.prototype.processBarcode = function processBarcode(el) {
    const type = el.getAttribute('type');
    const position = el.getAttribute('position');
    const code = el.textContent;

    if (type === 'EAN13') {
      return Promise.resolve(this.escpos.transEAN13(code, position));
    }
    if (type === 'CODE128') {
      return Promise.resolve(this.escpos.transCODE128(code, position));
    }
    // Unknown barcode type
    let line = new Uint8Array();
    line = OB.ARRAYS.append(line, this.escpos.CENTER_JUSTIFICATION);
    line = OB.ARRAYS.append(line, this.escpos.NEW_LINE);
    line = OB.ARRAYS.append(line, this.escpos.encoderText.encode(type));
    line = OB.ARRAYS.append(line, this.escpos.encoderText.encode(': '));
    line = OB.ARRAYS.append(line, this.escpos.encoderText.encode(code));
    line = OB.ARRAYS.append(line, this.escpos.NEW_LINE);
    line = OB.ARRAYS.append(line, this.escpos.LEFT_JUSTIFICATION);
    return Promise.resolve(line);
  };

  WEBPrinter.prototype.processQR = function processQR(el) {
    const quality = el.getAttribute('quality') || 'L';
    const size = el.getAttribute('size') || 'M';
    const code = el.textContent;
    return Promise.resolve(this.escpos.transQR(code, quality, size));
  };

  WEBPrinter.prototype.processImage = function processImage(el) {
    const image = el.textContent;
    const imagetype = el.getAttribute('type');
    let imageurl = this.images[image] || image;
    if (imagetype === 'data') {
      imageurl = `data:png;base64,${image}`;
    } else {
      imageurl = this.images[image] || image;
    }

    return getImageData({
      image: imageurl
    })
      .then(result => this.escpos.transImage(result.imagedata))
      .catch(() => {
        // Log the error and continue printing the receipt
        OB.warn(`Cannot load receipt image '${image}' -> '${imageurl}'`);
        return new Uint8Array();
      });
  };

  OB.WEBPrinter = WEBPrinter;
})();
