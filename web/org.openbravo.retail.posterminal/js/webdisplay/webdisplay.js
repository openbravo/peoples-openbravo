/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Promise, DOMParser  */

(function () {

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

  var WEBDisplay = function (display) {
      this.display = display;
      };

  WEBDisplay.prototype.print = function (doc) {
    var parser = new DOMParser();
    var dom = parser.parseFromString(doc, "application/xml");

    if (dom.documentElement.nodeName === "parsererror") {
      return Promise.reject("Error while parsing XML template.");
    }

    return this.processDOM(dom);
  };

  WEBDisplay.prototype.processDOM = function (dom) {
    var result = Promise.resolve();
    Array.from(dom.children).forEach(function (el) {
      if (el.nodeName === 'output') {
        result = result.then(function () {
          return this.processOutput(el);
        }.bind(this));
      }
    }.bind(this));
    return result;
  };

  WEBDisplay.prototype.processOutput = function (dom) {
    var result = Promise.resolve();
    Array.from(dom.children).forEach(function (el) {
      if (el.nodeName === 'display') {
        result = result.then(function () {
          return this.processDisplay(el);
        }.bind(this));
      }
    }.bind(this));
    return result;
  };

  WEBDisplay.prototype.processDisplay = function (dom) {
    var result = Promise.resolve();
    var i = 0;
    Array.from(dom.children).forEach(function (el) {
      if (el.nodeName === 'line') {
        result = result.then(function () {
          return this.processLine(el);
        }.bind(this)).then(function (line) {
          this.display.printLine(i++, line);
        }.bind(this));
      }
    }.bind(this));
    return result;
  };

  WEBDisplay.prototype.processLine = function (dom) {
    var line = '';
    var i, el;

    for (i = 0; i < dom.children.length; i++) {
      el = dom.children[i];
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
        line = line + txt;
      }
    }

    return Promise.resolve(line);
  };

  window.OB = window.OB || {};
  OB.WEBDisplay = WEBDisplay;
}());