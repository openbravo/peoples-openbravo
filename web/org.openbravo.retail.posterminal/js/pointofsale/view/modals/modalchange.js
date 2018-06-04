/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalChange',
  handlers: {
    'onActionOK': 'actionOK'
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
    //this.$.bodyContent.$.bodymessage.setContent(this.args.message);
    this.waterfall('onActionShow', this.args);
  },
  actionOK: function (inSender, inEvent) {
    alert('OK');
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
      style: 'width: 100%; margin-bottom:0px; text-align: right;'
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
  actionShow: function (inSender, inEvent) {
    var change = OB.DEC.mul(inEvent.receipt.get('change'), this.payment.mulrate);
    var cRounded = OB.OBPOSPointOfSale.UI.Payment.getChangeRounded(this.payment, change);
    this.$.infoline.setContent(OB.I18N.getLabel('OBPOS_MaxChange', [OB.I18N.formatCurrencyWithSymbol(cRounded, this.payment.symbol, this.payment.currencySymbolAtTheRight)]));

    var currentChange = inEvent.receipt.get('changePayments').find(function (item) {
      return item.key === this.payment.payment.searchKey;
    }, this);
    this.$.textline.setValue(currentChange ? currentChange.amountRounded : '');
  }
});