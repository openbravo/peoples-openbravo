/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $ */

(function () {

  var PrintCashMgmt = function () {
      var terminal = OB.MobileApp.model.get('terminal');
      this.templatecashmgmt = new OB.DS.HWResource(terminal.printCashMgmTemplate || OB.OBPOSPointOfSale.Print.CashMgmTemplate);

      this.cashMgmtSuccess = function () {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_LblDone'), OB.I18N.getLabel('OBPOS_FinishCashMgmtDialog'), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function () {
            OB.POS.navigate('retail.pointofsale');
          }
        }], {
          autoDismiss: false,
          onHideFunction: function () {
            OB.POS.navigate('retail.pointofsale');
          }
        });
      };
      };

  PrintCashMgmt.prototype.print = function (depsdropstosave) {

    OB.POS.hwserver.cleanDisplay();
    OB.POS.hwserver.print(this.templatecashmgmt, {
      cashmgmt: depsdropstosave
    }, function (result) {
      if (result && result.exception) {
        // callbacks definition
        var successfunc = function () {
            var printCashMgmt = new OB.OBPOSCashMgmt.Print.CashMgmt();
            printCashMgmt.print(depsdropstosave);
            return true;
            };
        var cancelfunc = function () {
            var printCashMgmt = new OB.OBPOSCashMgmt.Print.CashMgmt();
            printCashMgmt.cashMgmtSuccess();
            };
        var hidefunc = function () {
            OB.POS.navigate('retail.pointofsale');
            return true;
            };
        OB.OBPOS.showSelectPrinterDialog(successfunc, hidefunc, cancelfunc, false, 'OBPOS_MsgPrintAgainCashMgmt');
      } else {
        var printCashMgmt = new OB.OBPOSCashMgmt.Print.CashMgmt();
        printCashMgmt.cashMgmtSuccess();
      }
    });
  };

  // Public object definition
  OB.OBPOSCashMgmt = OB.OBPOSCashMgmt || {};
  OB.OBPOSCashMgmt.Print = OB.OBPOSCashMgmt.Print || {};

  OB.OBPOSCashMgmt.Print.CashMgmt = PrintCashMgmt;
  OB.OBPOSCashMgmt.Print.CashMgmtTemplate = PrintCashMgmt;

}());