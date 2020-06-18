/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
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
      name: 'payOpenReceipts',
      permission: 'OBPOS_retail.multiorders',
      properties: {
        i18nContent: 'OBPOS_LblPayOpenTickets'
      },
      isActive: function(view) {
        return OB.MobileApp.model.get('payments').length > 0;
      },
      command: function(view) {
        if (!OB.MobileApp.model.get('connectedToERP')) {
          OB.UTIL.showError(
            OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline')
          );
          return;
        }
        if (OB.MobileApp.model.hasPermission('OBPOS_retail.multiorders')) {
          OB.MobileApp.model.receipt.trigger('updateView');
          view.multiOrders();
        }
      }
    })
  );
})();
