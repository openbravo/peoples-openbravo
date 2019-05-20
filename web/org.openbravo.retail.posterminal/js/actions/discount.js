/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */

(function () {

  OB.MobileApp.actionsRegistry.register(
  new OB.Actions.CommandAction({
    window: 'retail.pointofsale',
    name: 'discount',
    permission: 'OBPOS_order.discount',
    properties: {
      i18nContent: 'OBMOBC_KbDiscount'
    },
    command: function (view) {

      var receipt = view.model.get('order');
      var selectedReceiptLine = view.state.readCommandState({
        name: 'selectedReceiptLine'
      });

      if (receipt.get('isEditable') === false) {
        this.doShowPopup({
          popup: 'modalNotEditableOrder'
        });
        return;
      }

      if (!selectedReceiptLine) {
        return false;
      }

      if (selectedReceiptLine.get('product').get('isEditableQty') === false) {
        this.doShowPopup({
          popup: 'modalNotEditableLine'
        });
        return;
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
  }));

}());