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
  published: {
    model: null
  },
  components: [{
    style: 'position: relative; background: #363636; color: white; height: 200px; margin: 5px; padding: 5px',
    components: [{ //clock here
      kind: 'OB.UI.Clock',
      classes: 'pos-clock'
    }, {
      // process info
      style: 'padding: 5px',
      initialize: function(){
        this.setContent(OB.I18N.getLabel('OBPOS_LblCashUpProcess'));
      }
    }, {
      style: 'padding: 3px',
      initialize: function(){
        this.setContent(OB.I18N.getLabel('OBPOS_LblStep1'));
      }
    }, {
      style: 'padding: 3px',
      initialize: function(){
        this.setContent(OB.I18N.getLabel('OBPOS_LblStep2'));
      }
    }, {
      style: 'padding: 3px',
      initialize: function(){
        this.setContent(OB.I18N.getLabel('OBPOS_LblStep3'));
      }
    }, {
      style: 'padding: 3px',
      initialize: function(){
        this.setContent(OB.I18N.getLabel('OBPOS_LblStep4'));
      }
    }]
  }]
});