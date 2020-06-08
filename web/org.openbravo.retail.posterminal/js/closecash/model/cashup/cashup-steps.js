/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.CashUp.StepPendingOrders',
  kind: enyo.Object,
  classes: 'obCashUpStepPendingOrders',
  getStepComponent: function(leftpanel$) {
    return leftpanel$.listPendingReceipts;
  },
  getToolbarName: function() {
    return 'toolbarempty';
  },
  nextFinishButton: function() {
    return false;
  },
  allowNext: function() {
    return (
      this.model.get('orderlist').length === 0 &&
      !this.model.get('pendingOrdersToProcess')
    );
  },
  getSubstepsLength: function(model) {
    return 1;
  },
  isSubstepAvailable: function(model, substep) {
    return true;
  }
});

enyo.kind({
  name: 'OB.CashUp.Master',
  kind: enyo.Object,
  classes: 'obCashUpMaster',
  getStepComponent: function(leftpanel$) {
    return leftpanel$.cashMaster;
  },
  getToolbarName: function() {
    return 'toolbarempty';
  },
  nextFinishButton: function() {
    return false;
  },
  allowNext: function() {
    return this.model.get('slavesCashupCompleted');
  },
  getSubstepsLength: function(model) {
    return 1;
  },
  isSubstepAvailable: function(model, substep) {
    return true;
  }
});
