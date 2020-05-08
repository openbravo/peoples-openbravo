/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _ */

(function() {
  OB.MobileApp.actionsRegistry.register(
    new OB.Actions.CommandAction({
      window: 'retail.pointofsale',
      name: 'returnLine',
      permission: 'OBPOS_ReturnLine',
      properties: {
        i18nContent: 'OBPOS_LblReturnLine'
      },
      isActive: function(view) {
        var selectedReceiptLine = view.state.readState({
          name: 'selectedReceiptLine'
        });
        var selectedReceiptLines = view.state.readState({
          name: 'selectedReceiptLines'
        });
        var isPaid = view.state.readState({
          name: 'receipt.isPaid'
        });
        var isLayaway = view.state.readState({
          name: 'receipt.isLayaway'
        });
        var isQuotation = view.state.readState({
          name: 'receipt.isQuotation'
        });
        var orderType = view.state.readState({
          name: 'receipt.orderType'
        });

        var active = !isPaid && !isLayaway && !isQuotation;
        active =
          active &&
          orderType !== 1 &&
          (orderType !== 2 ||
            OB.MobileApp.model.hasPermission(
              'OBPOS_AllowLayawaysNegativeLines',
              true
            ));
        active = active && selectedReceiptLine;
        active =
          active &&
          selectedReceiptLines.length ===
            Math.abs(
              _.reduce(
                selectedReceiptLines,
                function(memo, l) {
                  return memo + Math.sign(l.get('qty'));
                },
                0
              )
            );
        active = active && selectedReceiptLine.get('isEditable');
        active =
          active &&
          selectedReceiptLines.every(function(l) {
            return l.get('isEditable');
          });

        return active;
      },
      command: function(view) {
        var receipt = view.model.get('order');
        var selectedReceiptLines = view.state.readCommandState({
          name: 'selectedReceiptLines'
        });

        if (
          receipt.get('replacedorder') &&
          _.find(selectedReceiptLines, function(l) {
            l.get('remainingQuantity');
          })
        ) {
          OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBMOBC_Error'),
            OB.I18N.getLabel('OBPOS_CancelReplaceReturnLines')
          );
          return;
        }

        receipt.checkReturnableProducts(
          selectedReceiptLines,
          view.model,
          function(success) {
            if (!success) {
              return;
            }
            //The value of qty need to be negate because we want to change it
            if (
              receipt.validateAllowSalesWithReturn(
                -1,
                false,
                selectedReceiptLines
              )
            ) {
              view.waterfall('onRearrangedEditButtonBar');
              return;
            }
            receipt.set('undo', null);
            receipt.set('multipleUndo', true);
            receipt.set('preventServicesUpdate', true);
            _.each(selectedReceiptLines, function(line) {
              if (!line.get('relatedLines')) {
                view.returnLine(view, {
                  line: line
                });
              }
            });
            receipt.unset('preventServicesUpdate');
            receipt.get('lines').trigger('updateRelations');
            receipt.set('multipleUndo', null);
            view.waterfall('onRearrangedEditButtonBar');
          }
        );
      }
    })
  );
})();
