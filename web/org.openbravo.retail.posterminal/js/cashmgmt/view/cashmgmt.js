/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

enyo.kind({
  name: 'OB.OBPOSCashMgmt.UI.LeftToolbarImpl',
  kind: 'OB.UI.MultiColumn.Toolbar',
  synchId: null,
  buttons: [{
    kind: 'OB.UI.ToolbarButton',
    name: 'btnCancel',
    disabled: false,
    i18nLabel: 'OBMOBC_LblCancel',
    stepCount: 0,
    span: 6,
    tap: function () {
      OB.POS.hwserver.checkDrawer(function () {
        OB.POS.navigate('retail.pointofsale');
      });
    }
  }, {
    kind: 'OB.UI.ToolbarButton',
    name: 'btnDone',
    disabled: true,
    i18nLabel: 'OBPOS_LblDone',
    stepCount: 0,
    span: 6,
    processesToListen: ['showLoading'],
    disableButton: function () {
      this.setDisabled(true);
    },
    enableButton: function () {
      this.setDisabled(false);
    },
    init: function (model) {
      this.model = model;
    },
    tap: function () {
      OB.POS.hwserver.checkDrawer(function () {
        var hasProvider = false,
            payment = null,
            currentDrop = null;
        this.model.depsdropstosave.each(function (drop) {
          payment = _.find(OB.POS.modelterminal.get('payments'), function (p) {
            return p.payment.id === drop.get('paymentMethodId');
          });
          if (payment && payment.paymentMethod.cashManagementProvider) {
            hasProvider = true;
            currentDrop = drop;
          }
        });
        if (hasProvider) {
          this.bubble('onShowPopup', {
            popup: 'modalpayment',
            args: {
              'receipt': null,
              'cashManagement': this.model,
              'provider': payment.paymentMethod.cashManagementProvider,
              'key': payment.paymentMethod.searchKey,
              'name': payment.paymentMethod._identifier,
              'paymentMethod': payment.paymentMethod,
              'amount': currentDrop.get('amount'),
              'rate': payment.rate,
              'mulrate': payment.mulrate,
              'isocode': payment.isocode
            }
          });
        } else {
          this.model.depsdropstosave.trigger('makeDeposits');
        }
      }, this);
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
  allowedIncrementalRefresh: false,
  incrementalRefreshOnNavigate: false,
  events: {
    onShowPopup: ''
  },
  handlers: {
    onPaymentChanged: 'paymentChanged',
    onPaymentChangedCancelled: 'paymentChangedCancelled',
    onAdvancedFilterSelector: 'advancedFilterSelector',
    onChangeFilterSelector: 'changeFilterSelector',
    onCashManagementOpenWindow: 'cashManagementOpenWindow'
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
    }, {
      kind: 'OB.UI.ModalSelectPrinters'
    }]
  }, {
    name: 'otherSubWindowsContainer',
    components: [{
      kind: 'OB.UI.ModalSelectorBusinessPartners',
      name: 'modalcustomer'
    }, {
      kind: 'OB.UI.ModalAdvancedFilterBP',
      name: 'modalAdvancedFilterBP'
    }]
  }],

  paymentChanged: function (inSender, inEvent) {
    // sending the event to the components bellow this one
    this.waterfall('onButtonPaymentChanged', inEvent);
  },

  paymentChangedCancelled: function (inSender, inEvent) {
    // sending the event to the components bellow this one
    this.waterfall('onButtonPaymentChangedCancelled', inEvent);
  },

  advancedFilterSelector: function (inSender, inEvent) {
    this.waterfall('onGetAdvancedFilterSelector', inEvent);
  },

  changeFilterSelector: function (inSender, inEvent) {
    this.waterfall('onUpdateFilterSelector', inEvent);
  },

  cashManagementOpenWindow: function (inSender, inEvent) {
    inEvent.keyboard = this;
    this.waterfall('onCashManagementOpen', inEvent);
  },

  init: function () {
    this.inherited(arguments);

    // cashMgmtDepositEvents or cashMgmtDropEvents Collection is shown by OB.UI.Table, when selecting an option 'click' event
    // is triggered, propagating this UI event to model here
    this.model.get('cashMgmtDepositEvents').on('click', function (model) {
      var me = this;
      this.model.depsdropstosave.trigger('paymentDone', model, this.currentPayment, function () {
        OB.info('[CashMgmntEvent] Item Added. Current Cash Mgmnt items: ' + JSON.stringify(me.model.depsdropstosave.models.map(function (item) {
          return item.getRelevantInformationString();
        })));
        delete me.currentPayment;
      }, function (error) {
        OB.error('[CashMgmntEvent] Error executing paymentDone ' + error);
        delete me.currentPayment;
      });
    }, this);

    this.model.get('cashMgmtDropEvents').on('click', function (model) {
      var me = this;
      this.model.depsdropstosave.trigger('paymentDone', model, this.currentPayment, function () {
        OB.info('[CashMgmntEvent] Item Added. Current Cash Mgmnt items: ' + JSON.stringify(me.model.depsdropstosave.models.map(function (item) {
          return item.getRelevantInformationString();
        })));
        delete me.currentPayment;
      }, function (error) {
        OB.error('[CashMgmntEvent] Error executing paymentDone ' + error);
        delete me.currentPayment;
      });
    }, this);

    //finished
    this.model.on('change:finished', function () {

      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_LblDone'), OB.I18N.getLabel('OBPOS_FinishCashMgmtDialog'), [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        isConfirmButton: true,
        action: function () {
          OB.POS.navigate('retail.pointofsale');
        }
      }], {
        autoDismiss: false,
        onHideFunction: function () {
          OB.POS.navigate('retail.pointofsale');
        }
      });
    }, this);
    //finishedWrongly
    this.model.on('change:finishedWrongly', function () {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_CashMgmtWronglyHeader'), OB.I18N.getLabel('OBPOS_CashMgmtWrongly'), [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        isConfirmButton: true,
        action: function () {
          OB.POS.navigate('retail.pointofsale');
        }
      }], {
        autoDismiss: false,
        onHideFunction: function () {
          OB.POS.navigate('retail.pointofsale');
        }
      });
    }, this);
    // Disconnect RFID
    if (OB.UTIL.RfidController.isRfidConfigured()) {
      OB.UTIL.RfidController.disconnectRFIDDevice();
    }
  }
});

OB.POS.registerWindow({
  windowClass: OB.OBPOSCashMgmt.UI.CashManagement,
  route: 'retail.cashmanagement',
  menuPosition: 10,
  menuI18NLabel: 'OBPOS_LblCashManagement',
  permission: 'OBPOS_retail.cashmanagement',
  approvalType: 'OBPOS_approval.cashmgmt',
  rfidState: false,
  navigateTo: function (args, successCallback, errorCallback) {
    if (!OB.MobileApp.model.get('hasPaymentsForCashup')) {
      // Cannot navigate to the cash management window in case of being a seller terminal
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_NavigationNotAllowedHeader'), OB.I18N.getLabel('OBPOS_CannotNavigateToCashManagement'), [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        action: function () {
          errorCallback();
        }
      }], {
        onHideFunction: function () {
          errorCallback();
        }
      });
      return;
    }
    if (_.filter(OB.MobileApp.model.paymentnames, function (payment) {
      return payment.paymentMethod.iscash === true;
    }).length > 0) {
      successCallback(args.route);
    } else {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_CashMgmtError'), OB.I18N.getLabel('OBPOS_NoCashPaymentMethod'), [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        action: function () {
          errorCallback();
        }
      }], {
        onHideFunction: function () {
          errorCallback();
        }
      });
      return;
    }
  },
  menuItemDisplayLogic: function () {
    return OB.MobileApp.model.get('hasPaymentsForCashup') && _.filter(OB.MobileApp.model.paymentnames, function (payment) {
      return payment.paymentMethod.iscash === true;
    }).length > 0;
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSCashMgmt.UI.CashManagement', {
  kind: 'OB.UI.ModalPayment',
  name: 'modalpayment'
});