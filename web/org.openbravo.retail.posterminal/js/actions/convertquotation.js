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
    name: 'convertQuotation',
    permission: 'OBPOS_receipt.createorderfromquotation',
    properties: {
      i18nContent: 'OBPOS_CreateOrderFromQuotation'
    },
    command: function (view) {
      view.doShowPopup({
        popup: 'modalCreateOrderFromQuotation'
      });
    }
  }));

}());