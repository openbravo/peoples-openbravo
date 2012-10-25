/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo */

// Top-right panel with clock and buttons
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.CashMgmtInfo',
  components: [{
    style: 'position: relative; background: #363636; color: white; height: 200px; margin: 5px; padding: 5px',
    components: [{ //clock here
      kind: 'OB.UI.Clock',
      classes: 'pos-clock'
    }, {
      // process info
      style: 'padding: 10px; float: left; width: 320px; line-height: 23px;',
      content: OB.I18N.getLabel('OBPOS_LblDepositsWithdrawalsMsg')
    }, {
      style: 'padding: 5px; float: right;',
      components: [{
        kind: 'OB.UI.CancelButton'
      }]
    }, {
      // done button
      style: 'width: 100%; float: left;',
      attributes: {
        align: 'center'
      },
      components: [{
        kind: 'OB.OBPOSCashMgmt.UI.DoneButton'
      }]
    }]
  }]
});

enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.DoneButton',
  kind: 'OB.UI.RegularButton',
  classes: 'btnlink-white btnlink-fontgray',
  style: 'min-width: 115px;',
  content: OB.I18N.getLabel('OBPOS_LblDone'),
  tap: function () {
    this.owner.owner.model.depsdropstosend.trigger('makeDeposits');
  }
});