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

  function SimpleDisplay() {
    this.lines = [];
    this.content = document.createElement('div');

    this.createLine();
    this.createLine();
  }

  SimpleDisplay.prototype.createLine = function () {
    var line = document.createTextNode('');
    var div = document.createElement('div');
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
  OB.SimpleDisplay = SimpleDisplay;

}());