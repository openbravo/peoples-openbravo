/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, $*/

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
    if (this.disabled) {
      return true;
    }
    this.doChangeStep();
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
    this.doCancelCashup();
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RightToolbarImpl',
  kind: 'OB.UI.MultiColumn.Toolbar',
  buttons: [{
    kind: 'OB.OBPOSCashUp.UI.Button',
    name: 'btnCashUp',
    span: 12,
    i18nLabel: 'OBPOS_LblCloseCash'
  }]
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
    handlers: {
      onDisableNextButton: 'disableNextButton',
      onEnableNextButton: 'enableNextButton'
    },
    enableNextButton: function () {
      this.setDisabled(false);
      if (this.hasClass('btn-over')) {
        this.removeClass('btn-over');
      }
    },
    disableNextButton: function (inSender, inEvent) {
      this.setDisabled(inEvent.disable);
      if (this.hasClass('btn-over')) {
        this.removeClass('btn-over');
      }
    }
  }]
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashUp',
  kind: 'OB.UI.WindowView',
  windowmodel: OB.OBPOSCashUp.Model.CashUp,
  handlers: {
    onButtonOk: 'buttonOk',
    onTapRadio: 'tapRadio',
    onChangeStep: 'changeStep',
    onCancelCashup: 'cancelCashup',
    onCountAllOK: 'countAllOK',
    onLineEditCount: 'lineEditCount',
    onPaymentMethodKept: 'paymentMethodKept',
    onResetQtyToKeep: 'resetQtyToKeep',
    onHoldActiveCmd: 'holdActiveCmd'
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
      kind: 'OB.OBPOSCashUp.UI.RightToolbarImpl',
      name: 'rightToolbar',
      showMenu: false,
      showWindowsMenu: false
    },
    leftPanel: {
      name: 'cashupLeftPanel',
      components: [{
        classes: 'span12',
        kind: 'OB.OBPOSCashUp.UI.ListPendingReceipts',
        name: 'listPendingReceipts'
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
  }],
  init: function () {
    var me = this;
    this.inherited(arguments);

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
    this.$.cashupMultiColumn.$.rightPanel.$.cashUpKeyboard.setPayments(this.model.getData('DataCloseCashPaymentMethod'));

    this.model.on('change:totalCounted', function () {
      this.$.cashupMultiColumn.$.leftPanel.$.listPaymentMethods.$.difference.printAmount(OB.DEC.sub(this.model.get('totalCounted'), this.model.get('totalExpected')));
      this.model.set("totalDifference", OB.DEC.sub(this.model.get('totalCounted'), this.model.get('totalExpected')));
      this.waterfall('onAnyCounted');
      this.refreshButtons();
    }, this);

    // Cash to keep - Step 3.
    this.$.cashupMultiColumn.$.leftPanel.$.cashToKeep.setPaymentToKeep(this.model.get('paymentList').at(this.model.get('substep')));

    this.model.get('paymentList').at(this.model.get('substep')).on('change:foreignCounted', function () {
      this.$.cashupMultiColumn.$.leftPanel.$.cashToKeep.$.formkeep.renderBody(this.model.get('paymentList').at(this.model.get('substep')));
    }, this);
    // Cash Up Report - Step 4
    //this data doesn't changes
    this.$.cashupMultiColumn.$.leftPanel.$.postPrintClose.setModel(this.model.get('cashUpReport').at(0));

    //This data changed when money is counted
    //difference is calculated after counted
    this.$.cashupMultiColumn.$.leftPanel.$.postPrintClose.setSummary(this.model.getCountCashSummary());
    this.model.on('change:totalDifference', function (model) {
      this.$.cashupMultiColumn.$.leftPanel.$.postPrintClose.setSummary(this.model.getCountCashSummary());
    }, this);

    //finished
    this.model.on('change:finished', function () {

      // this.model.messages ????
      var content;
      var i;
      var messages = this.model.get('messages');
      var next = this.model.get('next');

      // Build the content of the dialog.
      if (messages && messages.length) {
        content = [{
          content: OB.I18N.getLabel('OBPOS_FinishCloseDialog')
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
        content = OB.I18N.getLabel('OBPOS_FinishCloseDialog');
      }

      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_LblGoodjob'), content, [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        action: function () {
          if ('logout' === next) {
            OB.UTIL.showLoggingOut(true);
            OB.MobileApp.model.logout();
          } else {
            OB.POS.navigate('retail.pointofsale');
          }
          return true;
        }
      }]);

    }, this);
    //finishedWrongly
    this.model.on('change:finishedWrongly', function (model) {
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
          action: function () {
            me.waterfall('onEnableNextButton');
            return true;
          }
        }]);
      } else {
        OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_CashUpWronglyHeader'), message, [{
          label: OB.I18N.getLabel('OBMOBC_LblOk'),
          action: function () {
            OB.POS.navigate('retail.pointofsale');
            return true;
          }
        }]);
      }
    }, this);

    this.refreshButtons();
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
      });
    } else {
      this.moveStep(direction);
    }
  },
  cancelCashup: function (inSender, inEvent) {
    OB.POS.navigate('retail.pointofsale');
  },
  moveStep: function (direction) { // direction can be -1 or +1
    // allways moving substep by substep
    var nextstep = this.model.get('step');
    var nextsubstep = this.model.get('substep') + direction;

    if (nextstep <= 0) {
      // error. go to the begining   
      this.model.set('step', 1);
      this.model.set('substep', -1);
      this.moveStep(1);
    } else if (this.model.isFinishedWizard(nextstep)) {
      //send cash up to the server if it has not been sent yet
      if (this.model.get('cashUpSent')) {
        return true;
      }
      this.$.cashupMultiColumn.$.leftToolbar.$.leftToolbar.$.toolbar.getComponents()[2].$.theButton.$.btnNext.setDisabled(true);
      this.model.set('cashUpSent', true);
      this.model.processAndFinishCashUp();
    } else if (nextsubstep < 0) {
      // jump to previous step
      this.model.set('step', nextstep - 1);
      this.model.set('substep', this.model.getSubstepsLength(nextstep - 1));
      this.moveStep(-1);
    } else if (nextsubstep >= this.model.getSubstepsLength(nextstep)) {
      // jump to next step
      this.model.set('step', nextstep + 1);
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
      this.moveStep(direction);
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
  }
});

OB.POS.registerWindow({
  windowClass: OB.OBPOSCashUp.UI.CashUp,
  route: 'retail.cashup',
  online: true,
  menuPosition: 20,
  menuI18NLabel: 'OBPOS_LblCloseCash',
  permission: 'OBPOS_retail.cashup'
});