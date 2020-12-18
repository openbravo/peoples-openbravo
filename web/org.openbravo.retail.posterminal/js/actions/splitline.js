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
      name: 'splitLine',
      permission: 'OBPOS_ActionButtonSplit',
      properties: {
        i18nContent: 'OBPOS_lblSplit'
      },
      isActive: function(view) {
        var selectedReceiptLine = view.state.readState({
          name: 'selectedReceiptLine'
        });
        var selectedReceiptLines = view.state.readCommandState({
          name: 'selectedReceiptLines'
        });
        var isEditable = view.state.readState({
          name: 'receipt.isEditable'
        });
        var hasServices = view.state.readState({
          name: 'receipt.hasServices'
        });

        var active = isEditable;
        active =
          active &&
          selectedReceiptLine &&
          (!selectedReceiptLines || selectedReceiptLines.length <= 1);
        active =
          active &&
          selectedReceiptLine.get('qty') > 1 &&
          (!selectedReceiptLine.get('remainingQuantity') ||
            selectedReceiptLine.get('remainingQuantity') <
              selectedReceiptLine.get('qty'));
        active =
          active &&
          (!hasServices ||
            (selectedReceiptLine.get('product').get('productType') !== 'S' &&
              !_.find(view.model.get('order').get('lines').models, function(l) {
                return (
                  l.get('relatedLines') &&
                  _.find(l.get('relatedLines'), function(rl) {
                    return rl.orderlineId === selectedReceiptLine.id;
                  }) !== undefined
                );
              })));

        return active;
      },
      command: function(view) {
        var selectedReceiptLine = view.state.readState({
          name: 'selectedReceiptLine'
        });
        view.doShowPopup({
          popup: 'OBPOS_modalSplitLine',
          args: {
            receipt: view.model.get('order'),
            model: selectedReceiptLine
          }
        });
      }
    })
  );
})();
