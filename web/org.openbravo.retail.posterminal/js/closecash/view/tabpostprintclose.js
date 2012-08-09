/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, B, Backbone */

//Renders the summary of deposits/drops and contains a list (OB.OBPOSCasgMgmt.UI.RenderDepositsDrops)
//with detailed information for each payment type
enyo.kind({
  name: 'OB.OBPOSCashUp.UI.PostPrintClose',
  components: [ {
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
              style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
              content: OB.I18N.getLabel('OBPOS_LblStep4of4')
            }]
          }]
        },
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            style: 'border-bottom: 1px solid #cccccc;',
            components:[{
              style: 'padding: 10px; text-align:center;',
              components:[{
                tag: 'img',
                style: 'padding: 20px 20px 20px 10px;',
                attributes: {src:'../../utility/ShowImageLogo?logo=yourcompanymenu'}
              },
              {
                style: 'padding: 5px; text-align:center;',
                content: 'User'
              },
              {
                style: 'padding: 5px 5px 15px 5px; text-align:center;',
                content: new Date().toString().substring(16, 21)
              }]
            }]
          }]
        }]
      }]
    }]
  }],
  create: function () {
    this.inherited(arguments);
    // explicitly set the total
//    this.$.totalLbl.setContent(OB.I18N.getLabel('OBPOS_ReceiptTotal'));
//    this.$.userName.setContent(OB.I18N.getLabel('OBPOS_LblUser') + ': ' + OB.POS.modelterminal.get('context').user._identifier);
//    this.$.time.setContent(OB.I18N.getLabel('OBPOS_LblTime') + ': ' + new Date().toString().substring(3, 24));
//    this.$.store.setContent(OB.I18N.getLabel('OBPOS_LblStore') + ': ' + OB.POS.modelterminal.get('terminal').organization$_identifier);
//    this.$.terminal.setContent(OB.I18N.getLabel('OBPOS_LblTerminal') + ': ' + OB.POS.modelterminal.get('terminal')._identifier);
//
//    this.$.paymentsList.setCollection(this.owner.model && this.owner.model.getData('DataCloseCashPaymentMethod'));
  },
  init: function () {
    // this.owner is the window (OB.UI.WindowView)
    // this.parent is the DOM object on top of the list (usually a DIV)
//    this.$.paymentsList.setCollection( this.owner.model.getData('DataCloseCashPaymentMethod'));
  }
});


(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.PostPrintClose = OB.COMP.CustomView.extend({
  _id: 'postprintclose',
    createView: function () {
      return (
        {kind: B.KindJQuery('div'), attr: {'id': 'postprintclose', 'class': 'tab-pane'}, content: [
          {kind: B.KindJQuery('div'), attr: {'style': 'overflow:auto; height: 500px; margin: 5px'}, content: [
            {kind: B.KindJQuery('div'), attr: {'style': 'background-color: #ffffff; color: black; padding: 5px;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;'}, content: [
                      OB.I18N.getLabel('OBPOS_LblStep4of4')
                  ]}
                ]}
              ]},
              {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                  {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                    {kind: B.KindJQuery('div'), attr: {'style': 'padding: 10px; text-align:center;'}, content: [
                       {kind: B.KindJQuery('img'), attr: {'style': 'padding: 20px 20px 20px 10px;', 'src':'../../utility/ShowImageLogo?logo=yourcompanymenu'}, content:[]},
                       {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px; text-align:center;'}, content:[
                             OB.I18N.getLabel('OBPOS_LblUser')+': '+OB.POS.modelterminal.get('context').user._identifier
                         ]},
                         {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 5px 15px 5px; text-align:center;', 'id':'reportTime'}, content:[
                            OB.I18N.getLabel('OBPOS_LblTime')+': '+ new Date().toString().substring(3,24)
                        ]}
                     ]}
                  ]}
                ]},
               {kind: OB.COMP.SearchRetailTransactions},
               {kind: OB.COMP.RenderPaymentLines},
               {kind: B.KindJQuery('div'), attr: {'class': 'row-fluid'}, content: [
                 {kind: B.KindJQuery('div'), attr: {'class': 'span12'}, content: [
                   {kind: B.KindJQuery('div'), attr: {'style': 'width: 10%; float: left'}, content: [{kind: B.KindHTML('<span>&nbsp;</span>')}]},
                   {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px 0px 0px 5px; float: left; width: 60%; font-weight:bold;'}, content: [
                {kind: B.KindJQuery('div')}
                ]},    {kind: B.KindJQuery('div'), attr: {style: 'clear: both'}}
                   ]}
               ]}
          ]}
        ]}
       ]}
      );
    }
  });
}());