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
      const { cashup } = messageData.data;
      const printSettings = messageData.data.printSettings || {};

      // print cashup
      await this.doPrintCashup(cashup, printSettings);
    }

    async doPrintCashup(cashup, printSettings) {
      try {
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

        const templateWithData = await OB.App.PrintTemplateStore.get(
          'printCashup'
        ).generate({ cashup });

        await this.selectPrinter({
          isRetry: false,
          skipSelectPrinters: printSettings.skipSelectPrinters
        });

        await this.controller.print(templateWithData, cashup);
      } catch (error) {
        OB.error(`Error printing cashup: ${error}`);
      }
    }
  }
  OB.App.Class.CashupPrinter = CashupPrinter;
})();
