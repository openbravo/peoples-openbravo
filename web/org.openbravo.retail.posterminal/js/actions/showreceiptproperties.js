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
    new OB.Actions.ViewMethodAction({
      window: 'retail.pointofsale',
      name: 'showModalReceiptProperties',
      permission: 'OBPOS_receipt.properties',
      properties: {
        i18nContent: 'OBPOS_LblProperties'
      },
      isActive: function(view) {
        return (
          view.state.readState({
            name: 'window.currentView'
          }).name === 'order' &&
          view.state.readState({
            name: 'receipt.isEditable'
          })
        );
      }
    })
  );
})();
