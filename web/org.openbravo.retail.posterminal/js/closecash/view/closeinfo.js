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
  name: 'OB.OBPOSCashUp.UI.Button',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-fontgray',
  style: 'min-width: 115px;',
  disabled: true,
  events: {
    onChangeStep: ''
  },
  tap: function () {
    this.doChangeStep();
  }
});

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
      style: 'padding: 5px; float: right;',
      components: [{
        kind: 'OB.UI.CancelButton'
      }]
    }, {
      // process info
      style: 'padding: 5px',
      content: OB.I18N.getLabel('OBPOS_LblCashUpProcess')
    }, {
      // Prev and Next buttons
      style: 'padding: 5px;',
      components: [{
        kind: 'OB.OBPOSCashUp.UI.Button',
        name: 'btnPrevious',
        content: OB.I18N.getLabel('OBPOS_LblPrevStep'),
        stepCount: -1
      }, {
        kind: 'OB.OBPOSCashUp.UI.Button',
        name: 'btnNext',
        content: OB.I18N.getLabel('OBPOS_LblNextStep'),
        stepCount: 1
      }]
    }, {
      style: 'padding: 3px',
      content: OB.I18N.getLabel('OBPOS_LblStep1')
    }, {
      style: 'padding: 3px',
      content: OB.I18N.getLabel('OBPOS_LblStep2')
    }, {
      style: 'padding: 3px',
      content: OB.I18N.getLabel('OBPOS_LblStep3')
    }, {
      style: 'padding: 3px',
      content: OB.I18N.getLabel('OBPOS_LblStep4')
    }]
  }],
  refresh: function () {
    this.$.btnPrevious.setDisabled(!this.model.allowPrevious());
    this.$.btnNext.setDisabled(!this.model.allowNext());
    //Normaly the button shows Next
    this.$.btnNext.setContent(OB.I18N.getLabel('OBPOS_LblNextStep'));
    if (this.model.get('step') === 4) {
      //in the last step the button shows another label
      this.$.btnNext.setContent(OB.I18N.getLabel('OBPOS_LblPostPrintClose'));
    }
    //Sometimes the button is shown with over css class.
    if (this.$.btnNext.hasClass('btn-over')) {
      this.$.btnNext.removeClass('btn-over');
    }
    if (this.$.btnPrevious.hasClass('btn-over')) {
      this.$.btnPrevious.removeClass('btn-over');
    }
  }
});