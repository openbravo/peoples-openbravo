/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.MobileApp.actionsRegistry.register(
    new OB.Actions.CommandAction({
      window: 'retail.pointofsale',
      name: 'convertQuotation',
      permission: 'OBPOS_receipt.createorderfromquotation',
      properties: {
        i18nContent: 'OBPOS_CreateOrderFromQuotation'
      },
      isActive: function(view) {
        var currentView = view.state.readState({
          name: 'window.currentView'
        }).name;
        var hasBeenPaid = view.state.readState({
          name: 'receipt.hasBeenPaid'
        });
        var isQuotation = view.state.readState({
          name: 'receipt.isQuotation'
        });

        var active = currentView === 'order';
        active = active && isQuotation && hasBeenPaid === 'Y';
        return active;
      },
      command: function(view) {
        view.doShowPopup({
          popup: 'modalCreateOrderFromQuotation'
        });
      }
    })
  );
})();
