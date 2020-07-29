/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Promise */

(function() {
  OB.MobileApp.actionsRegistry.register(
    new OB.Actions.CommandAction({
      window: 'retail.pointofsale',
      name: 'changePrice',
      permission: 'OBPOS_order.changePrice',
      properties: {
        i18nContent: 'OBMOBC_KbPrice'
      },
      isActive: function(view) {
        var isEditable = view.state.readCommandState({
          name: 'receipt.isEditable'
        });
        var selectedReceiptLine = view.state.readCommandState({
          name: 'selectedReceiptLine'
        });
        var selectedReceiptLines = view.state.readCommandState({
          name: 'selectedReceiptLines'
        });

        var active = isEditable;
        active =
          active &&
          selectedReceiptLine &&
          selectedReceiptLine.get('product').get('obposEditablePrice') &&
          selectedReceiptLine.get('product').get('isEditablePrice') !== false;
        active =
          active &&
          selectedReceiptLines &&
          selectedReceiptLines.every(function(l) {
            return (
              l.get('product').get('obposEditablePrice') &&
              l.get('product').get('isEditablePrice') !== false
            );
          });
        return active;
      },
      command: function(view) {
        var editboxvalue = view.state.readCommandState({
          name: 'editbox'
        });

        if (!editboxvalue) {
          return;
        }

        var selectedReceiptLine = view.state.readCommandState({
          name: 'selectedReceiptLine'
        });
        var selectedReceiptLines = view.state.readCommandState({
          name: 'selectedReceiptLines'
        });
        var price = OB.I18N.parseNumber(editboxvalue);
        var receipt = view.model.get('order');
        const lineIds = selectedReceiptLines.map(l => l.id);
        var setPrices = function(options = {}) {
          // enable flag 'propagatingBackboneToState' to skip calculateReceipt execution
          // otherwise an infinite loop causes the action to be executed repeateadly
          receipt.propagatingBackboneToState = true;
          OB.App.State.Ticket.setLinePrice({
            lineIds,
            price,
            reason: options.reason
          })
            .then(() => {
              receipt.trigger('scan');
            })
            .catch(OB.App.View.ActionCanceledUIHandler.handle)
            .finally(() => delete receipt.propagatingBackboneToState);
        };
        var validatePrice = function() {
          if (
            OB.MobileApp.model.hasPermission(
              'OBPOS_maxPriceUsingKeyboard',
              true
            ) &&
            price >=
              OB.I18N.parseNumber(
                OB.MobileApp.model.hasPermission(
                  'OBPOS_maxPriceUsingKeyboard',
                  true
                )
              )
          ) {
            return OB.DIALOGS.confirm({
              title: OB.I18N.getLabel('OBPOS_maxPriceUsingKeyboardHeader'),
              message: OB.I18N.getLabel('OBPOS_maxPriceUsingKeyboardBody', [
                price
              ])
            });
          } else {
            return Promise.resolve();
          }
        };

        if (!selectedReceiptLine) {
          return;
        }

        validatePrice()
          .then(() => {
            if (OB.MobileApp.model.get('priceModificationReasons').length > 0) {
              view.doShowPopup({
                popup: 'modalPriceModification',
                args: {
                  callback: setPrices,
                  lineIds
                }
              });
            } else {
              setPrices();
            }
          })
          .catch(function() {
            // Ignore. User cancelled action
          });
      }
    })
  );
})();
