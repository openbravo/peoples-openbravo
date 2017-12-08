/*
 ************************************************************************************
 * Copyright (C) 2012-2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _, $ */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.Button',
  kind: 'OB.UI.ToolbarButton',
  disabled: true,
  events: {
    onChangeStep: ''
  },
  init: function (model) {
    this.model = model;
  },
  tap: function () {
    return OB.POS.hwserver.checkDrawer(function () {
      if (this.disabled) {
        return true;
      }
      this.doChangeStep();
    }, this);
  },
  initialize: function () {
    if (this.i18nLabel) {
      this.setContent(OB.I18N.getLabel(this.i18nLabel));
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CancelButton',
  kind: 'OB.UI.ToolbarButton',
  i18nLabel: 'OBMOBC_LblCancel',
  disabled: true,
  events: {
    onCancelCashup: ''
  },
  tap: function () {
    OB.POS.hwserver.checkDrawer(function () {
      this.doCancelCashup();
    }, this);
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.LeftToolbarImpl',
  kind: 'OB.UI.MultiColumn.Toolbar',
  published: {
    model: null
  },
  buttons: [{
    kind: 'OB.OBPOSCashUp.UI.Button',
    name: 'btnPrevious',
    i18nLabel: 'OBPOS_LblPrevStep',
    stepCount: -1,
    span: 4,
    handlers: {
      onDisablePreviousButton: 'disablePreviousButton'
    },
    disablePreviousButton: function (inSender, inEvent) {
      this.setDisabled(inEvent.disable);
      if (this.hasClass('btn-over')) {
        this.removeClass('btn-over');
      }
    }
  }, {
    kind: 'OB.OBPOSCashUp.UI.CancelButton',
    name: 'btnCancel',
    disabled: false,
    span: 4
  }, {
    kind: 'OB.OBPOSCashUp.UI.Button',
    name: 'btnNext',
    i18nLabel: 'OBPOS_LblNextStep',
    stepCount: 1,
    span: 4,
    isEnableButton: false,
    isEnableNextButton: false,
    handlers: {
      onDisableNextButton: 'disableNextButton',
      onEnableNextButton: 'enableNextButton',
      synchronizing: 'disableButton',
      synchronized: 'enableButton'
    },
    disableButton: function () {
      this.setDisabled(true);
    },
    enableButton: function () {
      this.isEnableButton = true;
      if (this.isEnableNextButton) {
        this.setDisabled(false);
        if (this.hasClass('btn-over')) {
          this.removeClass('btn-over');
        }
      }
    },
    enableNextButton: function () {
      this.isEnableNextButton = true;
      if (this.isEnableButton) {
        this.setDisabled(false);
        if (this.hasClass('btn-over')) {
          this.removeClass('btn-over');
        }
      }
    },
    disableNextButton: function (inSender, inEvent) {
      this.isEnableNextButton = !inEvent.disable;
      if (this.isEnableButton) {
        this.setDisabled(inEvent.disable);
        if (this.hasClass('btn-over')) {
          this.removeClass('btn-over');
        }
      }
    }
  }]
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashUp',
  kind: 'OB.UI.WindowView',
  synchId: null,
  statics: {
    TitleExtensions: [],
    getTitleExtensions: function () {
      return _.reduce(this.TitleExtensions, function (memo, item) {
        return memo + ' ' + item();
      }, '');
    }
  },
  windowmodel: OB.OBPOSCashUp.Model.CashUp,
  titleLabel: 'OBPOS_LblCloseCash',
  finishCloseDialogLabel: 'OBPOS_FinishCloseDialog',
  cashupSentHook: 'OBPOS_AfterCashUpSent',
  handlers: {
    onButtonOk: 'buttonOk',
    onTapRadio: 'tapRadio',
    onChangeStep: 'changeStep',
    onCancelCashup: 'cancelCashup',
    onCountAllOK: 'countAllOK',
    onLineEditCount: 'lineEditCount',
    onPaymentMethodKept: 'paymentMethodKept',
    onResetQtyToKeep: 'resetQtyToKeep',
    onHoldActiveCmd: 'holdActiveCmd',
    onChangeCashupReport: 'changeCashupReport'
  },
  published: {
    model: null
  },
  events: {
    onShowPopup: '',
    onChangeOption: '',
    onDisablePreviousButton: '',
    onDisableNextButton: '',
    onEnableNextButton: ''
  },
  components: [{
    kind: 'OB.UI.MultiColumn',
    name: 'cashupMultiColumn',
    leftToolbar: {
      kind: 'OB.OBPOSCashUp.UI.LeftToolbarImpl',
      name: 'leftToolbar',
      showMenu: false,
      showWindowsMenu: false
    },
    rightToolbar: {
      kind: 'OB.UI.MultiColumn.Toolbar',
      name: 'rightToolbar',
      showMenu: false,
      showWindowsMenu: false,
      buttons: [{
        kind: 'OB.OBPOSCashUp.UI.Button',
        name: 'btnCashUp',
        span: 12
      }]
    },
    leftPanel: {
      name: 'cashupLeftPanel',
      components: [{
        classes: 'span12',
        kind: 'OB.OBPOSCashUp.UI.ListPendingReceipts',
        name: 'listPendingReceipts',
        showing: false
      }, {
        classes: 'span12',
        kind: 'OB.OBPOSCashUp.UI.CashMaster',
        name: 'cashMaster',
        showing: false
      }, {
        classes: 'span12',
        kind: 'OB.OBPOSCashUp.UI.CashPayments',
        name: 'cashPayments',
        showing: false
      }, {
        classes: 'span12',
        kind: 'OB.OBPOSCashUp.UI.ListPaymentMethods',
        name: 'listPaymentMethods',
        showing: false
      }, {
        classes: 'span12',
        kind: 'OB.OBPOSCashUp.UI.CashToKeep',
        name: 'cashToKeep',
        showing: false
      }, {
        classes: 'span12',
        kind: 'OB.OBPOSCashUp.UI.PostPrintClose',
        name: 'postPrintClose',
        showing: false
      }]
    },
    rightPanel: {
      classes: 'span12',
      name: 'cashupRightPanel',
      components: [{
        kind: 'OB.OBPOSCashUp.UI.CashUpInfo',
        name: 'cashUpInfo'
      }, {
        kind: 'OB.OBPOSCashUp.UI.CashUpKeyboard',
        name: 'cashUpKeyboard'
      }]
    }
  }, {
    kind: 'OB.UI.ModalCancel',
    name: 'modalCancel'
  }, {
    kind: 'OB.OBPOSCashUp.UI.modalPendingToProcess',
    name: 'modalPendingToProcess'
  }, {
    kind: 'OB.UI.ModalSelectPrinters',
    name: 'modalSelectPrinters'
  }],
  finalAction: function () {
    OB.POS.navigate('retail.pointofsale');
  },
  init: function () {
    this.synchId = OB.UTIL.SynchronizationHelper.busyUntilFinishes("cashup");
    var me = this;
    this.inherited(arguments);

    this.$.cashupMultiColumn.$.rightToolbar.$.rightToolbar.$.toolbar.$.button.$.theButton.$.btnCashUp.setContent(OB.I18N.getLabel(this.titleLabel));

    this.$.cashupMultiColumn.$.rightPanel.$.cashUpInfo.setModel(this.model);
    this.$.cashupMultiColumn.$.leftToolbar.$.leftToolbar.setModel(this.model);

    //step 0
    this.model.on('change:pendingOrdersToProcess', function (model) {
      this.doShowPopup({
        popup: 'modalprocessreceipts'
      });
    }, this);

    // Pending Orders - Step 1
    this.$.cashupMultiColumn.$.leftPanel.$.listPendingReceipts.setCollection(this.model.get('orderlist'));
    this.model.get('orderlist').on('all', function () {
      this.refreshButtons();
    }, this);

    // Cash count - Step 2
    this.$.cashupMultiColumn.$.leftPanel.$.listPaymentMethods.setCollection(this.model.get('paymentList'));
    this.$.cashupMultiColumn.$.leftPanel.$.listPaymentMethods.$.total.printAmount(this.model.get('totalExpected'));
    this.$.cashupMultiColumn.$.leftPanel.$.listPaymentMethods.$.difference.printAmount(OB.DEC.sub(0, this.model.get('totalExpected')));

    this.model.on('change:totalExpected', function () {
      this.$.cashupMultiColumn.$.leftPanel.$.listPaymentMethods.$.total.printAmount(this.model.get('totalExpected'));
    }, this);

    this.model.on('change:totalCounted', function () {
      this.model.set("totalDifference", OB.DEC.sub(this.model.get('totalCounted'), OB.Utilities.Number.roundJSNumber(this.model.get('totalExpected'), 2)));
      this.$.cashupMultiColumn.$.leftPanel.$.listPaymentMethods.$.difference.printAmount(this.model.get("totalDifference"));
      this.waterfall('onAnyCounted');
      this.refreshButtons();
    }, this);

    // Cash to keep - Step 3.
    this.model.on('change:stepOfStep3', function (model) {
      this.$.cashupMultiColumn.$.leftPanel.$.cashToKeep.disableSelection();
      this.$.cashupMultiColumn.$.leftPanel.$.cashToKeep.setPaymentToKeep(this.model.get('paymentList').at(this.model.get('stepOfStep3')));
      this.refresh();
    }, this);
    //FIXME:It is triggered only once, but it is not the best way to do it
    this.model.get('paymentList').on('reset', function () {
      var paymentList = this.model.get('paymentList').at(this.model.get('substep'));
      if (paymentList) {
        paymentList.on('change:foreignCounted', function () {
          this.$.cashupMultiColumn.$.leftPanel.$.cashToKeep.$.formkeep.renderBody(this.model.get('paymentList').at(this.model.get('substep')));
        }, this);
      }
    }, this);

    // Cash Up Report - Step 4
    //this data doesn't changes
    this.model.get('cashUpReport').on('reset', function (model) {
      this.$.cashupMultiColumn.$.leftPanel.$.postPrintClose.setModel(this.model.get('cashUpReport').at(0));
    });

    //This data changed when money is counted
    //difference is calculated after counted
    this.$.cashupMultiColumn.$.leftPanel.$.postPrintClose.setSummary(this.model.getCountCashSummary());
    this.model.on('change:totalDifference', function (model) {
      this.$.cashupMultiColumn.$.leftPanel.$.postPrintClose.setSummary(this.model.getCountCashSummary());
    }, this);

    //finished
    this.model.on('change:finished', function () {

      var content;
      var i;
      var messages = this.model.get('messages');
      var me = this;

      // Build the content of the dialog.
      if (messages && messages.length) {
        content = [{
          content: OB.I18N.getLabel(me.finishCloseDialogLabel)
        }, {
          allowHtml: true,
          content: '&nbsp;'
        }];
        for (i = 0; i < messages.length; i++) {
          content.push({
            content: OB.UTIL.decodeXMLComponent(messages[i])
          });
        }
      } else {
        content = OB.I18N.getLabel(me.finishCloseDialogLabel);
      }

      OB.UTIL.HookManager.executeHooks(me.cashupSentHook, {}, function () {

        if (OB.MobileApp.view.$.confirmationContainer.getAttribute('openedPopup') !== OB.I18N.getLabel('OBPOS_MsgPrintAgainCashUp')) {
          // Only display the good job message if there are no components displayed
          OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_LblGoodjob'), content, [{
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            isConfirmButton: true,
            action: function () {
              me.finalAction();
              return true;
            }
          }], {
            autoDismiss: false,
            onHideFunction: function () {
              me.finalAction();
            }
          });

        }
      });

    }, this);
    //finishedWrongly
    this.model.on('change:finishedWrongly', function (model) {
      // in case of synchronized mode then don't do specific things
      // message is already displayed
      if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
        return;
      }
      var message = "";
      if (model.get('errorMessage')) {
        message = OB.I18N.getLabel(model.get('errorMessage'), [model.get('errorDetail')]);
      } else {
        message = OB.I18N.getLabel('OBPOS_CashUpWrongly');
      }

      var errorNoNavigateToInitialScreen = model.get('errorNoNavigateToInitialScreen');
      if (errorNoNavigateToInitialScreen && errorNoNavigateToInitialScreen === 'true') {
        model.set('cashUpSent', false);
        model.set("finishedWrongly", false);
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_CashUpWronglyHeader'), message, [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function () {
            me.waterfall('onEnableNextButton');
            return true;
          }
        }], {
          autoDismiss: false,
          onHideFunction: function () {
            me.waterfall('onEnableNextButton');
          }
        });
      } else {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_CashUpWronglyHeader'), message, [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          isConfirmButton: true,
          action: function () {
            OB.POS.navigate('retail.pointofsale');
            return true;
          }
        }], {
          autoDismiss: false,
          onHideFunction: function () {
            OB.POS.navigate('retail.pointofsale');
          }
        });
      }
    }, this);

    this.refreshButtons();

    this.model.on('change:loadFinished', function (model) {

      function processCashCloseSlave(callback) {
        new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashCloseSlave').exec({
          cashUpId: OB.POS.modelterminal.get('terminal').cashUpId
        }, function (data) {
          if (data && data.exception) {
            // Error handler 
            OB.log('error', data.exception.message);
            OB.UTIL.showConfirmation.display(
            OB.I18N.getLabel('OBPOS_CashMgmtError'), OB.I18N.getLabel('OBPOS_ErrorServerGeneric') + data.exception.message, [{
              label: OB.I18N.getLabel('OBPOS_LblRetry'),
              action: function () {
                processCashCloseSlave(callback);
              }
            }], {
              autoDismiss: false,
              onHideFunction: function () {
                OB.POS.navigate('retail.pointofsale');
              }
            });
          } else {
            callback(data);
          }
        });
      }

      if (model.get("loadFinished")) {
        if (OB.POS.modelterminal.get('terminal').isslave) {
          processCashCloseSlave(function (data) {
            if (data.hasMaster) {
              me.moveStep(0);
            } else {
              OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_CashUpWronglyHeader'), OB.I18N.getLabel('OBPOS_ErrCashupMasterNotOpen'), null, {
                autoDismiss: false,
                onHideFunction: function () {
                  OB.POS.navigate('retail.pointofsale');
                }
              });
            }
          });
        } else {
          me.moveStep(0);
        }
      }
    });

    this.model.on('change:slavesCashupCompleted', function (model) {
      me.refreshButtons();
    });

    // Disconnect RFID
    if (OB.UTIL.RfidController.isRfidConfigured()) {
      OB.UTIL.RfidController.disconnectRFIDDevice();
    }
  },

  rendered: function () {
    OB.UTIL.SynchronizationHelper.finished(this.synchId, 'cashup');
  },

  refreshButtons: function () {
    // Disable/Enable buttons
    this.waterfall('onDisablePreviousButton', {
      disable: !this.model.allowPrevious()
    });
    this.waterfall('onDisableNextButton', {
      disable: !this.model.allowNext()
    });
    // Show step keyboard
    this.$.cashupMultiColumn.$.rightPanel.$.cashUpKeyboard.showToolbar(this.model.getStepToolbar());
  },

  refresh: function () {

    // Show step panel
    this.model.showStep(this.$.cashupMultiColumn.$.leftPanel.$);
    // Refresh buttons
    this.refreshButtons();
    // Show button label.
    var nextButtonI18NLabel = this.model.nextButtonI18NLabel();
    this.$.cashupMultiColumn.$.leftToolbar.$.leftToolbar.$.toolbar.getComponents()[2].$.theButton.$.btnNext.setContent(OB.I18N.getLabel(nextButtonI18NLabel));
  },
  changeStep: function (inSender, inEvent) {
    var direction = inEvent.originator.stepCount;
    var me = this;

    if (direction > 0) {
      // Check with the step if can go next.
      this.model.verifyStep(this.$.cashupMultiColumn.$.leftPanel.$, function () {
        me.moveStep(direction);
        OB.info("Cashup step: " + me.model.get('step'));
      });
    } else {
      this.moveStep(direction);
      OB.info("Cashup step: " + me.model.get('step'));
    }
  },
  cancelCashup: function (inSender, inEvent) {
    OB.POS.navigate('retail.pointofsale');
  },
  moveStep: function (direction) { // direction can be -1, 0 or +1
    // allways moving substep by substep
    var nextstep = this.model.get('step');
    var nextsubstep = this.model.get('substep') + direction;

    if (nextstep <= 0) {
      // error. go to the begining
      var step = this.model.getFirstStep();
      this.model.set('step', step);
      this.model.set('substep', -1);
      this.moveStep(step);
    } else if (this.model.isFinishedWizard(nextstep)) {
      // Sets the time
      this.model.get('cashUpReport').at(0).set('time', new Date());
      //send cash up to the server if it has not been sent yet
      if (this.model.get('cashUpSent')) {
        return true;
      }
      this.$.cashupMultiColumn.$.leftToolbar.$.leftToolbar.$.toolbar.getComponents()[2].$.theButton.$.btnNext.setDisabled(true);
      this.model.set('cashUpSent', true);
      this.model.processAndFinishCashUp();
    } else if (nextsubstep < 0) {
      // jump to previous step
      var previous = this.model.getPreviousStep();
      this.model.set('step', previous);
      this.model.set('substep', this.model.getSubstepsLength(previous));
      this.moveStep(-1);
    } else if (nextsubstep >= this.model.getSubstepsLength(nextstep)) {
      // jump to next step
      var next = this.model.getNextStep();
      this.model.set('step', next || nextstep + 1);
      this.model.set('substep', -1);
      this.moveStep(1);
    } else if (this.model.isSubstepAvailable(nextstep, nextsubstep)) {
      // go to step
      this.model.set('step', nextstep);
      this.model.set('substep', nextsubstep);
      this.refresh();
    } else {
      // move again
      this.model.set('step', nextstep);
      this.model.set('substep', nextsubstep);
      this.moveStep(direction === 0 ? 1 : direction);
    }
  },
  countAllOK: function (inSender, inEvent) {
    this.model.countAll();
    this.refreshButtons();
  },
  lineEditCount: function (inSender, inEvent) {
    this.waterfall('onShowColumn', {
      colNum: 1
    });
    this.$.cashupMultiColumn.$.rightPanel.$.cashUpKeyboard.setStatus(inEvent.originator.model.get('_id'));
  },
  paymentMethodKept: function (inSender, inEvent) {
    var validationResult = this.model.validateCashKeep(inEvent.qtyToKeep);
    if (validationResult.result) {
      this.model.get('paymentList').at(this.model.get('substep')).set('qtyToKeep', inEvent.qtyToKeep);
      this.model.get('paymentList').at(this.model.get('substep')).set('selectedCashToKeep', inEvent.name);
      this.model.get('paymentList').at(this.model.get('substep')).set('variableAmtToKeep', inEvent.qtyToKeep);
      this.model.get('paymentList').at(this.model.get('substep')).set('isCashToKeepSelected', true);
    } else {
      OB.UTIL.showWarning(validationResult.message);
    }
    this.refreshButtons();
    this.$.cashupMultiColumn.$.rightPanel.$.cashUpKeyboard.setStatus(inEvent.name);
  },
  resetQtyToKeep: function (inSender, inEvent) {
    this.model.get('paymentList').at(this.model.get('substep')).set('qtyToKeep', null);
    this.refreshButtons();
  },
  holdActiveCmd: function (inSender, inEvent) {
    this.waterfall('onChangeOption', {
      cmd: inEvent.cmd
    });
  },
  changeCashupReport: function () {
    this.$.cashupMultiColumn.$.leftPanel.$.postPrintClose.setModel(this.model.get('cashUpReport').at(0));
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
  navigateTo: function (args, successCallback, errorCallback) {
    var me = this;
    // Cannot navigate to the cashup window in case of being a seller terminal
    if (!OB.MobileApp.model.get('hasPaymentsForCashup')) {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_NavigationNotAllowedHeader'), OB.I18N.getLabel('OBPOS_CannotNavigateToCashUp'), [{
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
    // in case of synchronized mode reload the cashup from the server
    // this is needed because there is a slight change that the cashup on the client 
    // is out of date
    if (OB.MobileApp.model.hasPermission('OBMOBC_SynchronizedMode', true)) {
      OB.UTIL.rebuildCashupFromServer(function () {
        successCallback(args.route);
      });
    } else {
      successCallback(args.route);
    }
  },
  menuItemDisplayLogic: function () {
    return OB.MobileApp.model.get('hasPaymentsForCashup');
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashUpPartial',
  kind: 'OB.OBPOSCashUp.UI.CashUp',
  windowmodel: OB.OBPOSCashUp.Model.CashUpPartial,
  titleLabel: 'OBPOS_LblCloseCashPartial',
  finishCloseDialogLabel: 'OBPOS_FinishPartialDialog',
  cashupSentHook: 'OBPOS_AfterCashUpPartialSent',
  finalAction: function () {
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
  navigateTo: function (args, successCallback, errorCallback) {
    if (!OB.MobileApp.model.get('hasPaymentsForCashup')) {
      // Cannot navigate to the cashup partial window in case of being a seller terminal
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_NavigationNotAllowedHeader'), OB.I18N.getLabel('OBPOS_CannotNavigateToPartialCashUp'), [{
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
    successCallback(args.route);
  },
  menuItemDisplayLogic: function () {
    return OB.MobileApp.model.get('hasPaymentsForCashup');
  }
});