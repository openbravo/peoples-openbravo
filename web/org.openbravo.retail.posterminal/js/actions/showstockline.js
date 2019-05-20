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
    name: 'showStockLine',
    permission: 'OBPOS_ActionButtonCheckStock',
    properties: {
      i18nContent: 'OBPOS_checkStock'
    },
    command: function (view) {
      var selectedReceiptLine = view.state.readCommandState({
        name: 'selectedReceiptLine'
      });
      var product = selectedReceiptLine.get('product');
      var warehouse = selectedReceiptLine.get('warehouse');
      //show always or just when the product has been set to show stock screen?
      if (product.get('productType') === 'I' && !product.get('ispack') && OB.MobileApp.model.get('connectedToERP')) {
        view.showLeftSubWindow(this, {
          leftSubWindow: OB.OBPOSPointOfSale.UICustomization.stockLeftSubWindow,
          line: selectedReceiptLine,
          product: product,
          warehouse: warehouse
        });
      }
    }
  }));

}());