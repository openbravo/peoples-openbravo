/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, _, enyo */

enyo.kind({
  name: 'OB.UI.ModalChangeCashLine',
  handlers: {
    onActionShow: 'actionShow'
  },
  components: [{
    name: 'labelLine',
    classes: 'properties-label',
    style: 'padding: 5px 8px 0px 0px; font-size: 15px;',
    content: ''
  }, {
    classes: 'modal-dialog-receipt-properties-text',
    style: 'border: 1px solid #F0F0F0; float: left; width: 180px;',
    components: [{
      name: 'textline',
      kind: 'enyo.Input',
      type: 'text',
      classes: 'input',
      style: 'width: 100%; margin-bottom:0px; text-align: right;',
      oninput: 'actionInput'
    }]
  }, {
    name: 'infoline',
    style: 'float: left; color: #6f6f6f; font-size: 12px; margin-left: 25px;',
    content: ''
  }, {
    style: 'clear: both'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.labelLine.content = this.payment.payment.commercialName;
  },
  actionInput: function (inSender, inEvent) {
    var value = parseFloat(this.$.textline.getValue());
    this.hasErrors = _.isNaN(value) || value < 0 || value > this.maxValue;
    this.$.textline.addStyles('background-color: ' + (this.hasErrors ? '#fe7f7f' : 'inherit') + ';');

    return this.bubble('onActionInput', {
      'value': value,
      'hasErrors': this.hasErrors,
      'line': this
    });
  },
  actionShow: function (inSender, inEvent) {
    var s, change, cRounded, currentChange;

    s = this.payment.obposPosprecision;
    change = OB.DEC.mul(inEvent.receipt.get('change'), this.payment.mulrate, s);
    cRounded = OB.Payments.Change.getChangeRounded({
      'payment': this.payment,
      'change': change
    });
    this.maxValue = cRounded;
    this.$.infoline.setContent(OB.I18N.getLabel('OBPOS_MaxChange', [OB.I18N.formatCurrencyWithSymbol(cRounded, this.payment.symbol, this.payment.currencySymbolAtTheRight)]));

    currentChange = inEvent.receipt.get('changePayments').find(function (item) {
      return item.key === this.payment.payment.searchKey;
    }, this);

    this.assignValidValue(currentChange ? currentChange.amountRounded : 0);
  },
  assignValidValue: function (amountRounded) {
    this.$.textline.setValue(amountRounded);
    this.$.textline.addStyles('background-color: inherit;');
    this.hasErrors = false;
    setTimeout(function () {
      this.$.textline.hasNode().setSelectionRange(0, this.$.textline.getValue().length);
    }.bind(this), 100);
  }
});