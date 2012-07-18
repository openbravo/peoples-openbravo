/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

//  var sdf = new SimpleDateFormat("HH:mm:ss");
//  document.write(sdf.format(new Date()));

  OB.COMP.CashToKeep = OB.COMP.CustomView.extend({
  _id: 'cashtokeep',
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'countcash', 'class': 'tab-pane'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'id': 'cashtokeepheader', 'style': 'padding: 10px; border-bottom: 1px solid #cccccc;text-align:center;'}, content: [
                  ]}
                ]}
              ]},
                 {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black;'}, content: [
                     {kind: B.KindJQuery('div'), attr: {'class': 'btn-group','data-toggle':'buttons-radio'}, content: [
                        {kind: OB.COMP.CashToKeepRadioButton, attr: {'id': 'keepfixedamount'}, content: [
                          {kind: B.KindJQuery('div'), attr: {'id': 'keepfixedamountlbl' }, content: []}
                        ]},
                        {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}},

                        {kind: OB.COMP.CashToKeepRadioButton, attr: {'id': 'allowmoveeverything'}, content: [
                          {kind: B.KindJQuery('div'), attr: {'id': 'allowmoveeverythinglbl' }, content: []}
                        ]},
                        {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}},

                        {kind: OB.COMP.CashToKeepRadioButton, attr: {'id': 'allowdontmove'}, content: [
                          {kind: B.KindJQuery('div'), attr: {'id': 'allowdontmovelbl' }, content: []}
                        ]},
                        {kind: B.KindJQuery('div'), attr: {style: 'clear: both;'}},

                        {kind: OB.COMP.CashToKeepRadioButton, attr: {'id': 'allowvariableamount'}, content: [
                          {kind: B.KindJQuery('div'), attr: {'style': 'display: table-row;'}, content: [
                            {kind: B.KindJQuery('div'), attr: {'id': 'allowvariableamountlbl', 'style': 'display: table-cell; vertical-align: middle;'}, content: []},
                            {kind: B.KindJQuery('input'), attr: {'type':'text', 'class': 'span1', 'id': 'variableamount', 'style': 'display: table-cell; vertical-align: middle; margin: 0px 0px 0px 10px;'}, content: []}
                          ]}
                        ]}
                     ]}
                  ]}
            ]}
          ]}
        ]}
      );
    }
  });

}());