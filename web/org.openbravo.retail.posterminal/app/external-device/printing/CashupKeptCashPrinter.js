/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function CashupKeptCashPrinterDefinition() {
  class CashupKeptCashPrinter {
    constructor() {
      this.controller = new OB.App.Class.ExternalDeviceController();
    }

    async printKeptCash(message) {
      const messageData = message.messageObj;
      const { cashupData, keptCashData } = messageData.data;
      const printSettings = messageData.data.printSettings || {};
      await this.doPrintKeptCash(cashupData, keptCashData, printSettings);
    }

    async doPrintKeptCash(cashupData, keptCashData, printSettings) {
      try {
        const prePrintData = await this.controller.executeHooks(
          'OBPRINT_PrePrint',
          {
            forcePrint: printSettings.forcePrint,
            offline: printSettings.offline,
            cashup: cashupData,
            forcedtemplate: printSettings.forcedtemplate,
            model: this.legacyPrinter ? this.legacyPrinter.model : null,
            cancelOperation: false
          }
        );

        if (prePrintData.cancelOperation === true) {
          return;
        }

        const template = await OB.App.PrintTemplateStore.get(
          'printCashUpKeptCashTemplate'
        );

        await this.controller.selectPrinter({
          isRetry: false,
          skipSelectPrinters: printSettings.skipSelectPrinters
        });

        const {
          printablePaymentMethods,
          currentSafeBoxName,
          safeBoxPaymentMethods
        } = keptCashData;

        await this.controller.print(template, {
          cashup: cashupData,
          currentSafeBoxName,
          safeBoxPaymentMethods,
          printablePaymentMethods
        });
      } catch (error) {
        OB.error(`Error printing kept cash report: ${error}`);
      }
    }
  }
  OB.App.Class.CashupKeptCashPrinter = CashupKeptCashPrinter;
})();
