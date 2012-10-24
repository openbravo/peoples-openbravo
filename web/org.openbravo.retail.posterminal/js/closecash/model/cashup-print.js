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
      this.templatecashup = new OB.DS.HWResource(OB.OBPOSCashUp.Print.CashUpTemplate);
      };

  PrintCashUp.prototype.print = function (report, sumary) {
    OB.POS.hwserver.print(this.templatecashup, {
      cashup: {
        report: report,
        summary: sumary
      }
    });
  };

  // Public object definition
  OB.OBPOSCashUp = OB.OBPOSCashUp || {};
  OB.OBPOSCashUp.Print = OB.OBPOSCashUp.Print || {};

  OB.OBPOSCashUp.Print.CashUp = PrintCashUp;
  OB.OBPOSCashUp.Print.CashUpTemplate = 'res/printcashup.xml';

}());