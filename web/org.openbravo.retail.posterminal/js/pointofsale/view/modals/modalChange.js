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
  events: {
    onHideThisPopup: ''
  },
  handlers: {
    onActionOK: 'actionOK',
    onActionInput: 'actionInput'
  },
  bodyContent: {
    name: 'bodyattributes',
    components: [{
      kind: 'Scroller',
      maxHeight: '225px',
      classes: 'changedialog-properties',
      thumb: true,
      horizontal: 'hidden',
      components: [{
        name: 'paymentlines'
      }]
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

    var i = 0;
    OB.MobileApp.model.get('payments').forEach(function (payment) {
      if (payment.paymentMethod.iscash) {
        this.$.bodyContent.$.paymentlines.createComponent({
          kind: 'OB.UI.ModalChangeLine',
          name: 'line_' + payment.payment.searchKey,
          index: i++,
          payment: payment
        });
      }
    }, this);
  },
  executeOnShow: function () {
    this.waterfall('onActionShow', this.args);
  },
  actionOK: function (inSender, inEvent) {
    var paymentstatus, changeRounding, indexRounding, lines, paymentchangemap, paymentchange, amount, origAmount, edited, i, l;

    paymentstatus = this.args.receipt.getPaymentStatus();
    changeRounding = paymentstatus.changeAmt;
    indexRounding = -1;
    lines = this.$.bodyContent.$.paymentlines.getComponents();
    edited = false;
    paymentchangemap = [];

    for (i = 0; i < lines.length; i++) {
      l = lines[i];
      edited = edited || l.edited;
      if (l.hasErrors) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ChangeAmountsNotValid'));
        return;
      }
      amount = parseFloat(l.$.textline.getValue());
      origAmount = Math.min(OB.DEC.mul(amount, l.payment.rate), paymentstatus.changeAmt);
      paymentchangemap.push({
        payment: l.payment,
        amount: amount,
        origAmount: origAmount
      });

      changeRounding = OB.DEC.sub(changeRounding, origAmount);
      if ((!l.payment.paymentMethod.changePaymentType && indexRounding < 0) || i === lines.length - 1) {
        // Line used for rounding is selected as the first line that does no have *changePaymentType*
        // or in case of all lines have *changePaymentType* then the last line
        // This guaranties *lineRounding* is assigned in case lines.length > 0
        indexRounding = i;
      }
    }

    if (edited) {
      if (lines.length === 2 && OB.DEC.compare(changeRounding)) {
        // The origAmount of the selected change line is adjusted in order to guaranty the change
        // of the receipt is the sum of the origAmount of all change lines
        paymentchangemap[indexRounding].origAmount = OB.DEC.add(paymentchangemap[indexRounding].origAmount, changeRounding);
      }
      paymentchange = new OB.Payments.Change();
      paymentchangemap.forEach(paymentchange.add.bind(paymentchange));
      this.args.applyPaymentChange(paymentchange);
    }
    this.doHideThisPopup();
  },
  actionInput: function (inSender, inEvent) {
    var lines, line, paymentstatus, originalValue, change, precision, linechange;

    if (inEvent.hasErrors) {
      return;
    }

    lines = this.$.bodyContent.$.paymentlines.getComponents();
    if (lines.length !== 2) {
      return;
    }

    paymentstatus = this.args.receipt.getPaymentStatus();
    line = lines[inEvent.line === lines[0] ? 1 : 0];
    originalValue = Math.min(OB.DEC.mul(inEvent.value, inEvent.line.payment.rate), paymentstatus.changeAmt);
    change = OB.DEC.sub(paymentstatus.changeAmt, originalValue);
    precision = line.payment.obposPosprecision;
    linechange = OB.DEC.mul(change, line.payment.mulrate, precision);

    line.assignValidValue(OB.Payments.Change.getChangeRounded({
      payment: line.payment,
      amount: linechange
    }));
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