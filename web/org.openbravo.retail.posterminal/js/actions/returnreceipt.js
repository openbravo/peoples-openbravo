/*
 ************************************************************************************
 * Copyright (C) 2019-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.MobileApp.actionsRegistry.register(
    new OB.Actions.CommandAction({
      window: 'retail.pointofsale',
      name: 'returnReceipt',
      permission: 'OBPOS_receipt.return',
      properties: {
        i18nContent: 'OBPOS_LblReturn'
      },
      isActive: function(view) {
        var currentView = view.state.readState({
          name: 'window.currentView'
        }).name;
        var isQuotation = view.state.readState({
          name: 'receipt.isQuotation'
        });
        var orderType = view.state.readState({
          name: 'receipt.orderType'
        });
        var isEditable = view.state.readState({
          name: 'receipt.isEditable'
        });

        var active = currentView === 'order';
        active =
          active &&
          !isQuotation &&
          isEditable &&
          (orderType !== 2 ||
            OB.MobileApp.model.hasPermission(
              'OBPOS_AllowLayawaysNegativeLines',
              true
            ));
        // No negative lines
        active =
          active &&
          !view.model
            .get('order')
            .get('lines')
            .find(function(line) {
              return line.get('qty') < 0;
            });
        // Allows payment on credit or there is at least one refundable payment method
        active =
          active &&
          (OB.MobileApp.model.get('terminal').allowpayoncredit ||
            OB.MobileApp.model.get('payments').find(function(payment) {
              return payment.paymentMethod.refundable;
            }));
        return active;
      },
      command: async function(view) {
        try {
          await OB.App.State.Ticket.returnTicket(
            OB.UTIL.TicketUtils.addTicketCreationDataToPayload()
          );
        } catch (error) {
          OB.App.View.ActionCanceledUIHandler.handle(error);
        }

        OB.MobileApp.model.receipt.trigger('updateView');
        OB.MobileApp.model.receipt.trigger('paintTaxes');

        if (OB.MobileApp.model.get('lastPaneShown') === 'payment') {
          OB.MobileApp.model.receipt.trigger('scan');
        }

        view.waterfall('onRearrangedEditButtonBar', {
          permission: this.permission,
          orderType: 1
        });
      }
    })
  );
})();
