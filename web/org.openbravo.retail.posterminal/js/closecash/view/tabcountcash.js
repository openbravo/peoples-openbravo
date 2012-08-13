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
  name: 'OB.OBPOSCashUp.UI.ButtonOk',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-green btnlink-cashup-ok btn-icon-small btn-icon-check',
  events: {
    onButtonOk:''
  },
  tap: function() {
    this.doButtonOk();
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ButtonEdit',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-orange btnlink-cashup-ok btn-icon-small btn-icon-edit',
  tap: function() {
    //FIXME: Use events and handlers
    this.owner.owner.owner.owner.owner.owner.$.cashUpKeyboard.doCommandFired({key: this.type});
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderPaymentsLine',
  components: [{
    classes: 'display: table-row; height: 42px;',
      components: [{
        name: 'name',
        style: 'padding: 10px 20px 10px 10px; float: left; width: 10%'
      }, {
        name: 'expected',
        style: 'padding: 10px 20px 10px 10px; float: left; width: 20%'
      },
      {
        style: 'float: left;',
        components:[{
           name: 'buttonEdit',
           kind:'OB.OBPOSCashUp.UI.ButtonEdit',
           type: '',
           attributes:{'button':'editbutton'}
         }]
      },
      {
        style: 'float: left;',
        components:[{
           name: 'buttonOk',
           kind:'OB.OBPOSCashUp.UI.ButtonOk',
           type: '',
           attributes:{'button':'okbutton'}
         }]
      },
      {
        name: 'counted',
        style: 'display: none;float: left; padding: 10px 0px 10px 10px;'
      }
  ]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.name.setContent(this.model.get('name'));
    this.$.expected.setContent(OB.I18N.formatCurrency(OB.DEC.add(0,this.model.get('expected'))));
    this.$.buttonEdit.type = this.model.get('id');
    this.$.buttonOk.type = this.model.get('id');
//    this.$.counted.setContent(0);
    //FIXME: Use events and handlers
//    this.owner.owner.owner.owner.owner.model.set('totalExpected',OB.DEC.add(this.owner.owner.owner.owner.owner.model.get('totalExpected'),this.model.get('expected')));
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderTotal',
  tag: 'span',
  published: {
    total: OB.DEC.Zero
  },
  create: function () {
    this.inherited(arguments);
  },
  totalChanged: function (oldValue) {
    this.setContent(this.total);
    if (OB.DEC.compare(this.total) < 0) {
      this.applyStyle('color', 'red');
    } else {
      this.applyStyle('color', 'black');
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ListPaymentMethods',
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
              content: OB.I18N.getLabel('OBPOS_LblStep2of4')
            }]
          }]
        },
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            style: 'border-bottom: 1px solid #cccccc;',
            components:[{
              style: 'padding: 10px 20px 10px 10px; float: left; width: 20%',
              content: OB.I18N.getLabel('OBPOS_LblPaymentMethod')
            }, {
              style: 'padding: 10px 20px 10px 10px; float: left; width: 20%',
              content: OB.I18N.getLabel('OBPOS_LblExpected')
            },
            {
              style: 'padding: 10px 0px 10px 0px;  float: left;',
              content: OB.I18N.getLabel('OBPOS_LblCounted')
            }]
          }]
        },
        {
          classes: 'row-fluid',
          components: [{
            style: 'span12',
            components: [{
              classes: 'row-fluid',
                components: [ {
                  name: 'paymentsList',
                  kind: 'OB.UI.Table',
                  renderLine: 'OB.OBPOSCashUp.UI.RenderPaymentsLine',
                  renderEmpty: 'OB.UI.RenderEmpty',
                  listStyle: 'list'
                }]
            }]
          }]
        },
        {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            style: 'padding: 10px 5px 10px 0px; float: left; ',
            components: [{
              name: 'totalLbl',
              style: 'padding: 10px 20px 10px 10px; float: left; width: 70%; font-weight:bold;'
            }, {
              style: 'padding: 10px 20px 10px 0px;  float: right;',
              components: [{
                name: 'total',
                kind: 'OB.OBPOSCashUp.UI.RenderTotal',
                style: 'float:right; font-weight: bold;'
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
    this.$.totalLbl.setContent(OB.I18N.getLabel('OBPOS_ReceiptTotal'));
    this.$.paymentsList.setCollection(this.owner.model && this.owner.model.getData('DataCloseCashPaymentMethod'));
  },
  init: function () {
    // this.owner is the window (OB.UI.WindowView)
    // this.parent is the DOM object on top of the list (usually a DIV)
    this.$.paymentsList.setCollection( this.owner.model.getData('DataCloseCashPaymentMethod'));
  }
});

