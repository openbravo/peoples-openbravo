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
    if (this.model.get('cashUpSent')) {
      return true;
    }
    if (this.model.get('step') === 4 && this.getName() === 'btnNext') {
      this.setDisabled(true);
      this.model.set('cashUpSent', true);
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
    kind: 'OB.OBPOSCashUp.UI.Button',
    name: 'btnCancel',
    disabled: false,
    i18nLabel: 'OBMOBC_LblCancel',
    stepCount: 0,
    span: 4
  }, {
    kind: 'OB.OBPOSCashUp.UI.Button',
    name: 'btnNext',
    i18nLabel: 'OBPOS_LblNextStep',
    stepCount: 1,
    span: 4,
    handlers: {
      onDisableNextButton: 'disableNextButton'
    },
    disableNextButton: function (inSender, inEvent) {
      this.setDisabled(inEvent.disable);
      this.setContent(OB.I18N.getLabel('OBPOS_LblNextStep'));
      if (this.model.get('step') === 4) {
        //in the last step the button shows another label
        this.setContent(OB.I18N.getLabel('OBPOS_LblPostPrintClose'));
      }
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
    onDisableNextButton: ''
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
      this.refresh();
    }, this);

    // Cash count - Step 2
    this.$.cashupMultiColumn.$.leftPanel.$.listPaymentMethods.setCollection(this.model.get('paymentList'));
    this.$.cashupMultiColumn.$.leftPanel.$.listPaymentMethods.$.total.setTotal(this.model.get('totalExpected'));
    this.$.cashupMultiColumn.$.leftPanel.$.listPaymentMethods.$.diference.setTotal(OB.DEC.sub(0, this.model.get('totalExpected')));
    this.$.cashupMultiColumn.$.rightPanel.$.cashUpKeyboard.setPayments(this.model.getData('DataCloseCashPaymentMethod'));

    this.model.on('change:totalCounted', function () {
      this.$.cashupMultiColumn.$.leftPanel.$.listPaymentMethods.$.diference.setTotal(OB.DEC.sub(this.model.get('totalCounted'), this.model.get('totalExpected')));
      this.model.set("totalDifference", OB.DEC.sub(this.model.get('totalCounted'), this.model.get('totalExpected')));
      this.waterfall('onAnyCounted');
      this.refresh();
    }, this);

    this.model.on('change:step', function (model) {
      this.refresh();
    }, this);

    // Cash to keep - Step 3.
    this.$.cashupMultiColumn.$.leftPanel.$.cashToKeep.setPaymentToKeep(this.model.get('paymentList').at(this.model.get('stepOfStep3')));

    this.model.on('change:stepOfStep3', function (model) {
      this.$.cashupMultiColumn.$.leftPanel.$.cashToKeep.disableSelection();
      this.$.cashupMultiColumn.$.leftPanel.$.cashToKeep.setPaymentToKeep(this.model.get('paymentList').at(this.model.get('stepOfStep3')));
      this.refresh();
    }, this);
    this.model.get('paymentList').at(this.model.get('stepOfStep3')).on('change:foreignCounted', function () {
      this.$.cashupMultiColumn.$.leftPanel.$.cashToKeep.$.formkeep.renderBody(this.model.get('paymentList').at(this.model.get('stepOfStep3')));
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
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_LblGoodjob'), OB.I18N.getLabel('OBPOS_FinishCloseDialog'), [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        action: function () {
          OB.POS.navigate('retail.pointofsale');
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

      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBPOS_CashUpWronglyHeader'), message, [{
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        action: function () {
          OB.POS.navigate('retail.pointofsale');
          return true;
        }
      }]);
    }, this);

    this.refresh();
  },
  refresh: function () {
    this.waterfall('onDisablePreviousButton', {
      disable: !this.model.allowPrevious()
    });
    this.waterfall('onDisableNextButton', {
      disable: !this.model.allowNext()
    });
    this.$.cashupMultiColumn.$.leftPanel.$.listPendingReceipts.setShowing(this.model.showPendingOrdersList());
    this.$.cashupMultiColumn.$.leftPanel.$.listPaymentMethods.setShowing(this.model.showPaymentMethodList());
    this.$.cashupMultiColumn.$.leftPanel.$.cashToKeep.setShowing(this.model.showCashToKeep());
    this.$.cashupMultiColumn.$.leftPanel.$.postPrintClose.setShowing(this.model.showPostPrintClose());
    if (this.model.showPaymentMethodList()) {
      this.$.cashupMultiColumn.$.rightPanel.$.cashUpKeyboard.showToolbar('toolbarcountcash');
    } else {
      if (this.model.get('paymentList').at(this.model.get('stepOfStep3')).get('paymentMethod').allowvariableamount) {
        this.$.cashupMultiColumn.$.rightPanel.$.cashUpKeyboard.showToolbar('toolbarother');
      } else {
        this.$.cashupMultiColumn.$.rightPanel.$.cashUpKeyboard.showToolbar('toolbarempty');
      }
    }
  },
  changeStep: function (inSender, inEvent) {
    var nextStep, nextStepOfStep3;
    if (this.model.get('step') === 4 && inEvent.originator.stepCount > 0) {
      //send cash up to the server
      this.model.processAndFinishCashUp();
    } else {
      if (inEvent.originator.stepCount === 0) {
        OB.POS.navigate('retail.pointofsale');
        return true;
      }
      if (this.model.get('step') !== 3) {
        nextStep = this.model.get('step') + inEvent.originator.stepCount;
        if (nextStep === 3 && this.model.get('ignoreStep3')) {
          this.model.set('step', this.model.get('step') + inEvent.originator.stepCount + inEvent.originator.stepCount);
          //To step 4 or to step 2
        } else {
          //if the new step is 3 we should set the substep number
          if (nextStep === 3) {
            if (inEvent.originator.stepCount > 0) {
              //we come from step 2
              nextStepOfStep3 = 0;
              if (this.model.isStep3Needed(nextStepOfStep3) === false) {
                this.model.set('step', nextStep, {
                  silent: true
                });
                this.model.set('stepOfStep3', nextStepOfStep3, {
                  silent: true
                });
                this.changeStep(this, inEvent);
              } else {
                //change the substep, not the step
                this.model.set('step', nextStep);
                this.model.set('stepOfStep3', nextStepOfStep3);
                //because the last stepOfStep3 was the same that im setting the event is not raised
                this.model.trigger("change:stepOfStep3");
              }
            } else {
              //we come from step 4
              nextStepOfStep3 = this.model.get('paymentList').length - 1;
              if (this.model.isStep3Needed(nextStepOfStep3) === false) {
                this.model.set('step', nextStep, {
                  silent: true
                });
                this.model.set('stepOfStep3', nextStepOfStep3, {
                  silent: true
                });
                this.changeStep(this, inEvent);
              } else {
                //change the substep, not the step
                this.model.set('step', nextStep);
                this.model.set('stepOfStep3', nextStepOfStep3);
                //because the last stepOfStep3 was the same that im setting the event is not raised
                this.model.trigger("change:stepOfStep3");
              }
            }
          } else {
            this.model.set('step', nextStep);
          }
        }
      } else {
        nextStep = this.model.get('stepOfStep3') + inEvent.originator.stepCount;
        //if the new step is 2 or 4 we should set the step number
        if (nextStep < 0 || nextStep > this.model.get('paymentList').length - 1) {
          //change the step and not change the substep
          this.$.cashupMultiColumn.$.leftPanel.$.postPrintClose.setSummary(this.model.getCountCashSummary());
          this.model.set('step', this.model.get('step') + inEvent.originator.stepCount);
        } else {
          if (this.model.isStep3Needed(nextStep) === false) {
            this.model.set('stepOfStep3', nextStep, {
              silent: true
            });
            this.changeStep(this, inEvent);
          } else {
            //change the substep, not the step
            this.model.set('stepOfStep3', nextStep);
          }
        }
      }
    }
  },
  countAllOK: function (inSender, inEvent) {
    this.model.countAll();
    this.refresh();
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
      this.model.get('paymentList').at(this.model.get('stepOfStep3')).set('qtyToKeep', inEvent.qtyToKeep);
    } else {
      OB.UTIL.showWarning(validationResult.message);
    }
    this.refresh();
    this.$.cashupMultiColumn.$.rightPanel.$.cashUpKeyboard.setStatus(inEvent.name);
  },
  resetQtyToKeep: function (inSender, inEvent) {
    this.model.get('paymentList').at(this.model.get('stepOfStep3')).set('qtyToKeep', null);
    this.refresh();
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