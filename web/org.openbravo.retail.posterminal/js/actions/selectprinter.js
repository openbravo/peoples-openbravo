/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

(function() {
  OB.MobileApp.actionsRegistry.register(
    new OB.Actions.CommandAction({
      window: 'retail.pointofsale',
      name: 'selectPrinter',
      permission: 'OBPOS_retail.selectprinter',
      properties: {
        i18nContent: 'OBPOS_MenuSelectPrinter'
      },
      isActive: function(view) {
        var currentView = view.state.readState({
          name: 'window.currentView'
        }).name;
        var active = currentView === 'order';
        active = active && OB.MobileApp.model.hasPermission(this.permission);
        active =
          active &&
          !OB.MobileApp.model.get('terminal').terminalType.selectprinteralways;
        active =
          active &&
          _.any(OB.POS.modelterminal.get('hardwareURL'), function(printer) {
            return printer.hasReceiptPrinter;
          });
        return active;
      },
      command: function(view) {
        view.waterfall('onModalSelectPrinters');
      }
    })
  );
})();
