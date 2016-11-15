/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $, _ */

(function () {

  var PrintCashUp = function () {
      var terminal = OB.MobileApp.model.get('terminal');
      this.templatecashup = new OB.DS.HWResource(terminal.printCashUpTemplate || OB.OBPOSPointOfSale.Print.CashUpTemplate);
      this.cancelOrDismiss = function () {
        OB.POS.navigate('retail.pointofsale');
        OB.MobileApp.view.$.confirmationContainer.setAttribute('openedPopup', '');
      };
      var me = this;
      this.cashUpSuccess = function () {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_LblGoodjob'), OB.I18N.getLabel('OBPOS_FinishCloseDialog'), [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function () {
            me.cancelOrDismiss();
            return true;
          }
        }], {
          autoDismiss: false,
          onHideFunction: function () {
            me.cancelOrDismiss();
          }
        });
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
        // callbacks definition
        var successfunc = function () {
            var printCashUp = new OB.OBPOSCashUp.Print.CashUp();
            printCashUp.print(report, sumary, closed);
            return true;
            };
        var cancelfunc = function () {
            if (closed) {
              me.cashUpSuccess();
            } else {
              OB.POS.navigate('retail.pointofsale');
              OB.MobileApp.view.$.confirmationContainer.setAttribute('openedPopup', '');
            }
            return true;
            };
        var hidefunc = function () {
            if (closed) {
              me.cashUpSuccess();
            } else {
              OB.POS.navigate('retail.pointofsale');
              OB.MobileApp.view.$.confirmationContainer.setAttribute('openedPopup', '');
            }
            return true;
            };
        OB.OBPOS.showSelectPrinterDialog(successfunc, hidefunc, hidefunc, false, 'OBPOS_MsgPrintAgainCashUp');
      } else {
        if (OB.MobileApp.view.$.confirmationContainer.getCurrentPopup().header !== OB.I18N.getLabel('OBPOS_LblGoodjob')) {
          // Only display the good job message if there are no components displayed
          me.cashUpSuccess();
        }
      }
    });
  };

  // Public object definition
  OB.OBPOSCashUp = OB.OBPOSCashUp || {};
  OB.OBPOSCashUp.Print = OB.OBPOSCashUp.Print || {};

  OB.OBPOSCashUp.Print.CashUp = PrintCashUp;

}());