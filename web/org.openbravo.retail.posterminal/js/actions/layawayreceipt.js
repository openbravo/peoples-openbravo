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
      name: 'layawayReceipt',
      permission: 'OBPOS_receipt.layawayReceipt',
      properties: {
        i18nContent: 'OBPOS_LblLayawayReceipt'
      },
      isActive: function(view) {
        var isEditable = view.state.readState({
          name: 'receipt.isEditable'
        });
        var orderType = view.state.readState({
          name: 'receipt.orderType'
        });
        var isQuotation = view.state.readState({
          name: 'receipt.isQuotation'
        });
        var currentView = view.state.readState({
          name: 'window.currentView'
        }).name;

        return (
          currentView === 'order' &&
          isEditable &&
          !isQuotation && //
          (orderType !== 1 ||
            OB.MobileApp.model.hasPermission(
              'OBPOS_AllowLayawaysNegativeLines',
              true
            )) &&
          orderType !== 2
        );
      },
      command: function(view) {
        var negativeLines, deliveredLines;
        if (OB.UTIL.isNullOrUndefined(view.model.get('order').get('bp'))) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_layawaysOrderWithNotBP'));
          return true;
        }
        if (
          !OB.MobileApp.model.hasPermission(
            'OBPOS_AllowLayawaysNegativeLines',
            true
          )
        ) {
          negativeLines = _.find(
            view.model.get('order').get('lines').models,
            function(line) {
              return line.get('qty') < 0;
            }
          );
          if (negativeLines) {
            OB.UTIL.showWarning(
              OB.I18N.getLabel('OBPOS_layawaysOrdersWithReturnsNotAllowed')
            );
            return true;
          }
        }
        if (view.model.get('order').get('doCancelAndReplace')) {
          deliveredLines = _.find(
            view.model.get('order').get('lines').models,
            function(line) {
              return (
                line.get('deliveredQuantity') &&
                OB.DEC.compare(line.get('deliveredQuantity')) === 1
              );
            }
          );
          if (deliveredLines) {
            OB.UTIL.showWarning(
              OB.I18N.getLabel('OBPOS_LayawaysOrdersWithDeliveries')
            );
            return true;
          }
        }
        OB.UTIL.HookManager.executeHooks(
          'OBPOS_LayawayReceipt',
          {
            context: this
          },
          function(args) {
            if (args && args.cancelOperation && args.cancelOperation === true) {
              return;
            }
            view.showDivText(this, {
              permission: this.permission,
              orderType: 2
            });
            view.waterfall('onRearrangedEditButtonBar', {
              permission: this.permission,
              orderType: 2
            });
          }.bind(this)
        );
      }
    })
  );
})();
