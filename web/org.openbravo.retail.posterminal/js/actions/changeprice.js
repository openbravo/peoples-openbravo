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
          OB.App.State.Ticket.setPrice({
            lineIds,
            price,
            reason: options.reason
          })
            .then(() => {
              //TODO: remove this once implemented at ticket level
              receipt.calculateReceipt();

              receipt.trigger('scan');
            })
            .catch(OB.App.View.ActionCanceledUIHandler.handle);
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
          .then(function() {
            var callback = function() {
                if (
                  OB.MobileApp.model.get('priceModificationReasons').length > 0
                ) {
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
              },
              needToLookForServices = false,
              i = 0;

            if (
              !OB.MobileApp.model.hasPermission(
                'OBPOS_ChangeServicePriceNeedApproval',
                true
              )
            ) {
              // Iterate Selected Lines to look for services
              for (i; i < selectedReceiptLines.length; i++) {
                needToLookForServices = true;
                if (
                  selectedReceiptLines[i].get('product').get('productType') ===
                  'I'
                ) {
                  needToLookForServices = false;
                  break;
                }
              }
            }

            if (!needToLookForServices) {
              // Finally price is editable...
              OB.UTIL.Approval.requestApproval(
                view.model,
                'OBPOS_approval.setPrice',
                function(approved, supervisor, approvalType) {
                  if (approved) {
                    var approvals = receipt.get('approvals') || [],
                      approval = _.find(approvals, function(approval) {
                        return (
                          approval.approvalType === 'OBPOS_approval.setPrice'
                        );
                      });
                    if (approval) {
                      approval.approvalType = {
                        approval: 'OBPOS_approval.setPrice',
                        message: 'OBPOS_approval.setPriceMessage',
                        params: [
                          selectedReceiptLine.get('product').get('_identifier'),
                          OB.I18N.formatCurrency(
                            selectedReceiptLine.getGross()
                          ),
                          OB.I18N.formatCurrency(price)
                        ]
                      };
                      receipt.set('approvals', approvals);
                    }
                    callback();
                  }
                }
              );
            } else {
              callback();
            }
          })
          .catch(function() {
            // Ignore. User cancelled action
          });
      }
    })
  );
})();
