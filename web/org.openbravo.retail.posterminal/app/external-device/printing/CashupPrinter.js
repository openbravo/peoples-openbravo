/*
 ************************************************************************************
 * Copyright (C) 2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function CashupPrinterDefinition() {
  class CashupPrinter {
    constructor() {
      this.controller = new OB.App.Class.ExternalDeviceController();
    }

    async printCashup(message) {
      const messageData = message.messageObj;
      const { cashupData } = messageData.data;
      const printSettings = messageData.data.printSettings || {};
      await this.doPrintCashup(cashupData, printSettings);
    }

    async doPrintCashup(cashup, printSettings) {
      try {
        const cashupData = cashup;
        const prePrintData = await this.controller.executeHooks(
          'OBPRINT_PrePrint',
          {
            forcePrint: printSettings.forcePrint,
            offline: printSettings.offline,
            cashup,
            forcedtemplate: printSettings.forcedtemplate,
            model: this.legacyPrinter ? this.legacyPrinter.model : null,
            cancelOperation: false
          }
        );

        if (prePrintData.cancelOperation === true) {
          return;
        }

        const template = await OB.App.PrintTemplateStore.get(
          'printCashUpTemplate'
        );

        await this.controller.selectPrinter({
          isRetry: false,
          skipSelectPrinters: printSettings.skipSelectPrinters
        });

        await this.controller.print(template, { cashup: cashupData });
      } catch (error) {
        OB.error(`Error printing cashup: ${error}`);
      }
    }
  }
  OB.App.Class.CashupPrinter = CashupPrinter;
})();
