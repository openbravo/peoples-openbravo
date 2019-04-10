/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
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

  var display = new OB.SimpleDisplay();
  document.body.appendChild(display.getElement());

  var webdisplay = new OB.WEBDisplay(display);

  var urlParams = new URLSearchParams(window.location.search);
  var terminal = urlParams.get('terminal');
  var displaykey = 'WEBPOSHW.' + terminal + '.1';

  printValue(webdisplay, localStorage.getItem(displaykey));

  window.addEventListener('storage', function (e) {
    if (e.key === displaykey) { // Display request
      printValue(webdisplay, e.newValue);
    }
  });

}());