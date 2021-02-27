/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function HardwareManagerServerDefinition() {
  // retrieve the list of hardware URLs
  function getHardwareURLs() {
    const urlList = OB.App.TerminalProperty.get('hardwareURL') || [];
    const printers = urlList
      .filter(hwURL => hwURL.hasReceiptPrinter)
      .map(hwURL => toPrinterURL(hwURL.hardwareURL, '/printer'));
    const pdfPrinters = urlList
      .filter(hwUrl => hwUrl.hasPDFPrinter)
      .map(hwURL => toPrinterURL(hwURL.hardwareURL, '/printerpdf'));

    const hwURLs = [...printers, ...pdfPrinters];

    const terminal = OB.App.TerminalProperty.get('terminal') || {};
    if (terminal.hardwareurl) {
      hwURLs.push(toPrinterURL(terminal.hardwareurl, '/printer'));
    }
    if (terminal.scaleurl) {
      hwURLs.push(terminal.scaleurl);
    }

    return hwURLs;
  }

  // includes the printer type in the URL (if required)
  function toPrinterURL(url, printerType) {
    return `${url}${url.endsWith(printerType) ? '' : printerType}`;
  }

  /**
   * Allows to handle connectivity with the backend server
   */
  OB.App.Class.HardwareManagerServer = class HardwareManagerServer extends OB
    .App.Class.RemoteServer {
    constructor() {
      super('HardwareManager');
      this.hardwareURLs = getHardwareURLs();
    }

    isAttendedURL(url) {
      return this.hardwareURLs.includes(url);
    }
  };

  OB.App.RemoteServerController.registerRemoteServer(
    new OB.App.Class.HardwareManagerServer()
  );
})();
