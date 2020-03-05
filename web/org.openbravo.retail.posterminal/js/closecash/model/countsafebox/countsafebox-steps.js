/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.CountSafeBox.StepSafeBoxList',
  kind: enyo.Object,
  getStepComponent: function(leftpanel$) {
    return leftpanel$.listSafeBoxes;
  },
  getToolbarName: function() {
    return 'toolbarempty';
  },
  nextFinishButton: function() {
    return false;
  },
  allowNext: function() {
    return !OB.UTIL.isNullOrUndefined(
      OB.UTIL.localStorage.getItem('currentSafeBox')
    );
  },
  getSubstepsLength: function(model) {
    return 1;
  },
  isSubstepAvailable: function(model, substep) {
    return true;
  }
});
