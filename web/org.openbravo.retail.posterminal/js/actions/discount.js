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
      name: 'discount',
      permission: 'OBPOS_order.discount',
      properties: {
        i18nContent: 'OBMOBC_KbDiscount'
      },
      isActive: function(view) {
        var currentView = view.state.readState({
          name: 'window.currentView'
        }).name;
        var isEditable = view.state.readState({
          name: 'receipt.isEditable'
        });

        var active = isEditable && currentView === 'order';
        active = active && view.model.get('order').get('lines').length > 0;
        active =
          active &&
          OB.Discounts.Pos.manualRuleImpls &&
          OB.Discounts.Pos.manualRuleImpls.length > 0;
        return active;
      },
      command: function(view) {
        var receipt = view.model.get('order');
        var selectedReceiptLine = view.state.readCommandState({
          name: 'selectedReceiptLine'
        });

        if (receipt.get('isEditable') === false) {
          view.doShowPopup({
            popup: 'modalNotEditableOrder'
          });
          return;
        }

        if (!selectedReceiptLine) {
          return false;
        }

        view.discountsMode(view, {
          tabPanel: 'edit',
          keyboard: 'toolbardiscounts',
          edit: false,
          options: {
            discounts: true
          }
        });
      }
    })
  );
})();
