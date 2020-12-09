/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OB.OBPOSCountSafeBox.UI.CountSafeBox',
  kind: 'OB.OBPOSCloseCash.UI.CloseCash',
  classes: 'obObposCloseCashUiCloseCash',
  windowmodel: OB.OBPOSCountSafeBox.Model.CountSafeBox,
  titleLabel: 'OBPOS_LblCountSafeBox',
  finishCloseDialogLabel: 'OBPOS_FinishCountDialog',
  cashupSentHook: 'OBPOS_AfterCountSafeBoxSent',
  letfPanelComponents: [
    {
      classes:
        'obObPosCountSafeBox-closeCashMultiColumn-closeCashLeftPanel-listSafeBoxes',
      kind: 'OB.OBPOSCountSafeBox.UI.ListSafeBoxes',
      name: 'listSafeBoxes',
      showing: false
    },
    {
      classes:
        'obObPosCloseCashUiCloseCash-closeCashMultiColumn-closeCashLeftPanel-cashPayments',
      kind: 'OB.OBPOSCloseCash.UI.CashPayments',
      name: 'cashPayments',
      showing: false
    },
    {
      classes:
        'obObPosCloseCashUiCloseCash-closeCashMultiColumn-closeCashLeftPanel-listPaymentMethods',
      kind: 'OB.OBPOSCloseCash.UI.ListPaymentMethods',
      name: 'listPaymentMethods',
      showing: false
    },
    {
      classes:
        'obObPosCloseCashUiCloseCash-closeCashMultiColumn-closeCashLeftPanel-cashToKeep',
      kind: 'OB.OBPOSCloseCash.UI.CashToKeep',
      name: 'cashToKeep',
      showing: false
    },
    {
      classes:
        'obObPosCloseCashUiCloseCash-closeCashMultiColumn-closeCashLeftPanel-postPrintClose',
      kind: 'OB.OBPOSCountSafeBox.UI.PostPrintClose',
      name: 'postPrintClose',
      showing: false
    }
  ],
  finalAction: function() {
    // Open drawer to insert the Safe Box
    OB.POS.hwserver.openDrawer(
      false,
      OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerCount
    );

    OB.UTIL.showLoggingOut(true);
    OB.MobileApp.model.logout();
  },
  initializeWindow: function() {
    // Safe Box list - Step 1
    this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.listSafeBoxes.setCollection(
      this.model.get('safeBoxesList')
    );
    this.model.get('safeBoxesList').on(
      'all',
      function() {
        this.refreshButtons();
      },
      this
    );
    this.model.on(
      'selectedSafeBox',
      function() {
        this.refreshButtons();
      },
      this
    );
  }
});

OB.POS.registerWindow({
  windowClass: OB.OBPOSCountSafeBox.UI.CountSafeBox,
  route: 'retail.countSafeBox',
  online: true,
  menuPosition: 23,
  menuI18NLabel: 'OBPOS_LblCountSafeBox',
  permission: 'OBPOS_retail.cashup',
  approvalType: 'OBPOS_approval.cashup',
  rfidState: false,
  navigateTo: function(args, successCallback, errorCallback) {
    if (
      OB.UTIL.isNullOrUndefined(
        OB.UTIL.localStorage.getItem('currentSafeBox')
      ) &&
      !OB.MobileApp.model.hasPermission('OBPOS_approval.manager.safebox', true)
    ) {
      errorCallback();
    } else {
      successCallback(args.route);
    }
  },
  menuItemDisplayLogic: function() {
    return JSON.parse(OB.UTIL.localStorage.getItem('isSafeBox'));
  }
});
