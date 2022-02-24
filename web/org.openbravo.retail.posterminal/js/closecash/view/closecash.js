/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.Button',
  kind: 'OB.UI.ToolbarButton',
  classes: 'obObPosCloseCashUiButton',
  disabled: true,
  events: {
    onChangeStep: ''
  },
  init: function(model) {
    this.model = model;
  },
  tap: function() {
    return OB.POS.hwserver.checkDrawer(function() {
      if (this.disabled) {
        return true;
      }
      this.doChangeStep();
    }, this);
  },
  initialize: function() {
    if (this.i18nLabel) {
      this.setContent(OB.I18N.getLabel(this.i18nLabel));
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.CancelButton',
  kind: 'OB.UI.ToolbarButton',
  classes: 'obObPosCloseCashUiCancelButton',
  i18nLabel: 'OBMOBC_LblCancel',
  disabled: true,
  events: {
    onCancelCloseCash: ''
  },
  tap: function() {
    OB.POS.hwserver.checkDrawer(function() {
      this.doCancelCloseCash();
    }, this);
  }
});

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.LeftToolbarImpl',
  kind: 'OB.UI.MultiColumn.Toolbar',
  classes: 'obObPosCloseCashUiLeftToolbarImpl',
  published: {
    model: null
  },
  buttons: [
    {
      name: 'btnCloseCash',
      kind: 'OB.OBPOSCloseCash.UI.Button',
      classes: 'obObPosCloseCashUiCloseCash-rightToolbar-btnCloseCash',
      disabled: true,
      i18nLabel: 'OBPOS_LblCloseCash'
    },
    {
      name: 'btnToggleView',
      kind: 'OB.UI.ToolbarButton',
      classes: 'obObPosCloseCashUiLeftToolbarImpl-btnToggleView',
      i18nLabel: 'OBPOS_LblSwitchView',
      tap: function() {
        if (!this.disabled) {
          var keypadClass = 'obUiMultiColumn-panels-showKeypad',
            panels = OB.POS.terminal.$.containerWindow.getRoot().$
              .closeCashMultiColumn.$.panels;
          if (panels.hasClass(keypadClass)) {
            panels.removeClass(keypadClass);
          } else {
            panels.addClass(keypadClass);
          }
        }
      }
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.Button',
      name: 'btnPrevious',
      classes: 'obObPosCloseCashUiLeftToolbarImpl-btnPrevious',
      i18nLabel: 'OBPOS_LblPrevStep',
      stepCount: -1,
      span: 4,
      handlers: {
        onDisablePreviousButton: 'disablePreviousButton'
      },
      disablePreviousButton: function(inSender, inEvent) {
        this.setDisabled(inEvent.disable);
        if (this.hasClass('btn-over')) {
          this.removeClass('btn-over');
        }
      }
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.CancelButton',
      name: 'btnCancel',
      classes: 'obObPosCloseCashUiLeftToolbarImpl-btnCancel',
      disabled: false,
      span: 4
    },
    {
      kind: 'OB.OBPOSCloseCash.UI.Button',
      name: 'btnNext',
      classes: 'obObPosCloseCashUiLeftToolbarImpl-btnNext',
      i18nLabel: 'OBPOS_LblNextStep',
      stepCount: 1,
      span: 4,
      isEnableButton: false,
      isEnableNextButton: false,
      handlers: {
        onDisableNextButton: 'disableNextButton',
        onEnableNextButton: 'enableNextButton'
      },
      processesToListen: ['closeCashWindow'],
      disableButton: function() {
        this.setDisabled(true);
      },
      enableButton: function() {
        this.isEnableButton = true;
        if (this.isEnableNextButton) {
          this.setDisabled(false);
          if (this.hasClass('btn-over')) {
            this.removeClass('btn-over');
          }
        }
      },
      enableNextButton: function() {
        this.isEnableNextButton = true;
        if (this.isEnableButton) {
          this.setDisabled(false);
          if (this.hasClass('btn-over')) {
            this.removeClass('btn-over');
          }
        }
      },
      disableNextButton: function(inSender, inEvent) {
        this.isEnableNextButton = !inEvent.disable;
        if (this.isEnableButton) {
          this.setDisabled(inEvent.disable);
          if (this.hasClass('btn-over')) {
            this.removeClass('btn-over');
          }
        }
      }
    }
  ]
});

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.CloseCash',
  kind: 'OB.UI.WindowView',
  classes: 'obObPosCloseCashUiCloseCash',
  allowedIncrementalRefresh: false,
  incrementalRefreshOnNavigate: false,
  synchId: null,
  statics: {
    TitleExtensions: [],
    getTitleExtensions: function() {
      return this.TitleExtensions.reduce((memo, item) => {
        return memo + ' ' + item();
      }, '');
    }
  },
  handlers: {
    onButtonOk: 'buttonOk',
    onTapRadio: 'tapRadio',
    onChangeStep: 'changeStep',
    onCancelCloseCash: 'cancelCloseCash',
    onCountAllOK: 'countAllOK',
    onLineEditCount: 'lineEditCount',
    onPaymentMethodKept: 'paymentMethodKept',
    onResetQtyToKeep: 'resetQtyToKeep',
    onHoldActiveCmd: 'holdActiveCmd',
    onChangeCloseCashReport: 'changeCloseCashReport',
    onCommandFired: 'commandHandler'
  },
  published: {
    model: null
  },
  events: {
    onShowPopup: '',
    onChangeOption: '',
    onDisablePreviousButton: '',
    onDisableNextButton: '',
    onEnableNextButton: '',
    onSetPaymentMethodStatus: ''
  },
  letfPanelComponents: [
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
      kind: 'OB.OBPOSCloseCash.UI.PostPrintClose',
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
      kind: 'OB.UI.ModalSelectPrinters',
      name: 'modalSelectPrinters',
      classes: 'obObPosCloseCashUiCloseCash-modalSelectPrinters'
    }
  ],
  components: [
    {
      kind: 'OB.UI.MultiColumn',
      name: 'closeCashMultiColumn',
      classes: 'obObPosCloseCashUiCloseCash-closeCashMultiColumn',
      leftToolbar: {
        kind: 'OB.OBPOSCloseCash.UI.LeftToolbarImpl',
        name: 'leftToolbar',
        classes: 'obObPosCloseCashUiCloseCash-closeCashMultiColumn-leftToolbar',
        showMenu: false,
        showWindowsMenu: false
      },
      rightToolbar: {
        kind: 'OB.UI.MultiColumn.Toolbar',
        name: 'rightToolbar',
        classes:
          'obObPosCloseCashUiCloseCash-closeCashMultiColumn-rightToolbar',
        showMenu: false,
        showWindowsMenu: false,
        buttons: [
          {
            kind: 'OB.OBPOSCloseCash.UI.Button',
            name: 'btnCloseCash',
            classes: 'obObPosCloseCashUiCloseCash-rightToolbar-btnCloseCash',
            span: 12
          }
        ]
      },
      leftPanel: {
        name: 'closeCashLeftPanel',
        classes:
          'obObPosCloseCashUiCloseCash-closeCashMultiColumn-closeCashLeftPanel'
      },
      rightPanel: {
        classes:
          'obObPosCloseCashUiCloseCash-closeCashMultiColumn-closeCashRightPanel',
        name: 'closeCashRightPanel',
        components: [
          {
            kind: 'OB.OBPOSCloseCash.UI.CloseCashInfo',
            name: 'closeCashInfo',
            classes:
              'obObPosCloseCashUiCloseCash-closeCashMultiColumn-closeCashRightPanel-closeCashInfo'
          },
          {
            kind: 'OB.OBPOSCloseCash.UI.CloseCashKeyboard',
            name: 'closeCashKeyboard',
            classes:
              'obObPosCloseCashUiCloseCash-closeCashMultiColumn-closeCashRightPanel-closeCashKeyboard'
          }
        ]
      },
      resizeHandler: function() {
        // see issue https://issues.openbravo.com/view.php?id=43360
        var me = this;
        setTimeout(function() {
          me.render();
        }, 1);
      }
    }
  ],
  finalAction: function() {
    OB.POS.navigate('retail.pointofsale');
  },
  init: function() {
    // Create dinamically modal components
    this.modalComponents.forEach(modal => {
      this.createComponent(modal);
    });
    // Create dinamically Left Panel components
    this.letfPanelComponents.forEach(component => {
      this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.createComponent(
        component
      );
    });

    this.inherited(arguments);
    this.execution = OB.UTIL.ProcessController.start('closeCashWindow');

    this.$.closeCashMultiColumn.$.rightToolbar.$.rightToolbar.$.toolbar.$.btnCloseCash.setContent(
      OB.I18N.getLabel(this.titleLabel)
    );

    this.$.closeCashMultiColumn.$.rightPanel.$.closeCashInfo.setModel(
      this.model
    );
    this.$.closeCashMultiColumn.$.leftToolbar.$.leftToolbar.setModel(
      this.model
    );

    // Initialize not common steps
    this.initializeWindow();

    // Cash count
    this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.listPaymentMethods.setCollection(
      this.model.get('paymentList')
    );
    this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.listPaymentMethods.$.total.printAmount(
      this.model.get('totalExpected')
    );
    this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.listPaymentMethods.$.counted.printAmount(
      0
    );
    this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.listPaymentMethods.$.difference.printAmount(
      OB.DEC.sub(0, this.model.get('totalExpected'))
    );

    this.model.on('change:totalExpected', () => {
      this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.listPaymentMethods.$.total.printAmount(
        this.model.get('totalExpected')
      );
    });

    this.model.on('change:totalCounted', () => {
      this.model.set(
        'totalDifference',
        OB.DEC.sub(
          this.model.get('totalCounted'),
          this.model.get('totalExpected')
        )
      );
      this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.listPaymentMethods.$.counted.printAmount(
        this.model.get('totalCounted')
      );
      this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.listPaymentMethods.$.difference.printAmount(
        this.model.get('totalDifference')
      );
      if (this.model.get('totalDifference') <= 0) {
        this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.listPaymentMethods.$.differenceLbl.setContent(
          OB.I18N.getLabel('OBPOS_Remaining')
        );
      } else {
        this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.listPaymentMethods.$.differenceLbl.setContent(
          OB.I18N.getLabel('OBPOS_Surplus')
        );
      }
      this.waterfall('onAnyCounted');
      this.refreshButtons();
    });

    // Cash to keep
    this.model.on('change:stepOfStep3', model => {
      this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.cashToKeep.disableSelection();
      this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.cashToKeep.setPaymentToKeep(
        this.model.get('paymentList').at(this.model.get('stepOfStep3'))
      );
      this.refresh();
    });
    //FIXME:It is triggered only once, but it is not the best way to do it
    this.model.get('paymentList').on('reset', () => {
      const paymentList = this.model
        .get('paymentList')
        .at(this.model.get('substep'));
      if (paymentList) {
        paymentList.on('change:foreignCounted', () => {
          this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.cashToKeep.$.formkeep.renderBody(
            this.model.get('paymentList').at(this.model.get('substep'))
          );
        });
      }
    });

    // Close Cash Report
    //this data doesn't changes
    this.model.get('closeCashReport').on('reset', model => {
      this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.postPrintClose.setModel(
        this.model.get('closeCashReport').at(0)
      );
    });

    //This data changed when money is counted
    //difference is calculated after counted
    this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.postPrintClose.setSummary(
      this.model.getCountCashSummary()
    );
    this.model.on('change:totalDifference', model => {
      this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.postPrintClose.setSummary(
        this.model.getCountCashSummary()
      );
    });

    //finished
    this.model.on('change:finished', () => {
      let content;
      const messages = this.model.get('messages');

      // Build the content of the dialog.
      if (messages && messages.length) {
        content = [
          {
            content: OB.I18N.getLabel(this.finishCloseDialogLabel)
          },
          {
            allowHtml: true,
            content: '&nbsp;'
          }
        ];
        for (let i = 0; i < messages.length; i++) {
          content.push({
            content: OB.UTIL.decodeXMLComponent(messages[i])
          });
        }
      } else {
        content = OB.I18N.getLabel(this.finishCloseDialogLabel);
      }

      const excuteCashupSentHooks = () => {
        OB.UTIL.HookManager.executeHooks(this.cashupSentHook, {}, () => {
          if (
            OB.MobileApp.view.$.confirmationContainer.getAttribute(
              'openedPopup'
            ) !== OB.I18N.getLabel('OBPOS_MsgPrintAgainCashUp')
          ) {
            // Only display the good job message if there are no components displayed
            OB.UTIL.showConfirmation.display(
              OB.I18N.getLabel('OBPOS_LblGoodjob'),
              content,
              [
                {
                  label: OB.I18N.getLabel('OBMOBC_LblOk'),
                  isConfirmButton: true,
                  action: () => {
                    this.finalAction();
                    return true;
                  }
                }
              ],
              {
                autoDismiss: false,
                onHideFunction: () => {
                  this.finalAction();
                }
              }
            );
          }
        });
      };

      OB.UTIL.localStorage.removeItem('isCountOnRemoveOfSafeBox');
      const currentSafeBox = JSON.parse(
        OB.UTIL.localStorage.getItem('currentSafeBox')
      );
      if (
        this.name === 'cashUp' &&
        currentSafeBox != null &&
        currentSafeBox.countOnRemove
      ) {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_CountOnRemoveSafeBoxTitle'),
          OB.I18N.getLabel('OBPOS_CountOnRemoveSafeBoxText'),
          [
            {
              label: OB.I18N.getLabel('OBMOBC_LblOk'),
              isConfirmButton: true,
              action: function() {
                OB.UTIL.localStorage.setItem(
                  'isCountOnRemoveOfSafeBox',
                  'true'
                );
                // we have just remove the safe box, if we want to navigate to the count safe box,
                // we need to set again the safe box
                OB.UTIL.localStorage.setItem(
                  'currentSafeBox',
                  JSON.stringify(currentSafeBox)
                );
                OB.POS.navigate('retail.countSafeBox');
              }
            },
            {
              label: OB.I18N.getLabel('OBMOBC_LblCancel')
            }
          ],
          {
            onHideFunction: function() {
              excuteCashupSentHooks();
            }
          }
        );
      } else {
        excuteCashupSentHooks();
      }
    });
    //finishedWrongly
    this.model.on('change:finishedWrongly', model => {
      let message = '';
      if (model.get('errorMessage')) {
        message = OB.I18N.getLabel(model.get('errorMessage'), [
          model.get('errorDetail')
        ]);
      } else {
        message = OB.I18N.getLabel('OBPOS_CashUpWrongly');
      }

      const errorNoNavigateToInitialScreen = model.get(
        'errorNoNavigateToInitialScreen'
      );
      if (
        errorNoNavigateToInitialScreen &&
        errorNoNavigateToInitialScreen === 'true'
      ) {
        model.set('cashUpSent', false);
        model.set('finishedWrongly', false);
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_CashUpWronglyHeader'),
          message,
          [
            {
              label: OB.I18N.getLabel('OBMOBC_LblOk'),
              isConfirmButton: true,
              action: () => {
                this.waterfall('onEnableNextButton');
                return true;
              }
            }
          ],
          {
            autoDismiss: false,
            onHideFunction: () => {
              this.waterfall('onEnableNextButton');
            }
          }
        );
      } else {
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBPOS_CashUpWronglyHeader'),
          message,
          [
            {
              label: OB.I18N.getLabel('OBMOBC_LblOk'),
              isConfirmButton: true,
              action: () => {
                OB.POS.navigate('retail.pointofsale');
                return true;
              }
            }
          ],
          {
            autoDismiss: false,
            onHideFunction: function() {
              OB.POS.navigate('retail.pointofsale');
            }
          }
        );
      }
    });

    this.refreshButtons();

    this.model.on('change:loadFinished', model => {
      this.loadFinished(model);
    });

    // Disconnect RFID
    if (OB.UTIL.RfidController.isRfidConfigured()) {
      OB.UTIL.RfidController.disconnectRFIDDevice();
    }
  },
  initializeWindow: function() {
    // To be implemented in each window that extends from OB.OBPOSCloseCash.UI.CloseCash
  },
  loadFinished: function(model) {
    if (model.get('loadFinished')) {
      this.moveStep(0);
    }
  },
  rendered: function() {
    OB.UTIL.ProcessController.finish('closeCashWindow', this.execution);
  },
  refreshButtons: function() {
    // Disable/Enable buttons
    this.waterfall('onDisablePreviousButton', {
      disable: !this.model.allowPrevious()
    });
    this.waterfall('onDisableNextButton', {
      disable: !this.model.allowNext()
    });
    // Show step keyboard
    this.$.closeCashMultiColumn.$.rightPanel.$.closeCashKeyboard.showToolbar(
      this.model.getStepToolbar()
    );
  },

  refresh: function() {
    // Show step panel
    this.model.showStep(
      this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$
    );
    // Refresh buttons
    this.refreshButtons();
    // Show button label.
    const nextButtonI18NLabel = this.model.nextButtonI18NLabel();
    this.$.closeCashMultiColumn.$.leftToolbar.$.leftToolbar.$.toolbar.$.btnNext.setContent(
      OB.I18N.getLabel(nextButtonI18NLabel)
    );
  },
  changeStep: function(inSender, inEvent) {
    const direction = inEvent.originator.stepCount;

    if (direction > 0) {
      // Check with the step if can go next.
      this.model.verifyStep(
        this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$,
        () => {
          this.moveStep(direction);
          OB.info(`Cashup step: ${this.model.get('step')}`);
        }
      );
    } else {
      this.moveStep(direction);
      OB.info(`Cashup step: ${this.model.get('step')}`);
    }
  },
  cancelCloseCash: function(inSender, inEvent) {
    OB.POS.navigate('retail.pointofsale');
  },
  moveStep: function(direction) {
    // direction can be -1, 0 or +1
    // allways moving substep by substep
    const nextstep = this.model.get('step'),
      nextsubstep = this.model.get('substep') + direction;

    if (nextstep <= 0) {
      // error. go to the begining
      const step = this.model.getFirstStep();
      this.model.set('step', step);
      this.model.set('substep', -1);
      this.moveStep(step);
    } else if (this.model.isFinishedWizard(nextstep)) {
      // Sets the time
      this.model
        .get('closeCashReport')
        .at(0)
        .set('time', new Date());
      //send cash up to the server if it has not been sent yet
      if (this.model.get('cashUpSent')) {
        return true;
      }
      this.$.closeCashMultiColumn.$.leftToolbar.$.leftToolbar.$.toolbar.$.btnNext.setDisabled(
        true
      );
      this.model.set('cashUpSent', true);
      this.model.processAndFinishCloseCash();
    } else if (nextsubstep < 0) {
      // jump to previous step
      const previous = this.model.getPreviousStep();
      this.model.set('step', previous);
      this.model.set('substep', this.model.getSubstepsLength(previous));
      this.moveStep(-1);
    } else if (nextsubstep >= this.model.getSubstepsLength(nextstep)) {
      // jump to next step
      const next = this.model.getNextStep();
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
  countAllOK: function(inSender, inEvent) {
    this.model.countAll();
    this.refreshButtons();
  },
  lineEditCount: function(inSender, inEvent) {
    this.waterfall('onShowColumn', {
      colNum: 1
    });
    this.$.closeCashMultiColumn.$.rightPanel.$.closeCashKeyboard.setStatus(
      inEvent.originator.model.get('_id')
    );
  },
  commandHandler: function(inSender, inEvent) {
    //On selecting payment method, focus corresponding payment's counted field
    const paymentList = this.$.closeCashMultiColumn.$.leftPanel.$
      .closeCashLeftPanel.$.listPaymentMethods.$.paymentsList.$.tbody.children;
    let paymentLine = paymentList.find(payment => {
      return payment.controls[0].model.get('searchKey') === inEvent.key;
    });

    if (paymentLine) {
      this.waterfall('onSetPaymentMethodStatus', {
        originator: paymentLine.controls[0]
      });
    }
  },
  paymentMethodKept: function(inSender, inEvent) {
    const validationResult = this.model.validateCashKeep(inEvent.qtyToKeep);
    if (validationResult.result) {
      this.model
        .get('paymentList')
        .at(this.model.get('substep'))
        .set('qtyToKeep', inEvent.qtyToKeep);
      this.model
        .get('paymentList')
        .at(this.model.get('substep'))
        .set('selectedCashToKeep', inEvent.name);
      this.model
        .get('paymentList')
        .at(this.model.get('substep'))
        .set('variableAmtToKeep', inEvent.qtyToKeep);
      this.model
        .get('paymentList')
        .at(this.model.get('substep'))
        .set('isCashToKeepSelected', true);
    } else {
      OB.UTIL.showWarning(validationResult.message);
    }
    this.refreshButtons();
    this.$.closeCashMultiColumn.$.rightPanel.$.closeCashKeyboard.setStatus(
      inEvent.name
    );
  },
  resetQtyToKeep: function(inSender, inEvent) {
    this.model
      .get('paymentList')
      .at(this.model.get('substep'))
      .set('qtyToKeep', null);
    this.refreshButtons();
  },
  holdActiveCmd: function(inSender, inEvent) {
    this.waterfall('onChangeOption', {
      cmd: inEvent.cmd
    });
  },
  changeCloseCashReport: function() {
    this.$.closeCashMultiColumn.$.leftPanel.$.closeCashLeftPanel.$.postPrintClose.setModel(
      this.model.get('closeCashReport').at(0)
    );
  }
});
