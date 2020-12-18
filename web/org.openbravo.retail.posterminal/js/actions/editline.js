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
      name: 'editLine',
      permission: 'OBPOS_ActionButtonDescription',
      properties: {
        i18nContent: 'OBPOS_LblDescription'
      },
      isActive: function(view) {
        var selectedReceiptLine = view.state.readState({
          name: 'selectedReceiptLine'
        });
        var selectedReceiptLines = view.state.readCommandState({
          name: 'selectedReceiptLines'
        });
        return (
          selectedReceiptLine &&
          (!selectedReceiptLines || selectedReceiptLines.length <= 1)
        );
      }
    })
  );
})();
