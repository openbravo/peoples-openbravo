/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function HardwareManagerServerDefinition() {
  /**
   * Allows to handle connectivity with the HardwareManager remote server
   */
  class HardwareManagerServer extends OB.App.Class.RemoteServer {
    constructor() {
      super('HardwareManagerServer');
      this.label = 'OBPOS_HardwareManagerServer';
    }

    isAttendedURL(url) {
      if (!this.hardwareURLs) {
        this.hardwareURLs = getHardwareURLs();
      }
      const hardwareURLs = this.hardwareURLs || [];
      return hardwareURLs.includes(url);
    }
  }

  // retrieve the list of hardware URLs
  function getHardwareURLs() {
    const terminal = OB.App.TerminalProperty.get('terminal');
    if (!terminal) {
      // terminal information not yet available
      return undefined;
    }
    const terminalURLs = [];
    if (terminal.hardwareurl) {
      terminalURLs.push(toPrinterURL(terminal.hardwareurl, '/printer'));
      terminalURLs.push(toStatusURL(terminal.hardwareurl, '/status.json'));
    }
    if (terminal.scaleurl) {
      terminalURLs.push(toPrinterURL(terminal.scaleurl, '/scale'));
    }
    const urlList = OB.App.TerminalProperty.get('hardwareURL') || [];
    const printers = urlList
      .filter(hwURL => hwURL.hasReceiptPrinter)
      .map(hwURL => toPrinterURL(hwURL.hardwareURL, '/printer'));
    const status = urlList
      .filter(hwURL => hwURL.hasReceiptPrinter)
      .map(hwURL => toStatusURL(hwURL.hardwareURL, '/status.json'));
    const pdfPrinters = urlList
      .filter(hwUrl => hwUrl.hasPDFPrinter)
      .map(hwURL => toPrinterURL(hwURL.hardwareURL, '/printerpdf'));

    return [...terminalURLs, ...printers, ...status, ...pdfPrinters];
  }

  // includes the printer type in the URL (if required)
  function toPrinterURL(url, printerType) {
    return `${url}${url.endsWith(printerType) ? '' : printerType}`;
  }

  // transforms a printer url into an url to check the status
  function toStatusURL(url) {
    if (url.endsWith('/printer')) {
      return `${url.substring(0, url.length - 8)}/status.json`;
    }
    return `${url}/status.json`;
  }

  OB.App.RemoteServerController.registerRemoteServer(
    new HardwareManagerServer(),
    { priority: 50 }
  );
})();
