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
  name: 'OB.UI.ModalChangeLine',
  handlers: {
    onActionShow: 'actionShow'
  },
  components: [{
    name: 'labelLine',
    classes: 'properties-label changedialog-properties-label',
    content: ''
  }, {
    classes: 'modal-dialog-receipt-properties-text changedialog-properties-text',
    components: [{
      name: 'textline',
      kind: 'enyo.Input',
      type: 'text',
      classes: 'input changedialog-properties-input',
      oninput: 'actionInput'
    }]
  }, {
    name: 'infoline',
    classes: 'changedialog-properties-info',
    content: ''
  }, {
    classes: 'changedialog-properties-end'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.labelLine.content = this.payment.payment.commercialName;
  },
  actionInput: function (inSender, inEvent) {
    var value = parseFloat(this.$.textline.getValue());
    this.edited = true;
    this.hasErrors = _.isNaN(value) || value < 0 || value > this.maxValue;
    this.$.textline.removeClass('changedialog-properties-validation-ok');
    this.$.textline.removeClass('changedialog-properties-validation-error');
    this.$.textline.addClass(this.hasErrors ? 'changedialog-properties-validation-error' : 'changedialog-properties-validation-ok');

    return this.bubble('onActionInput', {
      value: value,
      hasErrors: this.hasErrors,
      line: this
    });
  },
  actionShow: function (inSender, inEvent) {
    var paymentstatus, precision, change, cRounded, currentChange;

    paymentstatus = inEvent.receipt.getPaymentStatus();
    precision = this.payment.obposPosprecision;
    change = OB.DEC.mul(paymentstatus.changeAmt, this.payment.mulrate, precision);
    cRounded = OB.Payments.Change.getChangeRounded({
      payment: this.payment,
      amount: change
    });
    this.maxValue = cRounded;
    this.$.infoline.setContent(OB.I18N.getLabel('OBPOS_MaxChange', [OB.I18N.formatCurrencyWithSymbol(cRounded, this.payment.symbol, this.payment.currencySymbolAtTheRight)]));
    this.edited = false;

    currentChange = inEvent.receipt.get('changePayments').find(function (item) {
      return item.key === this.payment.payment.searchKey;
    }, this);

    this.assignValidValue(currentChange ? currentChange.amountRounded : 0);
  },
  assignValidValue: function (amountRounded) {
    this.$.textline.setValue(amountRounded);
    this.$.textline.removeClass('changedialog-properties-validation-error');
    this.$.textline.addClass('changedialog-properties-validation-ok');
    this.hasErrors = false;
    setTimeout(function () {
      this.$.textline.hasNode().setSelectionRange(0, this.$.textline.getValue().length);
    }.bind(this), 100);
  }
});