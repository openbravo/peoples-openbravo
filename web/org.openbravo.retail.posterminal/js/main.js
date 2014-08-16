/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, Backbone, window, confirm, OB, console, localStorage */

(function () {

  // alert all errors
  window.onerror = function (e, url, line) {
    var errorInfo;
    if (typeof (e) === 'string') {
      errorInfo = e + '. Line number: ' + line + '. File uuid: ' + url + '.';
      OB.UTIL.showError(errorInfo);
      OB.error(errorInfo);
    }
  };

}());