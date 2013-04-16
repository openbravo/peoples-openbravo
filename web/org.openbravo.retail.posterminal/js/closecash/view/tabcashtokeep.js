/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
  kind: 'OB.UI.RadioButton',
  style: 'padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 40px; margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px;',
  components: [{
    name: 'lbl',
    style: 'padding: 5px 0px 0px 0px;'
  }],
  events: {
    onPaymentMethodKept: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.doPaymentMethodKept({
      qtyToKeep: this.qtyToKeep,
      name: this.name
    });
  },
  render: function (content) {
    this.$.lbl.setContent(content);
  },
  setQtyToKeep: function (qty) {
    this.qtyToKeep = qty;
  },
  initComponents: function () {
    this.inherited(arguments);
    if (this.i18nLabel) {
      this.$.lbl.setContent(OB.I18N.getLabel(this.i18nLabel));
      return;
    }
    if (this.label) {
      this.$.lbl.setContent(this.label);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.KeepDetails',
  style: 'background-color: #ffffff; color: black;',
  events: {
    onResetQtyToKeep: ''
  },
  init: function (model) {
    this.model = model;
    var me = this;
    this.model.on('change:otherInput', function () {
      me.$.variableInput.setContent(me.model.get('otherInput'));
      me.$.allowvariableamount.setQtyToKeep(me.model.get('otherInput'));
      me.$.allowvariableamount.tap();
    }, this);
  },
  components: [{
    kind: "Group",
    name: 'RadioGroup',
    classes: 'btn-group',
    components: [{
      name: 'keepfixedamount',
      showing: false,
      kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton'
    }, {
      style: 'clear: both;'
    }, {
      name: 'allowmoveeverything',
      kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
      qtyToKeep: 0,
      i18nLabel: 'OBPOS_LblNothing',
      showing: false
    }, {
      style: 'clear: both;'
    }, {
      name: 'allowdontmove',
      kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
      showing: false
    }, {
      style: 'clear: both;'
    }, {
      name: 'allowvariableamount',
      binded: false,
      kind: 'OB.OBPOSCashUp.UI.CashToKeepRadioButton',
      showing: false,
      qtyToKeep: 0,
      style: 'padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 40px; margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px;',
      components: [{
        style: 'display: table-row;',
        components: [{
          style: 'vertical-align: middle; display: table-cell; ',
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LblOther'));
          }
        }, {
          name: 'variableInput',
          style: 'width: 90px; vertical-align: middle; margin-top: 0px; margin-right: 0px; margin-bottom: 1px; margin-left: 10px; display: inline-block; ',
          content: OB.DEC.Zero
        }]
      }]
    }]
  }]
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashToKeep',
  published: {
    paymentToKeep: null
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
              name: 'cashtokeepheader',
              style: 'padding: 10px; border-bottom: 1px solid #cccccc; text-align:center;',
              renderHeader: function (value) {
                this.setContent(OB.I18N.getLabel('OBPOS_LblStep3of4', [value]));
              }
            }]
          }]
        }, {
          kind: 'OB.OBPOSCashUp.UI.KeepDetails',
          name: 'formkeep',
          handlers: {
            onChangeOption: 'changeOption'
          },
          changeOption: function (inSender, inEvent) {
            this.$.allowvariableamount.tap();
          },
          disableControls: function () {
            //remove selected RButtons
            //reset UI and model.
            this.$.keepfixedamount.disableRadio();
            this.$.allowmoveeverything.disableRadio();
            this.$.allowdontmove.disableRadio();
            this.$.allowvariableamount.disableRadio();

            this.$.variableInput.setContent('');
            this.doResetQtyToKeep({
              qtyToKeep: null
            });
          },
          renderFixedAmount: function (modelToDraw) {
            var udfn, cnted;

            if (modelToDraw.get('foreignCounted')) {
              cnted = modelToDraw.get('foreignCounted');
            } else {
              cnted = modelToDraw.get('counted');
            }
            this.$.keepfixedamount.setShowing(modelToDraw.get('paymentMethod').keepfixedamount);
            if (modelToDraw.get('paymentMethod').keepfixedamount) {
              if (modelToDraw.get('foreignCounted') !== null && modelToDraw.get('foreignCounted') !== udfn) {
                if (cnted < modelToDraw.get('paymentMethod').amount) {
                  this.$.keepfixedamount.render(OB.I18N.formatCurrency(cnted));
                  this.$.keepfixedamount.setQtyToKeep(cnted);
                } else {
                  this.$.keepfixedamount.render(OB.I18N.formatCurrency(modelToDraw.get('paymentMethod').amount));
                  this.$.keepfixedamount.setQtyToKeep(modelToDraw.get('paymentMethod').amount);
                }
              } else {
                this.$.keepfixedamount.render(OB.I18N.formatCurrency(modelToDraw.get('paymentMethod').amount));
                this.$.keepfixedamount.setQtyToKeep(modelToDraw.get('paymentMethod').amount);
              }
            } else {
              this.$.keepfixedamount.render('');
            }
          },
          renderBody: function (modelToDraw) {
            var paymentMethod = modelToDraw.get('paymentMethod');
            this.disableControls();
            //draw
            this.renderFixedAmount(modelToDraw);

            this.$.allowmoveeverything.setShowing(paymentMethod.allowmoveeverything);

            this.$.allowdontmove.setShowing(paymentMethod.allowdontmove);
            if (paymentMethod.allowdontmove) {
              this.$.allowdontmove.setQtyToKeep(modelToDraw.get('foreignCounted'));
              this.$.allowdontmove.render(OB.I18N.getLabel('OBPOS_LblTotalAmount') + ' ' + OB.I18N.formatCurrency(modelToDraw.get('foreignCounted')));
            } else {
              this.$.allowdontmove.render('');
            }

            this.$.allowvariableamount.setShowing(paymentMethod.allowvariableamount);
          }
        }]
      }]
    }]
  }],
  paymentToKeepChanged: function (model) {
    this.$.cashtokeepheader.renderHeader(this.paymentToKeep.get('name'));
    this.$.formkeep.renderBody(this.paymentToKeep);

    //If fixed quantity to keep is more than counted quantity,
    //counted quantity should be propossed to keep.
    if (this.paymentToKeep.get('paymentMethod').keepfixedamount) {
      this.paymentToKeep.on('change:counted', function (mod) {
        this.$.formkeep.renderFixedAmount(this.paymentToKeep);
      }, this);
    }
  },
  disableSelection: function () {
    this.$.formkeep.disableControls();
  }
});