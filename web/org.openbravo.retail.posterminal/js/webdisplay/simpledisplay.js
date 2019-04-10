/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global localStorage, URLSearchParams */

(function () {

  function printValue(webdisplay, value) {
    var obj = JSON.parse(value);
    if (obj && obj.data) {
      webdisplay.print(obj.data);
    }
  }

  function startDisplayListener(terminal, display) {
    var webdisplay = new OB.WEBDisplay(display);
    var displaykey = 'WEBPOSHW.' + terminal + '.1';

    window.addEventListener('storage', function (e) {
      if (e.key === displaykey) { // Display request
        printValue(webdisplay, e.newValue);
      }
    });

    // Print the current value
    printValue(webdisplay, localStorage.getItem(displaykey));
  }

  function SimpleDisplay() {
    this.lines = [];
    this.content = document.createElement('div');
    this.content.setAttribute('class', 'display-screen');

    this.createLine();
    this.createLine();
  }

  SimpleDisplay.prototype.createLine = function () {
    var line = document.createTextNode(' ');
    var div = document.createElement('div');
    div.setAttribute('class', 'display-text');
    div.appendChild(line);
    this.content.appendChild(div);
    this.lines.push(line);
  };

  SimpleDisplay.prototype.getElement = function () {
    return this.content;
  };

  SimpleDisplay.prototype.printLine = function (i, text) {
    this.lines[i].data = text;
  };

  window.OB = window.OB || {};
  OB.Display = {
    SimpleDisplay: SimpleDisplay,
    startDisplayListener: startDisplayListener
  };

}());