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
  name: 'OB.OBPOSCashUp.UI.CashUp',
  kind: 'OB.OBPOSCloseCash.UI.CloseCash',
  classes: 'obObPosCashupUiCashUp',
  windowmodel: OB.OBPOSCashUp.Model.CashUp,
  titleLabel: 'OBPOS_LblCloseCash',
  finishCloseDialogLabel: 'OBPOS_FinishCloseDialog',
  cashupSentHook: 'OBPOS_AfterCashUpSent',
  letfPanelComponents: [
    {
      classes:
        'obObPosCashupUiCashUp-closeCashMultiColumn-closeCashLeftPanel-listPendingReceipts',
      kind: 'OB.OBPOSCashUp.UI.ListPendingReceipts',
      name: 'listPendingReceipts',
      showing: false
    },
    {
      classes:
        'obObPosCashupUiCashUp-closeCashMultiColumn-closeCashLeftPanel-cashMaster',
      kind: 'OB.OBPOSCashUp.UI.CashMaster',
      name: 'cashMaster',
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
      kind: 'OB.OBPOSCashUp.UI.PostPrintClose',
      name: 'postPrintClose',
      showing: false
    }
  ],
  modalComponents: [
    {
      kind: 'OB.UI.ModalCancel',
      name: 'modalCancel',
      classes: 'obObPosCloseCashUiCloseCash-modalCancel'
    },
    {
      kind: 'OB.OBPOSCashUp.UI.modalPendingToProcess',
      name: 'modalPendingToProcess',
      classes: 'obObPosCashupUiCashUp-modalPendingToProcess'
    },
    {
      kind: 'OB.UI.ModalSelectPrinters',
      name: 'modalSelectPrinters',
      classes: 'obObPosCloseCashUiCloseCash-modalSelectPrinters'
    }
  ],
  finalAction: function() {
    if (JSON.parse(OB.UTIL.localStorage.getItem('isSafeBox'))) {
      // Open drawer to insert the Safe Box
      OB.POS.hwserver.openDrawer(
        false,
        OB.MobileApp.model.get('permissions').OBPOS_timeAllowedDrawerCount
      );
      OB.UTIL.showLoggingOut(true);
      OB.MobileApp.model.logout();
    } else {
      OB.POS.navigate('retail.pointofsale');
    }
  },
  initializeWindow: function() {
    // Step 0
    this.model.on('change:pendingOrdersToProcess', model => {
      this.doShowPopup({
        popup: 'modalprocessreceipts'
      });
    });

    // Pending Orders
    this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.listPendingReceipts.setCollection(
      this.model.get('orderlist')
    );
    this.model.get('orderlist').on('all', () => {
      this.refreshButtons();
    });

    this.model.on('change:slavesCashupCompleted', model => {
      this.refreshButtons();
    });
  },
  loadFinished: function(model) {
    const processCashCloseSlave = callback => {
      new OB.DS.Process(
        'org.openbravo.retail.posterminal.ProcessCashCloseSlave'
      ).exec(
        {
          cashUpId: OB.POS.modelterminal.get('terminal').cashUpId
        },
        function(data) {
          if (data && data.exception) {
            // Error handler
            OB.log('error', data.exception.message);
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_CashMgmtError'),
              OB.I18N.getLabel('OBPOS_ErrorServerGeneric') +
                data.exception.message,
              [
                {
                  label: OB.I18N.getLabel('OBPOS_LblRetry'),
                  action: () => {
                    processCashCloseSlave(callback);
                  }
                }
              ],
              {
                autoDismiss: false,
                onHideFunction: () => {
                  OB.POS.navigate('retail.pointofsale');
                }
              }
            );
          } else {
            callback(data);
          }
        }
      );
    };
    if (model.get('loadFinished')) {
      if (OB.POS.modelterminal.get('terminal').isslave) {
        processCashCloseSlave(data => {
          if (data.hasMaster) {
            this.moveStep(0);
          } else {
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_CashUpWronglyHeader'),
              OB.I18N.getLabel('OBPOS_ErrCashupMasterNotOpen'),
              null,
              {
                autoDismiss: false,
                onHideFunction: () => {
                  OB.POS.navigate('retail.pointofsale');
                }
              }
            );
          }
        });
      } else {
        this.moveStep(0);
      }
    }
  }
});

OB.POS.registerWindow({
  windowClass: OB.OBPOSCashUp.UI.CashUp,
  route: 'retail.cashup',
  online: false,
  menuPosition: 20,
  menuI18NLabel: 'OBPOS_LblCloseCash',
  permission: 'OBPOS_retail.cashup',
  approvalType: 'OBPOS_approval.cashup',
  rfidState: false,
  navigateTo: function(args, successCallback, errorCallback) {
    // Cannot navigate to the cashup window in case of being a seller terminal
    if (!OB.MobileApp.model.get('hasPaymentsForCashup')) {
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_NavigationNotAllowedHeader'),
        OB.I18N.getLabel('OBPOS_CannotNavigateToCashUp'),
        [
          {
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            action: function() {
              errorCallback();
            }
          }
        ],
        {
          onHideFunction: function() {
            errorCallback();
          }
        }
      );
      return;
    }
    // in case of synchronized mode reload the cashup from the server
    // this is needed because there is a slight change that the cashup on the client
    // is out of date
    if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
      OB.UTIL.rebuildCashupFromServer(function() {
        successCallback(args.route);
      });
    } else {
      successCallback(args.route);
    }
  },
  menuItemDisplayLogic: function() {
    // Update here the label of CashUp in case Safe Box configuration is active
    if (OB.MobileApp.model.get('terminal').terminalType.safebox) {
      this.$.lbl.setContent(OB.I18N.getLabel('OBPOS_LblRemoveSafeBox'));
    } else {
      this.$.lbl.setContent(OB.I18N.getLabel('OBPOS_LblCloseCash'));
    }
    return OB.MobileApp.model.get('hasPaymentsForCashup');
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashUpPartial',
  kind: 'OB.OBPOSCashUp.UI.CashUp',
  classes: 'obObPosCashUpUiCashUpPartial',
  windowmodel: OB.OBPOSCashUp.Model.CashUpPartial,
  titleLabel: 'OBPOS_LblCloseCashPartial',
  finishCloseDialogLabel: 'OBPOS_FinishPartialDialog',
  cashupSentHook: 'OBPOS_AfterCashUpPartialSent',
  finalAction: function() {
    OB.POS.navigate('retail.pointofsale');
  }
});

OB.POS.registerWindow({
  windowClass: OB.OBPOSCashUp.UI.CashUpPartial,
  route: 'retail.cashuppartial',
  online: false,
  menuPosition: 21,
  menuI18NLabel: 'OBPOS_LblCloseCashPartial',
  permission: 'OBPOS_retail.cashuppartial',
  approvalType: 'OBPOS_approval.cashuppartial',
  rfidState: false,
  navigateTo: function(args, successCallback, errorCallback) {
    if (!OB.MobileApp.model.get('hasPaymentsForCashup')) {
      // Cannot navigate to the cashup partial window in case of being a seller terminal
      OB.UTIL.showConfirmation.display(
        OB.I18N.getLabel('OBPOS_NavigationNotAllowedHeader'),
        OB.I18N.getLabel('OBPOS_CannotNavigateToPartialCashUp'),
        [
          {
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            action: function() {
              errorCallback();
            }
          }
        ],
        {
          onHideFunction: function() {
            errorCallback();
          }
        }
      );
      return;
    }
    successCallback(args.route);
  },
  menuItemDisplayLogic: function() {
    return OB.MobileApp.model.get('hasPaymentsForCashup');
  }
});
