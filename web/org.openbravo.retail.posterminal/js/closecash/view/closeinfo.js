/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, */


enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashUpInfo',
  classes: 'obObposCashupUiCashupInfo',
  published: {
    model: null
  },
  components: [{
    classes: 'obObposCashupUiCashupInfo-container1',
    components: [{ //clock here
      kind: 'OB.UI.Clock',
      classes: 'obObposCashupUiCashupInfo-container1-obUiClock'
    }, {
      // process info
      classes: 'obObposCashupUiCashupInfo-container1-element1',
      initialize: function () {
        this.setContent(OB.I18N.getLabel('OBPOS_LblCashUpProcess'));
      }
    }, {
      classes: 'obObposCashupUiCashupInfo-container1-element2',
      initialize: function () {
        this.setContent(OB.I18N.getLabel('OBPOS_LblStep1'));
      }
    }, {
      classes: 'obObposCashupUiCashupInfo-container1-element3',
      initialize: function () {
        this.setContent(OB.I18N.getLabel('OBPOS_LblStep2'));
      }
    }, {
      classes: 'obObposCashupUiCashupInfo-container1-element4',
      initialize: function () {
        this.setContent(OB.I18N.getLabel('OBPOS_LblStep3'));
      }
    }, {
      classes: 'obObposCashupUiCashupInfo-container1-element5',
      initialize: function () {
        this.setContent(OB.I18N.getLabel('OBPOS_LblStep4'));
      }
    }]
  }]
});