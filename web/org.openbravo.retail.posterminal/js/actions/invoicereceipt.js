/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB*/

(function() {
  OB.MobileApp.actionsRegistry.register(
    new OB.Actions.CommandAction({
      window: 'retail.pointofsale',
      name: 'invoiceReceipt',
      permission: 'OBPOS_receipt.invoice',
      properties: {
        i18nContent: 'OBPOS_LblInvoice'
      },
      isActive: function(view) {
        var receipt = view.model.get('order');
        var generateInvoice = view.state.readState({
          name: 'receipt.generateInvoice'
        });
        var isQuotation = view.state.readState({
          name: 'receipt.isQuotation'
        });

        return (
          !isQuotation &&
          !generateInvoice &&
          (receipt && receipt.getInvoiceTerms() === 'I')
        );
      },
      command: function(view) {
        var receipt = view.model.get('order');

        if (!OB.MobileApp.model.hasPermission('OBPOS_receipt.invoice')) {
          view.cancelReceiptToInvoice();
        } else if (
          OB.MobileApp.model.hasPermission(
            'OBPOS_retail.restricttaxidinvoice',
            true
          ) &&
          !receipt.get('bp').get('taxID')
        ) {
          if (OB.MobileApp.model.get('terminal').terminalType.generateInvoice) {
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_BP_No_Taxid'));
          } else {
            OB.UTIL.showWarning(OB.I18N.getLabel('OBPOS_BP_No_Taxid'));
          }
          view.cancelReceiptToInvoice();
        } else {
          view.receiptToInvoice();
        }
      }
    })
  );
})();
