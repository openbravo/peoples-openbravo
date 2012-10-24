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
    onResetQtyToKeep: 'resetQtyToKeep'
  },
  components: [{
    classes: 'row',
    components: [{
      classes: 'span6',
      components: [{
        kind: 'OB.OBPOSCashUp.UI.ListPendingReceipts',
        name: 'listPendingReceipts'
      }, {
        kind: 'OB.OBPOSCashUp.UI.ListPaymentMethods',
        name: 'listPaymentMethods',
        showing: false
      }, {
        kind: 'OB.OBPOSCashUp.UI.CashToKeep',
        name: 'cashToKeep',
        showing: false
      }, {
        kind: 'OB.OBPOSCashUp.UI.PostPrintClose',
        name: 'postPrintClose',
        showing: false
      }]
    }, {
      classes: 'span6',
      components: [{
        classes: 'span6',
        components: [{
          kind: 'OB.OBPOSCashUp.UI.CashUpInfo',
          name: 'cashUpInfo'
        }, {
          kind: 'OB.OBPOSCashUp.UI.CashUpKeyboard',
          name: 'cashUpKeyboard'
        }]
      }]
    }, {
      kind: 'OB.UI.ModalCancel',
      name: 'modalCancel'
    }, {
      kind: 'OB.OBPOSCashUp.UI.modalFinished',
      name: 'modalFinished',
      myId: 'modalFinished'
    }, {
      kind: 'OB.OBPOSCashUp.UI.modalFinishedWrongly',
      name: 'modalFinishedWrongly',
      myId: 'modalFinishedWrongly'
    }, {
      kind: 'OB.OBPOSCashUp.UI.modalPendingToProcess',
      name: 'modalPendingToProcess',
      myId: 'modalPendingToProcess'
    }]
  }],
  init: function () {
    this.inherited(arguments);

    this.$.cashUpInfo.setModel(this.model);

    //step 0
    this.model.on('change:pendingOrdersToProcess', function (model) {
      $('#modalprocessreceipts').modal('show');
    }, this);


    // Pending Orders - Step 1
    this.$.listPendingReceipts.setCollection(this.model.get('orderlist'));
    this.model.get('orderlist').on('all', function () {
      this.$.cashUpInfo.refresh();
    }, this);

    // Cash count - Step 2
    this.$.listPaymentMethods.setCollection(this.model.get('paymentList'));
    this.$.listPaymentMethods.$.total.setTotal(this.model.get('totalExpected'));
    this.$.listPaymentMethods.$.diference.setTotal(OB.DEC.sub(0, this.model.get('totalExpected')));
    this.$.cashUpKeyboard.setPayments(this.model.getData('DataCloseCashPaymentMethod'));

    this.model.on('change:totalCounted', function () {
      this.$.listPaymentMethods.$.diference.setTotal(OB.DEC.sub(this.model.get('totalCounted'), this.model.get('totalExpected')));
      this.model.set("totalDifference", OB.DEC.sub(this.model.get('totalCounted'), this.model.get('totalExpected')));
      this.waterfall('onAnyCounted');
      this.refresh();
    }, this);

    this.model.on('change:step', function (model) {
      this.refresh();
    }, this);

    // Cash to keep - Step 3.
    this.$.cashToKeep.setPaymentToKeep(this.model.get('paymentList').at(this.model.get('stepOfStep3')));

    this.model.on('change:stepOfStep3', function (model) {
      this.$.cashToKeep.disableSelection();
      this.$.cashToKeep.setPaymentToKeep(this.model.get('paymentList').at(this.model.get('stepOfStep3')));
      this.refresh();
    }, this);
    this.model.get('paymentList').at(this.model.get('stepOfStep3')).on('change:foreignCounted', function () {
      this.$.cashToKeep.$.formkeep.renderBody(this.model.get('paymentList').at(this.model.get('stepOfStep3')));
    }, this);
    // Cash Up Report - Step 4
    //this data doesn't changes
    this.$.postPrintClose.setModel(this.model.get('cashUpReport').at(0));

    //This data changed when money is counted
    //difference is calculated after counted
    this.$.postPrintClose.setSummary(this.model.getCountCashSummary());
    this.model.on('change:totalDifference', function (model) {
      this.$.postPrintClose.setSummary(this.model.getCountCashSummary());
    }, this);

    //finished
    this.model.on('change:finished', function () {
      $('#modalFinished').modal('show');
    }, this);
    //finishedWrongly
    this.model.on('change:finishedWrongly', function () {
      $('#modalFinishedWrongly').modal('show');
    }, this);

    this.refresh();
  },
  refresh: function () {
    this.$.listPendingReceipts.setShowing(this.model.showPendingOrdersList());
    this.$.listPaymentMethods.setShowing(this.model.showPaymentMethodList());
    this.$.cashToKeep.setShowing(this.model.showCashToKeep());
    this.$.postPrintClose.setShowing(this.model.showPostPrintClose());
    this.$.cashUpKeyboard.showToolbar(this.model.showPaymentMethodList() ? 'toolbarcountcash' : 'toolbarempty');

    this.$.cashUpInfo.refresh();
  },
  changeStep: function (inSender, inEvent) {
    var nextStep, nextStepOfStep3;
    if (this.model.get('step') === 4 && inEvent.originator.stepCount > 0) {
      //send cash up to the server
      this.model.processAndFinishCashUp();
    } else {
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
    this.$.cashUpInfo.refresh();
  },
  lineEditCount: function (sender, event) {
    this.$.cashUpKeyboard.setStatus(event.originator.model.get('_id'));
  },
  paymentMethodKept: function (inSender, event) {
    var validationResult = this.model.validateCashKeep(event.qtyToKeep);
    if (validationResult.result) {
      this.model.get('paymentList').at(this.model.get('stepOfStep3')).set('qtyToKeep', event.qtyToKeep);
    } else {
      OB.UTIL.showWarning(validationResult.message);
    }
    this.$.cashUpInfo.refresh();
  },
  resetQtyToKeep: function (inSender, event) {
    this.model.get('paymentList').at(this.model.get('stepOfStep3')).set('qtyToKeep', null);
    this.$.cashUpInfo.refresh();
  }
});

OB.POS.registerWindow({
  windowClass: OB.OBPOSCashUp.UI.CashUp,
  route: 'retail.cashup',
  online: true,
  menuPosition: 20,
  menuLabel: OB.I18N.getLabel('OBPOS_LblCloseCash')
});