/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $ */

(function () {

  var PrintCashUp = function () {
      var terminal = OB.MobileApp.model.get('terminal');
      this.templatecashup = new OB.DS.HWResource(terminal.printCashUpTemplate || OB.OBPOSPointOfSale.Print.CashUpTemplate);
      this.cancelOrDismiss = function () {
        OB.POS.navigate('retail.pointofsale');
        OB.MobileApp.view.$.confirmationContainer.setAttribute('openedPopup', '');
      };
      };

  PrintCashUp.prototype.print = function (report, sumary, closed) {
    var me = this;
    OB.POS.hwserver.print(this.templatecashup, {
      cashup: {
        closed: closed,
        report: report,
        summary: sumary
      }
    }, function (result) {
      if (result && result.exception) {
        OB.MobileApp.view.$.confirmationContainer.setAttribute('openedPopup', OB.I18N.getLabel('OBPOS_MsgPrintAgainCashUp'));
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_MsgHardwareServerNotAvailable'), OB.I18N.getLabel('OBPOS_MsgPrintAgainCashUp'), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function () {
            var printCashUp = new OB.OBPOSCashUp.Print.CashUp();
            printCashUp.print(report, sumary, closed);
            return true;
          }
        }, {
          label: OB.I18N.getLabel('OBMOBC_LblCancel'),
          action: function () {
            me.cancelOrDismiss();
            return true;
          }
        }], {
          autoDismiss: false,
          onHideFunction: function (dialog) {
            me.cancelOrDismiss();
          }
        });
      }
    });
  };

  // Public object definition
  OB.OBPOSCashUp = OB.OBPOSCashUp || {};
  OB.OBPOSCashUp.Print = OB.OBPOSCashUp.Print || {};

  OB.OBPOSCashUp.Print.CashUp = PrintCashUp;

}());