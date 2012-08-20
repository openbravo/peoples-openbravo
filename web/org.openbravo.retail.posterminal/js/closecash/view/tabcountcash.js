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
  name: 'OB.OBPOSCashUp.UI.RenderPaymentsLine',
  events: {
	onLineEditCount:''  
  },
  components: [{
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        style: 'border-bottom: 1px solid #cccccc;',
        components: [{
          name: 'name',
          style: 'padding: 10px 20px 10px 10px; float: left; width: 20%'
        }, {
          name: 'expected',
          style: 'padding: 10px 20px 10px 10px; float: left; width: 20%'
        }, {
          style: 'float: left;',
          components: [{
            name: 'buttonEdit',
            kind: 'OB.UI.SmallButton',
            classes: 'btnlink-orange btnlink-cashup-edit btn-icon-small btn-icon-edit',
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
      }]
    }]
  }],
  create: function() {
    this.inherited(arguments);
    this.$.name.setContent(this.model.get('name'));
    this.$.expected.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('expected'))));
  },
  render: function() {
    var udfn;
    this.inherited(arguments);
    counted = this.model.get('counted');
    if (counted !== null && counted !== udfn) {
      this.$.counted.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, counted)));
      this.$.counted.show();
      this.$.buttonOk.hide();
    }
  },
  lineEdit: function() {
    this.doLineEditCount();
  },
  lineOK: function(inSender, inEvent) {
    this.model.set('counted', this.model.get('expected'));
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderTotal',
  tag: 'span',
  published: {
    total: OB.DEC.Zero
  },
  create: function() {
    this.inherited(arguments);
  },
  totalChanged: function(oldValue) {
    // console.log('totalC');
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
  handlers: {
    onAnyCounted: 'anyCounted'
  },
  events: {
    onCountAllOK: ''
  },
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
          style: 'background-color: #ffffff; color: black;',
          components: [{
            components: [{
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
              name: 'paymentsList',
              kind: 'OB.UI.Table',
              renderLine: 'OB.OBPOSCashUp.UI.RenderPaymentsLine',
              renderEmpty: 'OB.UI.RenderEmpty',
              listStyle: 'list'
            }, {
              components: [{
                classes: 'row-fluid',
                components: [{
                  classes: 'span12',
                  style: 'border-bottom: 1px solid #cccccc;',
                  components: [{
                    style: 'padding: 10px 20px 10px 10px;  float: left; width: 20%;'
                  }, {
                    style: 'padding: 10px 20px 10px 10px;  float: left; width: 20%;'
                  }, {
                    style: 'padding: 10px 20px 10px 10px;  float: left; width: 30px;'
                  }, {
                    style: 'float: left;',
                    components: [{
                      name: 'buttonAllOk',
                      kind: 'OB.UI.SmallButton',
                      classes: 'btnlink-green btnlink-cashup-ok btn-icon-small btn-icon-check',
                      ontap: 'doCountAllOK'
                    }]
                  }]
                }]
              }]
            }, {
              classes: 'row-fluid',
              components: [{
                classes: 'span12',
                style: 'border-bottom: 1px solid #cccccc;',
                components: [{
                  name: 'totalLbl',
                  style: 'padding: 10px 20px 10px 10px; float: left; width: 20%;',
                  content: OB.I18N.getLabel('OBPOS_ReceiptTotal')
                }, {
                  style: 'padding: 10px 20px 10px 0px; float: left; width: 20%;',
                  components: [{
                    name: 'total',
                    kind: 'OB.OBPOSCashUp.UI.RenderTotal',
                    style: 'font-weight: bold;'
                  }]
                }, {
                  style: 'padding: 17px 10px 17px 10px; float: left; width: 44px'
                }, {
                  style: 'padding: 10px 5px 10px 0px; float: left;',
                  components: [{
                    name: 'diference',
                    kind: 'OB.OBPOSCashUp.UI.RenderTotal',
                    style: 'font-weight: bold;'
                  }]
                }]
              }]
            }]
          }]
        }]
      }]
    }]
  }],
  setCollection: function(col) {
    this.$.paymentsList.setCollection(col);
  },
  anyCounted: function() {
    this.$.buttonAllOk.applyStyle('visibility','hidden'); //hiding in this way to keep the space
  }

});