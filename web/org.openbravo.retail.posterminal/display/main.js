/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, URLSearchParams */

(function () {

  var display = new OB.Display.SimpleDisplay();
  document.body.appendChild(display.getElement());

  var urlParams = new URLSearchParams(window.location.search);
  var terminal = urlParams.get('terminal');

  OB.Display.startDisplayListener(terminal, display);

}());