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
            components:[{
              style: 'padding: 10px; text-align:center;',
              components:[{
                tag: 'img',
                style: 'padding: 20px 20px 20px 10px;',
                attributes: {src:'../../utility/ShowImageLogo?logo=yourcompanymenu'}
              },
              {
                style: 'padding: 5px; text-align:center;',
                name: 'user'
              },
              {
                name: 'time',
                style: 'padding: 5px 5px 15px 5px; text-align:center;',
              }]
            }]
          }]
        },
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components:[{
              style: 'width: 10%; float: left; clear: both;'},
              {
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%',
                content: OB.I18N.getLabel('OBPOS_LblNetSales')
              },
              {
                name: 'netSales',
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;',
              },
              {
                style: 'width: 10%; float: left;  clear: both;',
              }]
            }]
        },
        //FIXME: Iterate taxes
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components:[{
              style: 'width: 10%; float: left; clear: both;'},
              {
                name: 'taxSalesName',
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%',
                content: 'Entregas IVA 18% * 125.00'
              },
              {
                name: 'taxSalesAmount',
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;',
                content: '125.00'
              },
              {
                style: 'width: 10%; float: left;  clear: both;',
              }]
            }]
        },
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components:[{
              style: 'width: 10%; float: left; clear: both;'},
              {
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;',
                content: OB.I18N.getLabel('OBPOS_LblGrossSales')
              },
              {
                name: 'grossSales',
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;',
              },
              {
                style: 'width: 10%; float: left;  clear: both;',
              }]
            }]
        },
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components:[{
              style: 'width: 10%; float: left; clear: both;'},
              {
                style: 'padding: 10px 0px 10px 5px;  border-top: 1px solid #cccccc;  float: left; width: 60%',
                content: ''
              },
              {
                style: 'padding: 10px 0px 10px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;',
                content: ''
              },
              {
                style: 'width: 10%; float: left;  clear: both;',
              }]
            }]
        },
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components:[{
              style: 'width: 10%; float: left; clear: both;'},
              {
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%',
                content: OB.I18N.getLabel('OBPOS_LblNetReturns')
              },
              {
                name: 'netReturns',
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;',
              },
              {
                style: 'width: 10%; float: left;  clear: both;',
              }]
            }]
        },
        //FIXME: Iterate taxes
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components:[{
              style: 'width: 10%; float: left; clear: both;'},
              {
                name: 'taxReturnsName',
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%',
                content: 'Entregas IVA 18% * 125.00'
              },
              {
                name: 'taxReturnsAmount',
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;',
                content: '125.00'
              },
              {
                style: 'width: 10%; float: left;  clear: both;',
              }]
            }]
        },
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components:[{
              style: 'width: 10%; float: left; clear: both;'},
              {
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold; ',
                content: OB.I18N.getLabel('OBPOS_LblGrossReturns')
              },
              {
                name: 'grossReturns',
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;  font-weight:bold;',
              },
              {
                style: 'width: 10%; float: left;  clear: both;',
              }]
            }]
        },
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components:[{
              style: 'width: 10%; float: left; clear: both;'},
              {
                style: 'padding: 10px 0px 10px 5px;  border-top: 1px solid #cccccc;  float: left; width: 60%',
                content: ''
              },
              {
                style: 'padding: 10px 0px 10px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right;',
                content: ''
              },
              {
                style: 'width: 10%; float: left;  clear: both;',
              }]
            }]
        },
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            components:[{
              style: 'width: 10%; float: left; clear: both;'},
              {
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; border-right: 1px solid #cccccc; float: left; width: 60%; font-weight:bold;',
                content: OB.I18N.getLabel('OBPOS_LblTotalRetailTrans')
              },
              {
                name: 'totalRetailTransacntions',
                style: 'padding: 5px 0px 0px 5px;  border-top: 1px solid #cccccc; float: left; width: 15%; text-align:right; font-weight:bold;',
              },
              {
                style: 'width: 10%; float: left;  clear: both;',
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
    this.$.user.setContent(OB.I18N.getLabel('OBPOS_LblUser') + ': ' + OB.POS.modelterminal.get('context').user._identifier);
    this.$.time.setContent(OB.I18N.getLabel('OBPOS_LblTime') + ': ' + new Date().toString().substring(3, 24));
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

