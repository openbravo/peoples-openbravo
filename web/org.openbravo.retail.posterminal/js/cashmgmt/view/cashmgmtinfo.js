/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

// Top-right panel with clock and buttons
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.CashMgmtInfo',
  classes: 'obObposcashmgmtUiCashMgmtInfo',
  components: [
    {
      classes: 'obObposcashmgmtUiCashMgmtInfo-container1',
      components: [
        {
          //clock here
          kind: 'OB.UI.Clock',
          classes: 'obObposcashmgmtUiCashMgmtInfo-container1-obUiClock'
        },
        {
          // process info
          name: 'infoLbl',
          classes: 'obObposcashmgmtUiCashMgmtInfo-container1-obUiClock-infoLbl'
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.infoLbl.setContent(
      OB.I18N.getLabel('OBPOS_LblDepositsWithdrawalsMsg')
    );
  }
});

enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.DoneButton',
  kind: 'OB.UI.RegularButton',
  classes: 'obObposcashmgmtUiDoneButton',
  i18nContent: 'OBPOS_LblDone',
  tap: function() {
    this.owner.owner.model.depsdropstosave.trigger('makeDeposits');
  }
});
