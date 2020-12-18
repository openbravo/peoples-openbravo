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
      name: 'openReceipt',
      permission: 'OBPOS_retail.menuReceiptSelector',
      properties: {
        i18nContent: 'OBPOS_OpenReceipt'
      },
      command: function(view) {
        var connectedCallback = function() {
          if (OB.MobileApp.model.hasPermission(this.permission)) {
            view.doShowPopup({
              popup: 'modalReceiptSelector',
              args: {
                keepFiltersOnClose: true
              }
            });
          }
        }.bind(this);
        var notConnectedCallback = function() {
          OB.UTIL.showError(
            OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline')
          );
          return;
        }.bind(this);
        if (!OB.MobileApp.model.get('connectedToERP')) {
          OB.UTIL.checkOffLineConnectivity(
            500,
            connectedCallback,
            notConnectedCallback
          );
        } else {
          connectedCallback();
        }
      }
    })
  );
})();
