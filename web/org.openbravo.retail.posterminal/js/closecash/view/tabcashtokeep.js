/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, B, Backbone */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
  kind: 'OB.UI.RadioButton',
  events: {
    onTapRadio: ''
  },
  tap: function() {
    this.doTapRadio();
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashToKeep',
  components: [{
    classes: 'tab-pane',
    components: [ {
      style: 'overflow:auto; height: 500px; margin: 5px',
      components: [ {
        style: 'background-color: #ffffff; color: black; padding: 5px;',
        components: [ {
          classes: 'row-fluid',
          components: [ {
            classes: 'span12',
            components: [ {
              id: 'cashtokeepheader',
              style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
            }]
          }]
        },
        {
          style: 'background-color: #ffffff; color: black;',
          components: [{
            name: 'RadioGroup',
            attributes: {classes: 'btn-group','data-toggle':'buttons-radio'},
            components: [{
              name: 'keepfixedamount',
              value: '',
              kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
            },
            {style: 'clear: both;'},
            {
              name: 'allowmoveeverything',
              value: '',
              kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
            },
            {style: 'clear: both;'},
            {
              name: 'allowdontmove',
              value: '',
              kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
            },
            {style: 'clear: both;'},
            {
              name: 'allowvariableamount',
              value: '',
              kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
              components: [{
                style: 'display: table-cell; vertical-align: middle;'
              },{
                name: 'variableamount',
                tag: 'input',
                type:'text',
                classes: 'span1',
                style: 'display: table-cell; vertical-align: middle; margin: 0px 0px 0px 10px;'
              }]
            }]
          }]
        }]
      }]
    }]
  }]
});

//(function () {
//
//  OB = window.OB || {};
//  OB.COMP = window.OB.COMP || {};
//
////  var sdf = new SimpleDateFormat("HH:mm:ss");
////  document.write(sdf.format(new Date()));
//
//  OB.COMP.CashToKeep = OB.COMP.CustomView.extend({
//  _id: 'cashtokeep',
//    createView: function () {
//      return (
//        {kind: B.KindJQuery('div'), attr: {'id': 'countcash', 'class': 'tab-pane'}, content: [
//          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
//            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
//              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
//                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
//                  {kind: B.KindJQuery('div'), attr: {'id': 'cashtokeepheader', 'style': 'padding: 10px; border-bottom: 1px solid #cccccc;text-align:center;'}, content: [
//                  ]}
//                ]}
//              ]},
//                 {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black;'}, content: [
//                     {kind: B.KindJQuery('div'), attr: {'class': 'btn-group','data-toggle':'buttons-radio'}, content: [
//                        {kind: OB.COMP.CashToKeepRadioButton, attr: {'id': 'keepfixedamount'}, content: [
//                          {kind: B.KindJQuery('div'), attr: {'id': 'keepfixedamountlbl' }, content: []}
//                        ]},
//                        {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}},
//
//                        {kind: OB.COMP.CashToKeepRadioButton, attr: {'id': 'allowmoveeverything'}, content: [
//                          {kind: B.KindJQuery('div'), attr: {'id': 'allowmoveeverythinglbl' }, content: []}
//                        ]},
//                        {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}},
//
//                        {kind: OB.COMP.CashToKeepRadioButton, attr: {'id': 'allowdontmove'}, content: [
//                          {kind: B.KindJQuery('div'), attr: {'id': 'allowdontmovelbl' }, content: []}
//                        ]},
//                        {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}},
//
//                        {kind: OB.COMP.CashToKeepRadioButton, attr: {'id': 'allowvariableamount'}, content: [
//                          {kind: B.KindJQuery('div'), attr: {'style': 'display: table-row;'}, content: [
//                            {kind: B.KindJQuery('div'), attr: {'id': 'allowvariableamountlbl', 'style': 'display: table-cell; vertical-align: middle;'}, content: []},
//                            {kind: B.KindJQuery('input'), attr: {'type':'text', 'class': 'span1', 'id': 'variableamount', 'style': 'display: table-cell; vertical-align: middle; margin: 0px 0px 0px 10px;'}, content: []}
//                          ]}
//                        ]}
//                     ]}
//                  ]}
//            ]}
//          ]}
//        ]}
//      );
//    }
//  });
//
//}());