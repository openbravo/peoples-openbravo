/*
 ************************************************************************************
 * Copyright (C) 2012-2021 Openbravo S.L.U.
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
    this.cancelOrDismiss = () => {
      OB.POS.navigate('retail.pointofsale');
      OB.MobileApp.view.$.confirmationContainer.setAttribute('openedPopup', '');
    };
    this.isRetry = false;
  };

  PrintCloseCash.prototype.print = function(report, summary, closed, callback) {
    // Converting model to JSON to support migrated print template.
    const convertedCashUpReport = report.toJSON();
    const convertedCountCashSummary = Object.keys(summary).reduce(
      (prev, curr) => {
        return {
          ...prev,
          [curr]: Object.values(summary[curr]).map(summ => summ.toJSON())
        };
      },
      {}
    );

    // callbacks definition
    const successfunc = () => {
      const printCloseCash = new OB.PrintCloseCash.Print.CloseCash();
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
    const printProcess = () => {
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
              successfunc,
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
