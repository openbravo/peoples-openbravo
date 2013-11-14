/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, $ */

enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.LeftToolbarImpl',
  kind: 'OB.UI.MultiColumn.Toolbar',
  buttons: [{
    kind: 'OB.UI.ToolbarButton',
    name: 'btnCancel',
    disabled: false,
    i18nLabel: 'OBMOBC_LblCancel',
    stepCount: 0,
    span: 6,
    tap: function () {
      OB.POS.navigate('retail.pointofsale');
    }
  }, {
    kind: 'OB.UI.ToolbarButton',
    name: 'btnCancel',
    disabled: false,
    i18nLabel: 'OBPOS_LblDone',
    stepCount: 0,
    span: 6,
    init: function (model) {
      this.model = model;
    },
    tap: function () {
      this.model.depsdropstosave.trigger('makeDeposits');
    }
  }]
});

enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.RightToolbarImpl',
  kind: 'OB.UI.MultiColumn.Toolbar',
  buttons: [{
    kind: 'OB.UI.ToolbarButton',
    name: 'btnCashMgmt',
    span: 12,
    disabled: true,
    i18nLabel: 'OBPOS_LblCashManagement'
  }]
});

// Cash Management main window view
enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.CashManagement',
  kind: 'OB.UI.WindowView',
  windowmodel: OB.OBPOSCashMgmt.Model.CashManagement,
  tag: 'section',
  events: {
    onShowPopup: ''
  },
  components: [{
    kind: 'OB.UI.MultiColumn',
    name: 'cashupMultiColumn',
    leftToolbar: {
      kind: 'OB.OBPOSCashMgmt.UI.LeftToolbarImpl',
      name: 'leftToolbar',
      showMenu: false,
      showWindowsMenu: false
    },
    rightToolbar: {
      kind: 'OB.OBPOSCashMgmt.UI.RightToolbarImpl',
      name: 'rightToolbar',
      showMenu: false,
      showWindowsMenu: false
    },
    leftPanel: {
      name: 'cashmgmtLeftPanel',
      components: [{
        classes: 'row',
        components: [
        // 1st column: list of deposits/drops done or in process
        {
          classes: 'span12',
          components: [{
            kind: 'OB.OBPOSCashMgmt.UI.ListDepositsDrops'
          }]
        }]
      }]
    },
    rightPanel: {
      name: 'cashmgmtRightPanel',
      components: [

      //2nd column
      {
        classes: 'span12',
        components: [{
          kind: 'OB.OBPOSCashMgmt.UI.CashMgmtInfo'
        }, {
          kind: 'OB.OBPOSCashMgmt.UI.CashMgmtKeyboard'
        }]
      }]
    }
  }, //hidden stuff
  {
    components: [{
      kind: 'OB.OBPOSCashMgmt.UI.ModalDepositEvents',
      i18nHeader: 'OBPOS_SelectDepositDestinations',
      name: 'modaldepositevents',
      type: 'cashMgmtDepositEvents'
    }, {
      kind: 'OB.OBPOSCashMgmt.UI.ModalDepositEvents',
      i18nHeader: 'OBPOS_SelectDropDestinations',
      name: 'modaldropevents',
      type: 'cashMgmtDropEvents'
    }]
  }],


  init: function () {
    this.inherited(arguments);

    // cashMgmtDepositEvents or cashMgmtDropEvents Collection is shown by OB.UI.Table, when selecting an option 'click' event 
    // is triggered, propagating this UI event to model here
    this.model.get('cashMgmtDepositEvents').on('click', function (model) {
      this.model.depsdropstosave.trigger('paymentDone', model, this.currentPayment);
      delete this.currentPayment;
    }, this);

    this.model.get('cashMgmtDropEvents').on('click', function (model) {
      this.model.depsdropstosave.trigger('paymentDone', model, this.currentPayment);
      delete this.currentPayment;
    }, this);

    //finished
    this.model.on('change:finished', function () {

      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_LblDone'), OB.I18N.getLabel('OBPOS_FinishCashMgmtDialog'), [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        action: function () {
          OB.POS.navigate('retail.pointofsale');
        }
      }]);
    }, this);
    //finishedWrongly
    this.model.on('change:finishedWrongly', function () {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_CashMgmtWronglyHeader'), OB.I18N.getLabel('OBPOS_CashMgmtWrongly'), [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        action: function () {
          OB.POS.navigate('retail.pointofsale');
        }
      }]);
    }, this);
  }
});

OB.POS.registerWindow({
  windowClass: OB.OBPOSCashMgmt.UI.CashManagement,
  route: 'retail.cashmanagement',
  menuPosition: 10,
  menuI18NLabel: 'OBPOS_LblCashManagement',
  permission: 'OBPOS_retail.cashmanagement'
});