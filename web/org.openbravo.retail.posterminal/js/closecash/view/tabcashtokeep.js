/*global enyo */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
  kind: 'OB.UI.RadioButton',
  events: {
    onTapRadio: ''
  },
  tap: function () {
    this.doTapRadio();
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashToKeep',
  components: [{
    classes: 'tab-pane',
    components: [{
      style: 'overflow:auto; height: 500px; margin: 5px',
      components: [{
        style: 'background-color: #ffffff; color: black; padding: 5px;',
        components: [{
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components: [{
              //FIXME: Remove
              id: 'cashtokeepheader',
              style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;'
            }]
          }]
        }, {
          style: 'background-color: #ffffff; color: black;',
          components: [{
            name: 'RadioGroup',
            classes: 'btn-group',
            attributes: {
              'data-toggle': 'buttons-radio'
            },
            components: [{
              name: 'keepfixedamount',
              kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton'
            }, {
              style: 'clear: both;'
            }, {
              name: 'allowmoveeverything',
              kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton'
            }, {
              style: 'clear: both;'
            }, {
              name: 'allowdontmove',
              kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton'
            }, {
              style: 'clear: both;'
            }, {
              name: 'allowvariableamount'
            }, {
              name: 'variableamount',
              kind: 'enyo.Input',
              type: 'text',
              classes: 'span1',
              style: 'display: table-cell; vertical-align: middle; margin: 0px 0px 0px 10px;'
            }]
          }]
        }]
      }]
    }]
  }]
});