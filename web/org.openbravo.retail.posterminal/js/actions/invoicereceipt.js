/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB*/

(function() {
  const isInvoiceTicketActive = view => {
    const receipt = view.model.get('order');
    const generateInvoice = view.state.readState({
      name: 'receipt.generateInvoice'
    });
    const isQuotation = view.state.readState({
      name: 'receipt.isQuotation'
    });
    return (
      !isQuotation &&
      !generateInvoice &&
      (receipt && receipt.getInvoiceTerms() === 'I')
    );
  };
  const invoiceTicket = view => {
    const receipt = view.model.get('order');
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
  };
  OB.MobileApp.actionsRegistry.register(
    new OB.Actions.CommandAction({
      window: 'retail.pointofsale',
      name: 'issueSimplifiedInvoice',
      permission: 'OBPOS_receipt.invoice',
      properties: {
        i18nContent: 'OBPOS_LblIssueSimplifiedInvoice'
      },
      isActive: function(view) {
        return isInvoiceTicketActive(view);
      },
      command: function(view) {
        invoiceTicket(view);
      }
    })
  );
  OB.MobileApp.actionsRegistry.register(
    new OB.Actions.CommandAction({
      window: 'retail.pointofsale',
      name: 'issueFullInvoice',
      permission: 'OBPOS_receipt.invoice',
      properties: {
        i18nContent: 'OBPOS_LblIssueFullInvoice'
      },
      isActive: function(view) {
        return isInvoiceTicketActive(view);
      },
      command: function(view) {
        invoiceTicket(view);
      }
    })
  );
})();
