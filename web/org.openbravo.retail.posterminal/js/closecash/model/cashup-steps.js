/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, _, $ */

enyo.kind({
  name: 'OB.CashUp.StepPendingOrders',
  kind: enyo.Object,
  getStepComponent: function (leftpanel$) {
    return leftpanel$.listPendingReceipts;
  },
  getToolbarName: function () {
    return 'toolbarempty';
  },
  nextButtonI18NLabel: function () {
    return 'OBPOS_LblNextStep';
  },
  allowNext: function () {
    return this.model.get('orderlist').length === 0 && !this.model.get('pendingOrdersToProcess');
  },
  getSubstepsLength: function (model) {
    return 1;
  },
  isSubstepAvailable: function (model, substep) {
    return true;
  }
});

enyo.kind({
  name: 'OB.CashUp.CashPayments',
  kind: enyo.Object,
  getStepComponent: function (leftpanel$) {
    return leftpanel$.cashPayments;
  },
  getToolbarName: function () {
    return 'toolbarcashpayments';
  },
  nextButtonI18NLabel: function () {
    return 'OBPOS_LblNextStep';
  },
  allowNext: function () {
    return true;
  },
  getSubstepsLength: function (model) {
    return model.get('paymentList').length;
  },
  isSubstepAvailable: function (model, substep) {
    var payment = model.get('paymentList').at(substep);
    var paymentMethod = payment.get('paymentMethod');
    return paymentMethod.iscash;
  }
});

enyo.kind({
  name: 'OB.CashUp.PaymentMethods',
  kind: enyo.Object,
  getStepComponent: function (leftpanel$) {
    return leftpanel$.listPaymentMethods;
  },
  getToolbarName: function () {
    return 'toolbarcountcash';
  },
  nextButtonI18NLabel: function () {
    return 'OBPOS_LblNextStep';
  },
  allowNext: function () {
    return _.reduce(this.model.get('paymentList').models, function (allCounted, model) {
      return allCounted && model.get('counted') !== null && model.get('counted') !== undefined;
    }, true);
  },
  getSubstepsLength: function (model) {
    return 1;
  },
  isSubstepAvailable: function (model, substep) {
    return true;
  }
});

enyo.kind({
  name: 'OB.CashUp.CashToKeep',
  kind: enyo.Object,
  getStepComponent: function (leftpanel$) {
    return leftpanel$.cashToKeep;
  },
  getToolbarName: function () {
    if (this.model.get('paymentList').at(this.model.get('substep')).get('paymentMethod').allowvariableamount) {
      return 'toolbarother';
    } else {
      return 'toolbarempty';
    }
  },
  nextButtonI18NLabel: function () {
    return 'OBPOS_LblNextStep';
  },
  allowNext: function () {
    var qtyToKeep = this.model.get('paymentList').at(this.model.get('substep')).get('qtyToKeep');
    var foreignCounted = this.model.get('paymentList').at(this.model.get('substep')).get('foreignCounted');
    return _.isNumber(qtyToKeep) && foreignCounted >= qtyToKeep;
  },
  getSubstepsLength: function (model) {
    return model.get('paymentList').length;
  },
  isSubstepAvailable: function (model, substep) {
    var payment = model.get('paymentList').at(substep);
    var paymentMethod = payment.get('paymentMethod');
    var options = 0;

    if (paymentMethod.automatemovementtoother) {

      // Option 1
      if (paymentMethod.allowmoveeverything) {
        payment.set('qtyToKeep', 0);
        options++;
      }

      // Option 2
      if (paymentMethod.allowdontmove) {
        payment.set('qtyToKeep', payment.get('foreignCounted'));
        options++;
      }

      // Option 3
      if (paymentMethod.keepfixedamount) {
        if (_.isNumber(payment.get('foreignCounted')) && payment.get('foreignCounted') < paymentMethod.amount) {
          payment.set('qtyToKeep', payment.get('foreignCounted'));
        } else {
          payment.set('qtyToKeep', paymentMethod.amount);
        }
        options++;
      }

      // if there is there is more than one option or allowvariableamount exists. then show the substep
      return (options > 1 || paymentMethod.allowvariableamount);
    } else {
      return false;
    }
  }
});

enyo.kind({
  name: 'OB.CashUp.PostPrintAndClose',
  kind: enyo.Object,
  getStepComponent: function (leftpanel$) {
    return leftpanel$.postPrintClose;
  },
  getToolbarName: function () {
    return 'toolbarempty';
  },
  nextButtonI18NLabel: function () {
    return 'OBPOS_LblPostPrintClose';
  },
  allowNext: function () {
    this.model.get('cashUpReport').at(0).set('time', new Date());
    return true;
  },
  getSubstepsLength: function (model) {
    return 1;
  },
  isSubstepAvailable: function (model, substep) {
    return true;
  }
});