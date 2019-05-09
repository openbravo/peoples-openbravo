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
    name: 'returnReceipt',
    permission: 'OBPOS_receipt.return',
    properties: {
      i18nContent: 'OBPOS_LblReturn'
    },
    command: function (view) {
      return function (inSource, inEvent) {
        view.model.get('order').setDocumentNo(true, false);
        view.showDivText(inSource, {
          permission: this.permission,
          orderType: 1
        });
        if (OB.MobileApp.model.get('lastPaneShown') === 'payment') {
          view.model.get('order').trigger('scan');
        }
        view.waterfall('onRearrangedEditButtonBar', {
          permission: this.permission,
          orderType: 1
        });
      }.bind(this);
    }
  }));

}());