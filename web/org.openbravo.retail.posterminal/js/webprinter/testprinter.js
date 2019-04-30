/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

enyo.kind({
  name: 'OB.UI.MenuTestPrinter',
  kind: 'OB.UI.MenuAction',
  i18nLabel: 'OBPOS_TestPrinter',
  template: //
  '<output>' + //
  '<ticket>' + //
  '  <image>ticket-image.png</image>' + //
  '  <line size="1">' + //
  '  <text align="center" length="42">Sample Receipt</text>' + //
  '  </line>' + //
  '  <line></line>' + //
  '  <line>' + //
  '    <text align="left">Thank you.</text>' + //
  '  </line>' + //
  '  <line></line>' + //
  '  <barcode type="CODE128" position="center">ABCDE12345</barcode>' + //
  '  <qr position="center">ABCDE12345</qr>' + //
  '</ticket>' + //
  '</output>',
  checkReceipt: function (ev) {
    OB.UTIL.confirm(OB.I18N.getLabel('OBPOS_TestPrinter'), OB.I18N.getLabel(ev && ev.exception ? 'OBPOS_TestPrinterError' : 'OBPOS_TestPrinterOK'));
  },
  tap: function () {
    OB.POS.hwserver.print(this.template, {}, this.checkReceipt.bind(this), OB.DS.HWServer.PRINTER);
  }
});