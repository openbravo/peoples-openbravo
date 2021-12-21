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
          (active &&
            (OB.Discounts.Pos.manualRuleImpls &&
              OB.Discounts.Pos.manualRuleImpls.length > 0)) ||
          !OB.MobileApp.model.get('permissions')[
            'OBPOS_retail.discountkeyboard'
          ];
        return active;
      },
      command: function(view) {
        var keyboard =
          view.$.multiColumn.$.rightPanel.$.rightBottomPanel.$.keyboard;
        var receipt = view.model.get('order');
        var selectedReceiptLine = view.state.readCommandState({
          name: 'selectedReceiptLine'
        });
        if (
          !OB.MobileApp.model.get('permissions')[
            'OBPOS_retail.discountkeyboard'
          ]
        ) {
          if (!keyboard.validateReceipt(keyboard, true)) {
            return true;
          }
          keyboard.receipt.set('undo', null);
          keyboard.receipt.set('multipleUndo', true);
          const percentage = OB.I18N.parseNumber(keyboard.getString());
          if (
            OB.DEC.compare(percentage) > 0 &&
            OB.DEC.compare(OB.DEC.sub(percentage, OB.DEC.number(100))) <= 0
          ) {
            keyboard.selectedModels.forEach(line => {
              const lineIds = [line.get('id')];
              const price = OB.DEC.toNumber(
                new BigDecimal(String(line.get('price')))
                  .multiply(
                    new BigDecimal(
                      String(OB.DEC.sub(OB.DEC.number(100), percentage))
                    )
                  )
                  .divide(
                    new BigDecimal('100'),
                    OB.DEC.getScale(),
                    OB.DEC.getRoundingMode()
                  )
              );

              OB.App.State.Ticket.setLinePrice({ lineIds, price })
                .then(() => {
                  OB.UTIL.handlePriceRuleBasedServices(keyboard.receipt);
                  OB.UTIL.TicketUtils.printLinesOfTicket(
                    keyboard.receipt,
                    lineIds
                  );
                })
                .catch(OB.App.View.ActionCanceledUIHandler.handle);
            });
          }
          keyboard.receipt.set('multipleUndo', null);
          keyboard.lastStatus = '';
          keyboard.setStatus('');
          keyboard.resetString();
        } else {
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
      }
    })
  );
})();
