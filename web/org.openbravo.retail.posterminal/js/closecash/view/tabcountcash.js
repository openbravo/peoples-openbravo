/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.RenderPaymentsLine',
  events: {
    onLineEditCount: ''
  },
  components: [{
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        style: 'border-bottom: 1px solid #cccccc;',
        components: [{
          name: 'name',
          style: 'padding: 10px 20px 10px 10px; float: left; width: 15%'
        }, {
          name: 'foreignExpected',
          style: 'padding: 10px 10px 10px 0px; float: left; width: 15%;'
        }, {
          name: 'expected',
          style: 'padding: 10px 10px 10px 0px; float: left; width: 15%'
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
          name: 'foreignCounted',
          style: 'float: left; padding: 10px 0px 10px 0px; width: 15%',
          content: ''
        }, {
          name: 'counted',
          style: 'float: left; padding: 10px 0px 10px 0px; width: 15%',
          showing: false
        }]
      }]
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.name.setContent(this.model.get('name'));
    if (this.model.get('rate') && this.model.get('rate') !== '1') {
      this.$.foreignExpected.setContent('(' + OB.I18N.formatCurrency(this.model.get('foreignExpected')) + ' ' + this.model.get('isocode') + ')');
      this.$.foreignExpected.show();
    }
    this.$.expected.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, this.model.get('expected'))));
  },
  render: function () {
    var udfn, counted, foreignCounted;
    this.inherited(arguments);
    counted = this.model.get('counted');
    if (counted !== null && counted !== udfn) {
      this.$.counted.setContent(OB.I18N.formatCurrency(OB.DEC.add(0, counted)));
      this.$.counted.show();
      if (this.model.get('rate') && this.model.get('rate') !== '1') {
        this.$.foreignCounted.setContent('(' + OB.I18N.formatCurrency(this.model.get('foreignCounted')) + ' ' + this.model.get('isocode') + ')');
      }
      this.$.buttonOk.hide();
    }
    this.$.buttonEdit.setDisabled(this.model.get('paymentMethod').iscash);
  },
  lineEdit: function () {
    this.doLineEditCount();
  },
  lineOK: function (inSender, inEvent) {
    this.model.set('counted', this.model.get('expected'));
    this.model.set('foreignCounted', this.model.get('foreignExpected'));
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
    // console.log('totalC');
    this.setContent(OB.I18N.formatCurrency(this.total));
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
              initComponents: function () {
                this.setContent(OB.I18N.getLabel('OBPOS_LblStep2of4'));
              }
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
                  style: 'padding: 10px 20px 10px 10px; float: left; width: 32%',
                  initComponents: function () {
                    this.setContent(OB.I18N.getLabel('OBPOS_LblPaymentMethod'));
                  }
                }, {
                  style: 'padding: 10px 20px 10px 0px; float: left; width: 39%',
                  initComponents: function () {
                    this.setContent(OB.I18N.getLabel('OBPOS_LblExpected'));
                  }
                }, {
                  style: 'padding: 10px 0px 10px 0px;  float: left;',
                  initComponents: function () {
                    this.setContent(OB.I18N.getLabel('OBPOS_LblCounted'));
                  }
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
                    style: 'padding: 10px 20px 10px 10px;  float: left; width: 49px;'
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
                  style: 'padding: 10px 20px 10px 10px; float: left; width: 32%;',
                  initComponents: function () {
                    this.setContent(OB.I18N.getLabel('OBPOS_ReceiptTotal'));
                  }
                }, {
                  style: 'padding: 10px 20px 10px 0px; float: left; width: 14%;',
                  components: [{
                    name: 'total',
                    kind: 'OB.OBPOSCashUp.UI.RenderTotal',
                    style: 'font-weight: bold;'
                  }]
                }, {
                  style: 'padding: 17px 10px 17px 10px; float: left; width: 126px'
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
  setCollection: function (col) {
    this.$.paymentsList.setCollection(col);
  },
  anyCounted: function () {
    this.$.buttonAllOk.applyStyle('visibility', 'hidden'); //hiding in this way to keep the space
  },
  displayStep: function (model) {
    // this function is invoked when displayed.  
  }
});