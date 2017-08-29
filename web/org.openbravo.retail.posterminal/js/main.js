/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, console*/

(function () {

  /**
   * catches application wide uncaught exceptions
   * overrides the mobile.core's 'onerror' assignment
   */
  window.onerror = function (message, url, line, column, errorObj) {
    if (!errorObj) {
      if (typeof (message) !== 'string') {
        return;
      }
    }
    var errorMessage;
    if (errorObj) {
      // the last 2 arguments may not be implemented by the browser
      errorMessage = errorObj.stack;
      console.log(line, column);
    } else {
      errorMessage = message + "; line: " + url + ":" + line;
    }
    // try to log the error in the backend
    if (OB.error) {
      if (OB.UTIL && OB.UTIL.showError) {
        OB.UTIL.showError(errorMessage);
      } else {
        // the OB.error is already being sent in the showError method
        OB.error(errorMessage);
      }
    } else {
      console.error(errorMessage);
    }
  };

  /**
   * Global versions for WebPOS
   */
  // Add the current WebPOS version
  OB.UTIL.VersionManagement.current.posterminal = {
    year: 16,
    major: 2,
    minor: 0
  };
  // Add the current WebSQL database version for WebPOS
  OB.UTIL.VersionManagement.current.posterminal.WebSQLDatabase = {
    name: 'WEBPOS',
    size: 4 * 1024 * 1024,
    displayName: 'Openbravo Web POS',
    dbVersion: OB.UTIL.VersionManagement.current.posterminal.year + "." + OB.UTIL.VersionManagement.current.posterminal.major + OB.UTIL.VersionManagement.current.posterminal.minor
  };

  /**
   * Register active deprecations.
   * Deprecations must be registered before calling the 'deprecated' method
   */
  OB.UTIL.VersionManagement.registerDeprecation(27366, {
    year: 14,
    major: 4,
    minor: 0
  }, "The use of 'OB.POS.terminal' is deprecated. Please use 'OB.MobileApp.view' instead.");

  OB.UTIL.VersionManagement.registerDeprecation(27367, {
    year: 14,
    major: 4,
    minor: 0
  }, "The use of 'OB.POS.terminal.terminal' is deprecated. Please use 'OB.MobileApp.model' instead.");

  OB.UTIL.VersionManagement.registerDeprecation(27646, {
    year: 14,
    major: 4,
    minor: 0
  }, "The use of 'OB.POS.modelterminal' is deprecated. Please use 'OB.MobileApp.model' instead.");

  OB.UTIL.VersionManagement.registerDeprecation(27911, {
    year: 14,
    major: 2,
    minor: 5
  }, "The use of OB.MobileApp.model.get('documentsequence'), OB.MobileApp.model.get('quotationDocumentSequence') and the 'seqNoReady' event are deprecated. Please use 'OB.MobileApp.model.getNextDocumentno()' and 'OB.MobileApp.model.getNextQuotationno()' instead.");

  OB.UTIL.VersionManagement.registerDeprecation(35607, {
    year: 17,
    major: 3,
    minor: 0
  }, "The use of 'checkApproval' is deprecated. Please use 'OB.UTIL.checkApproval' instead.");

  /**
   * Global deprecations have to be executed somewhere, this is a good place
   */

}());