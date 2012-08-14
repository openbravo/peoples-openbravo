/*global enyo */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

//enyo.kind({
//  name: 'OB.OBPOSCashUp.UI.ButtonOk',
//  kind: 'OB.UI.SmallButton',
//  classes: 'btnlink-green btnlink-cashup-ok btn-icon-small btn-icon-check',
//  events: {
//    onButtonOk: ''
//  },
//  tap: function () {
//    this.doButtonOk();
//  }
//});
//enyo.kind({
//  name: 'OB.OBPOSCashUp.UI.ButtonEdit',
//  kind: 'OB.UI.SmallButton',
//  classes: 'btnlink-orange btnlink-cashup-ok btn-icon-small btn-icon-edit',
//  tap: function () {
//    //FIXME: Use events and handlers
//    this.owner.owner.owner.owner.owner.owner.$.cashUpKeyboard.doCommandFired({
//      key: this.type
//    });
//  }
//});
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
    }, {
      style: 'float: left;',
      components: [{
        name: 'buttonEdit',
        kind: 'OB.UI.SmallButton',
        classes: 'btnlink-orange btnlink-cashup-ok btn-icon-small btn-icon-edit',
        ontap: 'lineEdit'
      }]
    }, {
      style: 'float: left;',
      components: [{
        name: 'buttonOk',
        kind: 'OB.UI.SmallButton',
        classes: 'btnlink-green btnlink-cashup-ok btn-icon-small btn-icon-check',
        ontap: 'lineOK'
      }]
    }, {
      name: 'counted',
      style: 'float: left; padding: 10px 0px 10px 10px;',
      showing: false
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.name.setContent(this.model.get('name'));
    this.$.expected.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('expected'))));
  },
  lineEdit: function (inSender, inEvent) {
    this.log('lineEdit');
  },
  lineOK: function (inSender, inEvent) {
    debugger;
    this.model.set('counted', this.model.get('expected'));
    this.$.counted.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('counted'))));
    this.$.counted.setShowing(true);
    this.doLineOK({model: this.model}); // add this counted to window model totalCounted
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
              style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
              content: OB.I18N.getLabel('OBPOS_LblStep2of4')
            }]
          }]
        }, {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            style: 'border-bottom: 1px solid #cccccc;',
            components: [{
              style: 'padding: 10px 20px 10px 10px; float: left; width: 20%',
              content: OB.I18N.getLabel('OBPOS_LblPaymentMethod')
            }, {
              style: 'padding: 10px 20px 10px 10px; float: left; width: 20%',
              content: OB.I18N.getLabel('OBPOS_LblExpected')
            }, {
              style: 'padding: 10px 0px 10px 0px;  float: left;',
              content: OB.I18N.getLabel('OBPOS_LblCounted')
            }]
          }]
        }, {
          classes: 'row-fluid',
          components: [{
            style: 'span12',
            components: [{
              classes: 'row-fluid',
              components: [{
                name: 'paymentsList',
                kind: 'OB.UI.Table',
                renderLine: 'OB.OBPOSCashUp.UI.RenderPaymentsLine',
                renderEmpty: 'OB.UI.RenderEmpty',
                listStyle: 'list'
              }]
            }]
          }]
        }, {
          classes: 'row-fluid',
          components: [{
            classes: 'span12',
            style: 'padding: 10px 5px 10px 0px; float: left; ',
            components: [{
              name: 'totalLbl',
              style: 'padding: 10px 20px 10px 10px; float: left; width: 70%; font-weight:bold;',
              content: OB.I18N.getLabel('OBPOS_ReceiptTotal')
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
  setCollection: function (col) {
    this.$.paymentsList.setCollection(col);
  }
});