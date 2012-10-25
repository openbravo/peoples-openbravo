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

  var PrintCashMgmt = function () {
      this.templatecashmgmt = new OB.DS.HWResource(OB.OBPOSCashMgmt.Print.CashMgmtTemplate);
      };

  PrintCashMgmt.prototype.print = function (depsdropstosend) {

    OB.POS.hwserver.print(this.templatecashmgmt, {
      cashmgmt: depsdropstosend
    });
  };

  // Public object definition
  OB.OBPOSCashMgmt = OB.OBPOSCashMgmt || {};
  OB.OBPOSCashMgmt.Print = OB.OBPOSCashMgmt.Print || {};

  OB.OBPOSCashMgmt.Print.CashMgmt = PrintCashMgmt;
  OB.OBPOSCashMgmt.Print.CashMgmtTemplate = 'res/printcashmgmt.xml';

}());