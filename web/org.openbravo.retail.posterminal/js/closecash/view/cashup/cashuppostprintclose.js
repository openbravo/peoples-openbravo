/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.PostPrintClose',
  name: 'OB.OBPOSCashUp.UI.PostPrintClose',
  transactionComponents: [
    {
      kind: 'OBPOSCloseCash.UI.ppc_transactionsTable',
      name: 'transactionsTable',
      classes:
        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container2-container1'
    },
    {
      classes:
        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container2-element1'
    },
    {
      classes:
        'obObPosCloseCashUiPostPrintClose-wrapper-components-body-container2-element2'
    }
  ],
  componentInitialization: function() {
    this.model.get('closeCashReport').on('add', closeCashReport => {
      this.$.transactionContainer.$.transactionsTable.$.sales.setValue(
        'netsales',
        closeCashReport.get('netSales')
      );
      this.$.transactionContainer.$.transactionsTable.$.sales.setCollection(
        closeCashReport.get('salesTaxes')
      );
      this.$.transactionContainer.$.transactionsTable.$.sales.setValue(
        'totalsales',
        closeCashReport.get('grossSales')
      );

      this.$.transactionContainer.$.transactionsTable.$.returns.setValue(
        'netreturns',
        closeCashReport.get('netReturns')
      );
      this.$.transactionContainer.$.transactionsTable.$.returns.setCollection(
        closeCashReport.get('returnsTaxes')
      );
      this.$.transactionContainer.$.transactionsTable.$.returns.setValue(
        'totalreturns',
        closeCashReport.get('grossReturns')
      );

      this.$.transactionContainer.$.transactionsTable.$.totaltransactions.setValue(
        'totaltransactionsline',
        closeCashReport.get('totalRetailTransactions')
      );

      if (!OB.POS.modelterminal.get('terminal').ismaster) {
        this.closeCashReportChanged(closeCashReport);
      }
    });
  },
  modelChanger: function() {
    this.$.transactionContainer.$.transactionsTable.$.sales.setValue(
      'netsales',
      this.model.get('netSales')
    );
    this.$.transactionContainer.$.transactionsTable.$.sales.setCollection(
      this.model.get('salesTaxes')
    );
    this.$.transactionContainer.$.transactionsTable.$.sales.setValue(
      'totalsales',
      this.model.get('grossSales')
    );

    this.$.transactionContainer.$.transactionsTable.$.returns.setValue(
      'netreturns',
      this.model.get('netReturns')
    );
    this.$.transactionContainer.$.transactionsTable.$.returns.setCollection(
      this.model.get('returnsTaxes')
    );
    this.$.transactionContainer.$.transactionsTable.$.returns.setValue(
      'totalreturns',
      this.model.get('grossReturns')
    );

    this.$.transactionContainer.$.transactionsTable.$.totaltransactions.setValue(
      'totaltransactionsline',
      this.model.get('totalRetailTransactions')
    );
  },
  stepDisplayer: function() {
    if (
      OB.MobileApp.model.hasPermission('OBPOS_HideCashUpInfoToCashier', true)
    ) {
      this.$.transactionContainer.$.transactionsTable.hide();
    } else {
      this.$.transactionContainer.$.transactionsTable.show();
    }
  }
});
