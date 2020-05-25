/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */

enyo.kind({
  name: 'OB.CloseCash.CashPayments',
  kind: enyo.Object,
  classes: 'obCloseCashCashPayments',
  getStepComponent: function(leftpanel$) {
    return leftpanel$.cashPayments;
  },
  getToolbarName: function() {
    return 'toolbarcashpayments';
  },
  nextFinishButton: function() {
    return false;
  },
  allowNext: function() {
    return true;
  },
  getSubstepsLength: function(model) {
    return model.get('paymentList').length;
  },
  isSubstepAvailable: function(model, substep) {
    const payment = model.get('paymentList').at(substep),
      paymentMethod = payment.get('paymentMethod');
    return paymentMethod.iscash && paymentMethod.countcash;
  }
});

enyo.kind({
  name: 'OB.CloseCash.PaymentMethods',
  kind: enyo.Object,
  classes: 'obCloseCashPaymentMethods',
  getStepComponent: function(leftpanel$) {
    return leftpanel$.listPaymentMethods;
  },
  getToolbarName: function() {
    return 'toolbarcountcash';
  },
  nextFinishButton: function() {
    return false;
  },
  allowNext: function() {
    const paymentList = this.model.get(
      OB.MobileApp.model.hasPermission(
        'OBPOS_retail.cashupGroupExpectedPayment',
        true
      )
        ? 'paymentExpectedList'
        : 'paymentList'
    );
    return paymentList.models.reduce((allCounted, model) => {
      return (
        allCounted &&
        model.get('counted') !== null &&
        model.get('counted') !== undefined
      );
    }, true);
  },
  getSubstepsLength: function(model) {
    // Do not show this step if there are no payments defined
    return this.model.get('paymentList').length > 0 ? 1 : 0;
  },
  isSubstepAvailable: function(model, substep) {
    return true;
  }
});

enyo.kind({
  name: 'OB.CloseCash.CashToKeep',
  kind: enyo.Object,
  classes: 'obCloseCashCashToKeep',
  getStepComponent: function(leftpanel$) {
    return leftpanel$.cashToKeep;
  },
  getToolbarName: function() {
    if (
      this.model
        .get('paymentList')
        .at(this.model.get('substep'))
        .get('paymentMethod').allowvariableamount
    ) {
      return 'toolbarother';
    }
    return 'toolbarempty';
  },
  nextFinishButton: function() {
    return false;
  },
  allowNext: function() {
    const paymentMethod = this.model
        .get('paymentList')
        .at(this.model.get('substep')),
      cashToKeepSelected = paymentMethod.get('isCashToKeepSelected') || false,
      qtyToKeep = paymentMethod.get('qtyToKeep'),
      foreignCounted = paymentMethod.get('foreignCounted');
    return (
      cashToKeepSelected && _.isNumber(qtyToKeep) && foreignCounted >= qtyToKeep
    );
  },
  getSubstepsLength: function(model) {
    return model.get('paymentList').length;
  },
  isSubstepAvailable: function(model, substep) {
    const payment = model.get('paymentList').at(substep),
      paymentMethod = payment.get('paymentMethod');
    let options = 0;

    payment.set('isCashToKeepSelected', false);
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
        if (
          _.isNumber(payment.get('foreignCounted')) &&
          payment.get('foreignCounted') < paymentMethod.amount
        ) {
          payment.set('qtyToKeep', payment.get('foreignCounted'));
        } else {
          payment.set('qtyToKeep', paymentMethod.amount);
        }
        options++;
      }

      // if there is there is more than one option or allowvariableamount exists. then show the substep
      return options > 1 || paymentMethod.allowvariableamount;
    }
    return false;
  }
});

enyo.kind({
  name: 'OB.CloseCash.PostPrintAndClose',
  kind: enyo.Object,
  classes: 'obCloseCashPostPrintAndClose',
  getStepComponent: function(leftpanel$) {
    return leftpanel$.postPrintClose;
  },
  getToolbarName: function() {
    return 'toolbarempty';
  },
  nextFinishButton: function() {
    return true;
  },
  allowNext: function() {
    return true;
  },
  getSubstepsLength: function(model) {
    return 1;
  },
  isSubstepAvailable: function(model, substep) {
    return true;
  }
});
