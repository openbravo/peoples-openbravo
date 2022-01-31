/*
 ************************************************************************************
 * Copyright (C) 2012-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  const PrintCloseCash = function() {
    const terminal = OB.MobileApp.model.get('terminal');
    this.templateCloseCash = new OB.DS.HWResource(
      terminal.printCashUpTemplate || OB.OBPOSPointOfSale.Print.CashUpTemplate
    );
    this.templateKeptCash = new OB.DS.HWResource(
      terminal.printCashupKeptCashTemplate ||
        OB.OBPOSPointOfSale.Print.CashUpKeptCashTemplate
    );

    this.cancelOrDismiss = () => {
      OB.POS.navigate('retail.pointofsale');
      OB.MobileApp.view.$.confirmationContainer.setAttribute('openedPopup', '');
    };
    this.isRetry = false;
  };

  PrintCloseCash.prototype.print = function(report, summary, closed, callback) {
    const isLegacyTemplate = OB.App.PrintTemplateStore.get(
      'printCashUpTemplate'
    ).isLegacyTemplate();
    let convertedCashUpReport, convertedCountCashSummary;

    if (isLegacyTemplate) {
      convertedCashUpReport = report;
      convertedCountCashSummary = summary;
    } else {
      // Converting model to plain JS object to support migrated print template.
      convertedCashUpReport = JSON.parse(JSON.stringify(report));
      convertedCountCashSummary = JSON.parse(JSON.stringify(summary));
    }

    // callbacks definition
    const retryPrintCashup = () => {
      const printCloseCash = new OB.OBPOSCloseCash.Print.CloseCash();
      printCloseCash.isRetry = true;
      printCloseCash.print(
        convertedCashUpReport,
        convertedCountCashSummary,
        closed,
        this.cancelOrDismiss
      );
      return true;
    };
    const cancelfunc = () => {
      this.cancelOrDismiss();
      return true;
    };
    const printKeptCash = () => {
      const cashUpReport = JSON.parse(JSON.stringify(report));
      const countCashSummary = JSON.parse(JSON.stringify(summary));
      const paymentMethods = OB.MobileApp.model.get('payments');
      const numberOfCopies =
        OB.MobileApp.model.get('terminal').terminalType
          .keptcashNumberofcopies || 0;

      const printablePaymentMethods = paymentMethods
        .filter(pm => pm.paymentMethod.printcashupkeptamount)
        .map(pm => pm.payment.searchKey);

      const printablePaymentsCounted = countCashSummary.qtyToKeepSummary.some(
        pm => printablePaymentMethods.includes(pm.searchKey)
      );

      // Print kept cash tickets when the cashup process counts at least one printable payment method
      // printable = having the printcashupkeptamount flag enabled
      if (printablePaymentsCounted) {
        const currentSafeBox = JSON.parse(
          OB.UTIL.localStorage.getItem('currentSafeBox')
        );
        const safeBoxPaymentMethods = paymentMethods
          .filter(pm => pm.paymentMethod.issafebox)
          .map(pm => pm.payment.searchKey);
        for (let i = 0; i < numberOfCopies; i++) {
          OB.POS.hwserver.cleanDisplay();
          OB.POS.hwserver.print(
            this.templateKeptCash,
            {
              cashup: {
                report: cashUpReport,
                summary: countCashSummary
              },
              currentSafeBoxName: currentSafeBox && currentSafeBox.searchKey,
              safeBoxPaymentMethods,
              printablePaymentMethods
            },
            result => {
              if (result && result.exception) {
                OB.OBPOS.showSelectPrinterDialog(
                  retryPrintCashup, // Retry kept cash
                  cancelfunc,
                  cancelfunc,
                  false,
                  'OBPOS_MsgPrintAgainCashUp'
                );
              } else {
                if (callback) {
                  callback();
                }
              }
            }
          );
        }
      } else if (callback) {
        callback();
      }
    };
    const printCashup = () => {
      OB.POS.hwserver.cleanDisplay();
      OB.POS.hwserver.print(
        this.templateCloseCash,
        {
          cashup: {
            closed: closed,
            report: convertedCashUpReport,
            summary: convertedCountCashSummary
          }
        },
        result => {
          if (result && result.exception) {
            OB.OBPOS.showSelectPrinterDialog(
              retryPrintCashup,
              cancelfunc,
              cancelfunc,
              false,
              'OBPOS_MsgPrintAgainCashUp'
            );
          } else {
            if (callback) {
              callback();
            }
          }
        }
      );
    };
    const printProcess = () => {
      printCashup();
      printKeptCash();
    };
    if (OB.MobileApp.model.get('terminal').terminalType.selectprinteralways) {
      OB.OBPOS.showSelectPrintersWindow(
        printProcess,
        cancelfunc,
        cancelfunc,
        false,
        this.isRetry
      );
    } else {
      printProcess();
    }
  };

  // Public object definition
  OB.OBPOSCloseCash = OB.OBPOSCloseCash || {};
  OB.OBPOSCloseCash.Print = OB.OBPOSCloseCash.Print || {};

  OB.OBPOSCloseCash.Print.CloseCash = PrintCloseCash;
})();
