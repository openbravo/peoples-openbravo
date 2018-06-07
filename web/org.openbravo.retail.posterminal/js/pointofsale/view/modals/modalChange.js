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
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalChange',
  events: {
    onHideThisPopup: ''
  },
  handlers: {
    'onActionOK': 'actionOK',
    'onActionInput': 'actionInput'
  },
  bodyContent: {
    name: 'bodyattributes',
    components: [{
      kind: 'Scroller',
      maxHeight: '225px',
      style: 'background-color: #ffffff;',
      thumb: true,
      horizontal: 'hidden',
      components: [{
        name: 'paymentlines'
      }]
    }, {
      name: 'bodytext',
      style: 'visibility: hidden;'
    }]
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ModalChangeButtonOK'
    }, {
      kind: 'OB.UI.ModalChangeButtonCancel'
    }]
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.header.setContent(OB.I18N.getLabel('OBPOS_ChangeSplit'));

    OB.MobileApp.model.get('payments').forEach(function (payment) {
      if (payment.paymentMethod.iscash) {
        this.$.bodyContent.$.paymentlines.createComponent({
          kind: 'OB.UI.ModalChangeCashLine',
          name: 'line_' + payment.payment.searchKey,
          payment: payment
        });
      }
    }, this);
  },
  executeOnShow: function () {
    this.waterfall('onActionShow', this.args);
  },
  actionOK: function (inSender, inEvent) {
    var lines, result, i, l, change;

    lines = this.$.bodyContent.$.paymentlines.getComponents();
    result = [];

    for (i = 0; i < lines.length; i++) {
      l = lines[i];
      if (l.hasErrors) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_CHANGEAMOUNTSNOTVALID'));
        return;
      }
      change = parseFloat(l.$.textline.getValue());
      if (OB.DEC.compare(change) > 0) {
        result.push({
          'payment': l.payment,
          'change': change
        });
      }
    }

    this.args.callback(result);
    this.doHideThisPopup();
  },
  actionInput: function (inSender, inEvent) {
    // var i;
    // var lines = this.$.bodyContent.$.paymentlines.getComponents();
    // for (i = 0; i < lines.length; i ++) {
    // }
    // this.$.bodyContent.$.paymentlines.getComponents().forEach(function (item) {
    //   if (inSender === item) {
    //   }
    //   console.log(item.name + '-> ' + item.$.textline.getValue());
    // });
  }
});

enyo.kind({
  name: 'OB.UI.ModalChangeButtonOK',
  kind: 'OB.UI.ModalDialogButton',
  i18nContent: 'OBMOBC_LblOk',
  isDefaultAction: true,
  tap: function () {
    return this.bubble('onActionOK');
  }
});

enyo.kind({
  name: 'OB.UI.ModalChangeButtonCancel',
  kind: 'OB.UI.ModalDialogButton',
  i18nContent: 'OBMOBC_LblCancel',
  tap: function () {
    this.doHideThisPopup();
  }
});

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
      'hasErrors': this.hasErrors
    });
  },
  actionShow: function (inSender, inEvent) {
    var s, change, cRounded, currentChange, amountRounded;

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

    amountRounded = currentChange ? currentChange.amountRounded : 0;
    this.$.textline.setValue(amountRounded);
    this.$.textline.addStyles('background-color: inherit;');
    this.hasErrors = false;
    setTimeout(function () {
      this.$.textline.hasNode().setSelectionRange(0, this.$.textline.getValue().length);
    }.bind(this), 100);
  }
});