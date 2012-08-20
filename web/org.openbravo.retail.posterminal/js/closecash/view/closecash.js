/*global OB, enyo, $*/

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.OBPOSCashUp.UI.modalFinished',
  header: OB.I18N.getLabel('OBPOS_LblGoodjob'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_FinishCloseDialog')
  },
  bodyButtons: {
    tag: 'div',
    components: [{
      //OK button
      kind: 'OB.UI.Button',
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      content: OB.I18N.getLabel('OBPOS_LblOk'),
      tap: function() {
        $('#' + this.parent.parent.parent.parent.parent.getId()).modal('hide');
        OB.POS.navigate('retail.pointofsale');
      }
    }]
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.OBPOSCashUp.UI.modalPendingToProcess',
  header: OB.I18N.getLabel('OBPOS_LblReceiptsToProcess'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_MsgReceiptsProcess')
  },
  bodyButtons: {
    tag: 'div',
    components: [{
      kind: 'OB.UI.Button',
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      content: OB.I18N.getLabel('OBPOS_LblOk'),
      tap: function() {
        //continue with orders which have been paid.
        $('#' + this.parent.parent.parent.parent.parent.getId()).modal('hide');
      }
    }, {
      kind: 'OB.UI.Button',
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      content: OB.I18N.getLabel('OBPOS_LblCancel'),
      tap: function() {
        $('#' + this.parent.parent.parent.parent.parent.getId()).modal('hide');
        OB.POS.navigate('retail.pointofsale');
      }
    }]
  }
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
    onResetQtyToKeep: 'resetQtyToKeep'
  },
  tapRadio: function(inSender, inEvent) {
    //    if (inEvent.originator.name === 'allowvariableamount') {
    //      //      FIXME: Put focus on the input
    //      //      this.$.cashToKeep.$.variableamount.focus();
    //    }
    this.$.cashUpInfo.$.buttonNext.setDisabled(false);
  },
  buttonOk: function(inSender, inEvent) {
    //    $('button[button*="allokbutton"]').css('visibility','hidden');
    //    var elem = this.me.options.modeldaycash.paymentmethods.get(this.options[this._id].rowid);
    //    this.options['counted_'+this.options[this._id].rowid].$el.text(OB.I18N.formatCurrency(elem.get('expected')));
    //    elem.set('counted',OB.DEC.add(0,elem.get('expected')));
    //    this.me.options.modeldaycash.set('totalCounted',OB.DEC.add(this.me.options.modeldaycash.get('totalCounted'),elem.get('counted')));
    //    this.options['counted_'+this.rowid].$el.show();
    //    if($('button[button="okbutton"][style!="display: none; "]').length===0){
    //      this.me.options.closenextbutton.$el.removeAttr('disabled');
    //    }
  },
  prevStep: function(inSender, inEvent) {
    var found = false;
    if (this.model.get('step') === 3 || this.model.get('step') === 2) {
      //Count Cash back from Post, print & Close.
      if (this.model.get('step') === 2) {
        this.model.set('allowedStep', this.model.get('allowedStep') - 1);
      } else {
        this.model.set('step', 2);
      }
      found = false;
      this.$.cashUpInfo.$.buttonNext.setDisabled(true);
      $(".active").removeClass("active");
      //Count Cash to Cash to keep or Cash to keep to Cash to keep
      if ($(".active").length === 0) {
        this.$.cashToKeep.show();
        this.$.postPrintClose.hide();
        this.$.cashUpInfo.$.buttonNext.setContent(OB.I18N.getLabel('OBPOS_LblNextStep'));
      }
      while (this.model.get('allowedStep') >= 0) {

        //FIXME:Delete this line when Step 2 works well
        this.model.payList.at(this.model.get('allowedStep')).set('counted', 0);


        if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').automatemovementtoother) {
          found = true;
          $('#cashtokeepheader').text(OB.I18N.getLabel('OBPOS_LblStep3of4', [this.model.payList.at(this.model.get('allowedStep')).get('name')]));
          if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').keepfixedamount) {
            if (!this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').amount) {
              this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').amount = 0;
            }
            if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').amount > this.model.payList.at(this.model.get('allowedStep')).get('counted')) {
              this.$.cashToKeep.$.keepfixedamount.setContent(OB.I18N.formatCurrency(this.model.payList.at(this.model.get('allowedStep')).get('counted')));
              this.$.cashToKeep.$.keepfixedamount.value = this.model.payList.at(this.model.get('allowedStep')).get('counted');
            } else {
              this.$.cashToKeep.$.keepfixedamount.setContent(OB.I18N.formatCurrency(this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').amount));
              this.$.cashToKeep.$.keepfixedamount.value = this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').amount;
            }
            this.$.cashToKeep.$.keepfixedamount.show();
          } else {
            this.$.cashToKeep.$.keepfixedamount.hide();
          }
          if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').allowmoveeverything) {
            this.$.cashToKeep.$.allowmoveeverything.value = 0;
            this.$.cashToKeep.$.allowmoveeverything.setContent(OB.I18N.getLabel('OBPOS_LblNothing'));
            this.$.cashToKeep.$.allowmoveeverything.show();
          } else {
            this.$.cashToKeep.$.allowmoveeverything.hide();
          }
          if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').allowdontmove) {
            this.$.cashToKeep.$.allowdontmove.value = this.model.payList.at(this.model.get('allowedStep')).get('counted');
            this.$.cashToKeep.$.allowdontmove.setContent(OB.I18N.getLabel('OBPOS_LblTotalAmount') + ' ' + OB.I18N.formatCurrency(OB.DEC.add(0, this.model.payList.at(this.model.get('allowedStep')).get('counted'))));
            this.$.cashToKeep.$.allowdontmove.show();
          } else {
            this.$.cashToKeep.$.allowdontmove.hide();
          }
          if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').allowvariableamount) {
            this.$.cashToKeep.$.allowvariableamount.setContent(OB.I18N.getLabel('OBPOS_LblOther'));
            this.$.cashToKeep.$.allowvariableamount.show();
            this.$.cashToKeep.$.variableamount.show();
            this.$.cashToKeep.$.variableamount.value = '';
          } else {
            this.$.cashToKeep.$.allowvariableamount.hide();
            this.$.cashToKeep.$.variableamount.hide();
          }
          break;
        }
        this.model.set('allowedStep', this.model.get('allowedStep') - 1);
      }
      if (found === false) {
        this.$.listPaymentMethods.show();
        this.$.cashToKeep.hide();
        this.$.cashUpKeyboard.showToolbar('toolbarcountcash');
        this.model.set('step', 1);
        this.model.set('allowedStep', 0);
        this.$.cashUpInfo.$.buttonNext.setDisabled(false);
      }
    } else if (this.model.get('step') === 1) {
      //Pending receipts back from Count Cash.
      this.$.listPendingReceipts.show();
      this.$.listPaymentMethods.hide();
      this.$.cashUpInfo.$.buttonPrev.setDisabled(true);
      this.$.cashUpInfo.$.buttonNext.setDisabled(false);
      this.$.cashUpKeyboard.showToolbar('toolbarempty');
      this.model.set('step', 0);
      //    if($('button[button="okbutton"][style!="display: none; "]').length!==0){
      //      this.$el.attr('disabled','disabled');
      //    }
    }
  },
  nextStep: function(inSender, inEvent) {
    var found = false;
    if (this.model.get('step') === 0) {
      this.$.listPendingReceipts.hide();
      this.$.listPaymentMethods.show();
      this.$.cashUpInfo.$.buttonPrev.setDisabled(false);
      this.$.cashUpKeyboard.showToolbar('toolbarcountcash');
      this.model.set('step', this.model.get('step') + 1);
      //show toolbarcountcash
      //      if($('button[button="okbutton"][style!="display: none; "]').length!==0){
      //        this.$el.attr('disabled','disabled');
      //      }
    } else if (this.model.get('step') === 1 || this.model.get('step') === 2) {
      found = false;
      if (this.model.get('step') === 2) {
        this.model.set('allowedStep', this.model.get('allowedStep') + 1);
      }
      this.$.cashUpInfo.$.buttonNext.setDisabled(true);
      this.model.set('step', 2);
      //Count Cash to Cash to keep or Cash to keep to Cash to keep
      if ($(".active").length > 0 && this.model.get('allowedStep') !== 0) {
        if ($('.active').text() === "") { //Variable Amount
          if (this.$.cashToKeep.$.variableamount.value === '') {
            this.model.payList.at(this.model.get('allowedStep') - 1).get('paymentMethod').amountToKeep = 0;
          } else {
            if (OB.I18N.parseNumber(this.$.cashToKeep.$.variableamount.value) <= this.model.payList.at(this.model.get('allowedStep') - 1).get('counted')) {
              this.model.payList.at(this.model.get('allowedStep') - 1).get('paymentMethod').amountToKeep = OB.I18N.parseNumber(this.$.cashToKeep.$.variableamount.value);
            } else {
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgMoreThanCounted'));
              this.model.set('allowedStep', this.model.get('allowedStep') - 1);
              this.$.cashUpInfo.$.buttonNext.setDisabled(false);
              return true;
            }
          }
        } else {
          this.model.payList.at(this.model.get('allowedStep') - 1).get('paymentMethod').amountToKeep = OB.I18N.parseNumber($('.active').text());
        }
        $(".active").removeClass("active");
      }
      while (this.model.get('allowedStep') < this.model.payList.length) {
        //FIXME:Delete this line when Step 2 works well
        this.model.payList.at(this.model.get('allowedStep')).set('counted', 0);


        if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').automatemovementtoother) {
          found = true;
          $('#cashtokeepheader').text(OB.I18N.getLabel('OBPOS_LblStep3of4', [this.model.payList.at(this.model.get('allowedStep')).get('name')]));
          if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').keepfixedamount) {
            if (!this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').amount) {
              this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').amount = 0;
            }
            if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').amount > this.model.payList.at(this.model.get('allowedStep')).get('counted')) {
              this.$.cashToKeep.$.keepfixedamount.setContent(OB.I18N.formatCurrency(this.model.payList.at(this.model.get('allowedStep')).get('counted')));
              this.$.cashToKeep.$.keepfixedamount.value = this.model.payList.at(this.model.get('allowedStep')).get('counted');
            } else {
              this.$.cashToKeep.$.keepfixedamount.setContent(OB.I18N.formatCurrency(this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').amount));
              this.$.cashToKeep.$.keepfixedamount.value = this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').amount;
            }
            this.$.cashToKeep.$.keepfixedamount.show();
          } else {
            this.$.cashToKeep.$.keepfixedamount.hide();
          }
          if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').allowmoveeverything) {
            this.$.cashToKeep.$.allowmoveeverything.value = 0;
            this.$.cashToKeep.$.allowmoveeverything.setContent(OB.I18N.getLabel('OBPOS_LblNothing'));
            this.$.cashToKeep.$.allowmoveeverything.show();
          } else {
            this.$.cashToKeep.$.allowmoveeverything.hide();
          }
          if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').allowdontmove) {
            this.$.cashToKeep.$.allowdontmove.value = this.model.payList.at(this.model.get('allowedStep')).get('counted');
            this.$.cashToKeep.$.allowdontmove.setContent(OB.I18N.getLabel('OBPOS_LblTotalAmount') + ' ' + OB.I18N.formatCurrency(OB.DEC.add(0, this.model.payList.at(this.model.get('allowedStep')).get('counted'))));
            this.$.cashToKeep.$.allowdontmove.show();
          } else {
            this.$.cashToKeep.$.allowdontmove.hide();
          }
          if (this.model.payList.at(this.model.get('allowedStep')).get('paymentMethod').allowvariableamount) {
            this.$.cashToKeep.$.allowvariableamount.setContent(OB.I18N.getLabel('OBPOS_LblOther'));
            this.$.cashToKeep.$.allowvariableamount.show();
            this.$.cashToKeep.$.variableamount.show();
            this.$.cashToKeep.$.variableamount.value = '';
          } else {
            this.$.cashToKeep.$.allowvariableamount.hide();
            this.$.cashToKeep.$.variableamount.hide();
          }
          break;
        }
        this.model.set('allowedStep', this.model.get('allowedStep') + 1);
      }
      if (found === false) {
        this.$.postPrintClose.show();
        this.$.cashToKeep.hide();
        this.$.listPaymentMethods.hide();
        //        this.options.renderpaymentlines.$el.empty();
        //        this.options.renderpaymentlines.render();
        this.$.cashUpInfo.$.buttonNext.setContent(OB.I18N.getLabel('OBPOS_LblPostPrintClose'));
        this.$.cashUpInfo.$.buttonNext.setDisabled(false);
        this.model.set('allowedStep', this.model.get('allowedStep') - 1);
        this.model.set('step', 3);
        this.model.time = new Date().toString().substring(3, 24);
        $('#reportTime').text(OB.I18N.getLabel('OBPOS_LblTime') + ': ' + new Date().toString().substring(3, 24));
      } else {
        this.$.listPaymentMethods.hide();
        this.$.cashToKeep.show();
        this.$.cashUpKeyboard.showToolbar('toolbarempty');
      }
    }
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
      kind: 'OB.OBPOSCashUp.UI.modalPendingToProcess',
      name: 'modalPendingToProcess',
      myId: 'modalPendingToProcess'
    }]
  }],
  init: function() {
    this.inherited(arguments);

    this.$.cashUpInfo.setModel(this.model);

    //step 0
    this.model.on('change:pendingOrdersToProcess', function(model) {
      $('#modalprocessreceipts').modal('show');
    }, this);


    // Pending Orders - Step 1
    this.$.listPendingReceipts.setCollection(this.model.get('orderlist'));
    this.model.get('orderlist').on('all', function() {
      this.$.cashUpInfo.refresh();
    }, this);

    // Cash count - Step 2
    this.$.listPaymentMethods.setCollection(this.model.get('paymentList'));
    this.$.listPaymentMethods.$.total.setTotal(this.model.get('totalExpected'));
    this.$.listPaymentMethods.$.diference.setTotal(OB.DEC.sub(0, this.model.get('totalExpected')));
    this.$.cashUpKeyboard.setPayments(this.model.getData('DataCloseCashPaymentMethod'));

    this.model.on('change:totalCounted', function() {
      this.$.listPaymentMethods.$.diference.setTotal(OB.DEC.sub(this.model.get('totalCounted'), this.model.get('totalExpected')));
      this.model.set("totalDifference", OB.DEC.sub(this.model.get('totalCounted'), this.model.get('totalExpected')));
      this.waterfall('onAnyCounted');
      this.refresh();
    }, this);

    this.model.on('change:step', function(model) {
      this.refresh();
    }, this);

    // Cash to keep - Step 3.
    this.$.cashToKeep.setPaymentToKeep(this.model.get('paymentList').at(this.model.get('stepOfStep3')));

    this.model.on('change:stepOfStep3', function(model) {
      this.$.cashToKeep.disableSelection();
      this.$.cashToKeep.setPaymentToKeep(this.model.get('paymentList').at(this.model.get('stepOfStep3')));
      this.refresh();
    }, this);

    // Cash Up Report - Step 4
    //this data doesn't changes
    this.$.postPrintClose.setModel(this.model.get('cashUpReport').at(0));

    //This data changed when money is counted
    //difference is calculated after counted
    this.$.postPrintClose.setSummary(this.model.getCountCashSummary());
    this.model.on('change:totalDifference', function(model) {
      this.$.postPrintClose.setSummary(this.model.getCountCashSummary());
    }, this);

    //finished
    this.model.on('change:finished', function() {
      $('#modalFinished').modal('show');
    }, this);

    this.refresh();
  },
  refresh: function() {
    this.$.listPendingReceipts.setShowing(this.model.showPendingOrdersList());
    this.$.listPaymentMethods.setShowing(this.model.showPaymentMethodList());
    this.$.cashToKeep.setShowing(this.model.showCashToKeep());
    this.$.postPrintClose.setShowing(this.model.showPostPrintClose());
    this.$.cashUpKeyboard.showToolbar(this.model.showPaymentMethodList() ? 'toolbarcountcash' : 'toolbarempty');

    this.$.cashUpInfo.refresh();
  },
  changeStep: function(inSender, inEvent) {
    var nextStep;
    if (this.model.get('step') === 4 && inEvent.originator.stepCount > 0) {
      //send cash up to the server
      this.model.processAndFinishCashUp();
    } else {
      if (this.model.get('step') !== 3) {
        this.model.set('step', this.model.get('step') + inEvent.originator.stepCount);
        //if the new step is 3 we should set the substep number
        if (this.model.get('step') === 3) {
          if (inEvent.originator.stepCount > 0) {
            //we come from step 2
            this.model.set('stepOfStep3', 0);
          } else {
            //we come from step 4
            //because the last stepOfStep3 was the same that im setting the event is not raised
            this.model.set('stepOfStep3', this.model.get('paymentList').length - 1);
            //raise the event
            this.model.trigger("change:stepOfStep3");
          }
        }
      } else {
        nextStep = this.model.get('stepOfStep3') + inEvent.originator.stepCount;
        //if the new step is 2 or 4 we should set the step number
        if (nextStep < 0 || nextStep > this.model.get('paymentList').length - 1) {
          //change the step and not change the substep
          this.model.set('step', this.model.get('step') + inEvent.originator.stepCount);
        } else {
          //change the substep, not the step
          this.model.set('stepOfStep3', nextStep);
        }
      }
    }
  },
  countAllOK: function(inSender, inEvent) {
    this.model.countAll();
    this.$.cashUpInfo.refresh();
  },
  lineEditCount: function(sender, event) {
    this.$.cashUpKeyboard.setStatus(event.originator.model.get('_id'));
  },
  paymentMethodKept: function(inSender, event) {
    var validationResult = this.model.validateCashKeep(event.qtyToKeep)
    if (validationResult.result) {
      this.model.get('paymentList').at(this.model.get('stepOfStep3')).set('qtyToKeep', event.qtyToKeep);
    } else {
      OB.UTIL.showWarning(validationResult.message);
    }
    this.$.cashUpInfo.refresh();
  },
  resetQtyToKeep: function(inSender, event) {
    this.model.get('paymentList').at(this.model.get('stepOfStep3')).set('qtyToKeep', null);
    this.$.cashUpInfo.refresh();
  }
});

OB.POS.registerWindow({
  windowClass: OB.OBPOSCashUp.UI.CashUp,
  route: 'retail.cashup',
  menuPosition: 20,
  menuLabel: OB.I18N.getLabel('OBPOS_LblCloseCash')
});