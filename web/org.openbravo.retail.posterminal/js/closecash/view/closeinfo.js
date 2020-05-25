/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, */

enyo.kind({
  name: 'OB.OBPOSCloseCash.UI.CloseCashInfo',
  classes: 'obObposCloseCashUiCloseCashInfo',
  published: {
    model: null
  },
  components: [
    {
      classes: 'obObposCloseCashUiCloseCashInfo-wrapper',
      components: [
        {
          classes: 'obObposCloseCashUiCloseCashInfo-wrapper-components',
          components: [
            {
              //clock here
              kind: 'OB.UI.Clock',
              classes:
                'obObposCloseCashUiCloseCashInfo-wrapper-components-obUiClock'
            },
            {
              // process info
              classes:
                'obObposCloseCashUiCloseCashInfo-wrapper-components-element1',
              initialize: function() {
                this.setContent(OB.I18N.getLabel('OBPOS_LblCashUpProcess'));
              }
            },
            {
              classes:
                'obObposCloseCashUiCloseCashInfo-wrapper-components-element2',
              initialize: function() {
                this.setContent(OB.I18N.getLabel('OBPOS_LblStep1'));
              }
            },
            {
              classes:
                'obObposCloseCashUiCloseCashInfo-wrapper-components-element3',
              initialize: function() {
                this.setContent(OB.I18N.getLabel('OBPOS_LblStep2'));
              }
            },
            {
              classes:
                'obObposCloseCashUiCloseCashInfo-wrapper-components-element4',
              initialize: function() {
                this.setContent(OB.I18N.getLabel('OBPOS_LblStep3'));
              }
            },
            {
              classes:
                'obObposCloseCashUiCloseCashInfo-wrapper-components-element5',
              initialize: function() {
                this.setContent(OB.I18N.getLabel('OBPOS_LblStep4'));
              }
            }
          ]
        }
      ]
    }
  ]
});
