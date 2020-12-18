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
      name: 'openDrawer',
      permission: 'OBPOS_retail.opendrawerfrommenu',
      properties: {
        i18nContent: 'OBPOS_LblOpenDrawer'
      },
      isActive: function(view) {
        return OB.MobileApp.model.get('hasPaymentsForCashup');
      },
      command: function(view) {
        OB.UTIL.Approval.requestApproval(
          view.model,
          'OBPOS_approval.opendrawer.menu',
          function(approved, supervisor, approvalType) {
            if (approved) {
              OB.POS.hwserver.openDrawer(
                {
                  openFirst: true
                },
                OB.MobileApp.model.get('permissions')
                  .OBPOS_timeAllowedDrawerSales
              );
            }
          }
        );
      }
    })
  );
})();
