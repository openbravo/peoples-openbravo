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
      command: async function(view) {
        try {
          await OB.App.State.Ticket.returnLine(
            OB.UTIL.TicketUtils.addTicketCreationDataToPayload({
              lineIds: view.state
                .readCommandState({
                  name: 'selectedReceiptLines'
                })
                .map(line => line.id)
            })
          );
        } catch (error) {
          OB.App.View.ActionCanceledUIHandler.handle(error);
        }

        OB.MobileApp.model.receipt.trigger('updateView');
        OB.MobileApp.model.receipt.trigger('paintTaxes');

        view.waterfall('onRearrangedEditButtonBar');
      }
    })
  );
})();
