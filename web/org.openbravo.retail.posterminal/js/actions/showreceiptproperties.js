/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Promise */

(function () {

  OB.MobileApp.actionsRegistry.register(
  new OB.Actions.ViewMethodAction({
    window: 'retail.pointofsale',
    name: 'showModalReceiptProperties',
    permission: 'OBPOS_receipt.properties',
    properties: {
      i18nContent: 'OBPOS_LblProperties'
    }
  }));

  OB.MobileApp.actionsRegistry.register(
    new OB.Actions.CommandAction({
      window: 'retail.pointofsale',
      name: 'changePrice',
      permission: 'OBPOS_order.changePrice',
      properties: {
        i18nContent: 'OBMOBC_KbPrice'
      },
      command: function (view) {
        return function (inSender, inEvent) {
          var editboxvalue = view.state.getState('editbox');

          if (!editboxvalue) {
            return;
          }
          var selectedReceiptLine = view.state.getState('selectedReceiptLine');
          var selectedReceiptLines = view.state.getState('selectedReceiptLines');  
          var price = OB.I18N.parseNumber(editboxvalue);
          var receipt = view.model.get('order');  
          var i;  
          var setPrices = function () {
            receipt.setPrices(selectedReceiptLines, price);
            receipt.trigger('scan');
            };
          var validatePrice = function () {  
              if (OB.MobileApp.model.hasPermission('OBPOS_maxPriceUsingKeyboard', true) && price >= OB.I18N.parseNumber(OB.MobileApp.model.hasPermission('OBPOS_maxPriceUsingKeyboard', true))) {
                return OB.UTIL.question(OB.I18N.getLabel('OBPOS_maxPriceUsingKeyboardHeader'), OB.I18N.getLabel('OBPOS_maxPriceUsingKeyboardBody', [price]));
              } else {
                return Promise.resolve();
              }
          };
          if (receipt.get('isEditable') === false) {
            view.doShowPopup({
              popup: 'modalNotEditableOrder'
            });
            return;
          }
          if (!selectedReceiptLine) {
            return;
          }
          for (i = 0; i < selectedReceiptLines.length; i++) {
            if (!selectedReceiptLines[i].get('product').get('obposEditablePrice')) {
              view.doShowPopup({
                popup: 'modalNotEditableLine'
              });              
              return;
            }
          }          
          if (selectedReceiptLine.get('product').get('isEditablePrice') === false) {
            view.doShowPopup({
              popup: 'modalNotEditableLine'
            });
            return;
          }   
          validatePrice().then(function () {
            // Finally price is editable...
            OB.UTIL.Approval.requestApproval(
              view.model, 'OBPOS_approval.setPrice', function (approved, supervisor, approvalType) {
                if (approved) {
                  var approvals = receipt.get('approvals') || [];
                  approvals.push({
                    approvalType: {
                      approval: 'OBPOS_approval.setPrice',
                      message: 'OBPOS_approval.setPriceMessage',
                      params: [selectedReceiptLine.get('product').get('_identifier'), OB.I18N.formatCurrency(selectedReceiptLine.getGross()), OB.I18N.formatCurrency(price)]
                    },
                    userContact: supervisor.get('id'),
                    created: (new Date()).getTime()
                  });
                  receipt.set('approvals', approvals);        
                  if (OB.MobileApp.model.get('priceModificationReasons').length > 0) {
                    view.doShowPopup({
                      popup: 'modalPriceModification',
                      args: {
                        callback: setPrices,
                        selectedModels: selectedReceiptLines,
                        receipt: receipt,
                        line: selectedReceiptLine
                      }
                    });
                  } else {
                    setPrices();
                  }
                }
              });
          })['catch'](function () {
            // Ignore. User cancelled action
          });
        };
      }             
    }));

}());