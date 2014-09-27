/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, Backbone, window, confirm, console, localStorage */

var OB = window.OB || {};

(function () {


  // alert all errors
  window.onerror = function (e, url, line) {
    var errorInfo;
    if (typeof (e) === 'string') {
      errorInfo = e + '. Line number: ' + line + '. File uuid: ' + url + '.';
      OB.UTIL.showError(errorInfo);
      OB.error("main.js: " + errorInfo);
    }
  };

  // Add the current WebPOS version
  OB.UTIL.VersionManagement.current.posterminal = {
    rootName: 'RR14Q',
    major: '4',
    minor: '0'
  };
  // Add the current WebSQL database version for WebPOS
  OB.UTIL.VersionManagement.current.posterminal.WebSQLDatabase = {
    name: 'WEBPOS',
    size: 4 * 1024 * 1024,
    displayName: 'Openbravo Web POS',
    major: '0',
    minor: '7'
  };

}());
