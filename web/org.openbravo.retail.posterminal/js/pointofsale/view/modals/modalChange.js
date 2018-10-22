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
    }, {
      name: 'errors',
      classes: 'changedialog-errors'
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
    var lines = this.$.bodyContent.$.paymentlines.getComponents();
    lines.forEach(function (l) {
      l.executeOnShow(this.args);
    }.bind(this));
    this.calculateRemaining();
  },
  calculateRemainingFor: function (selectedLine) {
    var i, l, lines, amount, precision, roundingto, roundinggap, origamountmin, origamountmax, changemin, changemax, change, changeRounding;

    lines = this.$.bodyContent.$.paymentlines.getComponents();
    change = this.args.change;
    changemin = 0;
    changemax = 0;
    for (i = 0; i < lines.length; i++) {
      l = lines[i];
      if (l !== selectedLine && !l.hasErrors) {
        amount = l.getParsedValue();
        precision = l.payment.obposPosprecision;
        if (l.payment.changeRounding) {
          roundingto = l.payment.changeRounding.roundingto;
          roundinggap = l.payment.changeRounding.roundingdownlimit;
          origamountmin = OB.DEC.div(OB.DEC.sub(amount, OB.DEC.sub(roundingto, roundinggap, precision), precision), l.payment.mulrate);
          origamountmin = Math.max(origamountmin, 0);
          origamountmax = OB.DEC.div(OB.DEC.add(amount, roundinggap, precision), l.payment.mulrate);
        } else {
          origamountmin = OB.DEC.div(amount, l.payment.mulrate, precision);
          origamountmax = OB.DEC.div(amount, l.payment.mulrate, precision);
        }
        changemin = OB.DEC.add(changemin, origamountmin);
        changemax = OB.DEC.add(changemax, origamountmax);
      }
    }

    if (change > changemax) {
      changeRounding = OB.DEC.sub(change, changemax); // Incomplete
    } else if (change < changemin) {
      changeRounding = OB.DEC.sub(change, changemin); // Overpaid
    } else {
      changeRounding = 0; // In range, then complete.
    }

    return changeRounding;
  },

  calculateRemaining: function () {
    var changeremaining = this.calculateRemainingFor(),
        lines = this.$.bodyContent.$.paymentlines.getComponents(),
        errortext = '',
        overpayment = false;

    lines.forEach(function (l) {
      l.showRemaining(changeremaining, this.calculateRemainingFor(l));

      if (l.isOverpaid) {
        overpayment = true;
      } else if (l.hasErrors) {
        if (errortext) {
          errortext = OB.I18N.getLabel('OBPOS_ChangeAmountsNotValid');
        } else {
          errortext = l.labelError;
        }
      }
    }.bind(this));

    // Show the change error, if any
    if (overpayment) {
      this.$.bodyContent.$.errors.setContent(OB.I18N.getLabel('OBPOS_Overpayment'));
    } else {
      this.$.bodyContent.$.errors.setContent(errortext);
    }
  },

  actionOK: function (inSender, inEvent) {
    var changeRounding, indexRounding, lines, paymentchangemap, paymentchange, amount, origAmount, edited, i, l;

    changeRounding = this.args.change;
    indexRounding = -1;
    lines = this.$.bodyContent.$.paymentlines.getComponents();
    edited = false;
    paymentchangemap = [];

    for (i = 0; i < lines.length; i++) {
      l = lines[i];
      edited = edited || l.edited;
      if (l.hasErrors) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ChangeAmountsNotValid'));
        return true;
      }
      if (!l.isComplete) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ChangeAmountsNotComplete'));
        return true;
      }
      amount = l.getParsedValue();
      origAmount = OB.DEC.div(amount, l.payment.mulrate);
      paymentchangemap.push({
        payment: l.payment,
        amount: amount,
        origAmount: origAmount
      });

      changeRounding = OB.DEC.sub(changeRounding, origAmount);
      if ((l.payment.changeRounding || i === lines.length - 1) && indexRounding < 0) {
        // Line used for rounding is selected as the first line that does no have *changePaymentType*
        // or in case of all lines have *changePaymentType* then the last line
        // This guaranties *lineRounding* is assigned in case lines.length > 0
        indexRounding = i;
      }
    }

    if (edited) {
      if (OB.DEC.compare(changeRounding)) {
        // The origAmount of the selected change line is adjusted in order to guaranty the change
        // of the receipt is the sum of the origAmount of all change lines
        paymentchangemap[indexRounding].origAmount = OB.DEC.add(paymentchangemap[indexRounding].origAmount, changeRounding);
        paymentchangemap[indexRounding].amountRounded = paymentchangemap[indexRounding].amount;
        paymentchangemap[indexRounding].amount = OB.DEC.mul(paymentchangemap[indexRounding].origAmount, lines[indexRounding].payment.mulrate, lines[indexRounding].payment.obposPosprecision);
      }
      paymentchange = new OB.Payments.Change();
      paymentchangemap.forEach(paymentchange.add.bind(paymentchange));
      this.args.applyPaymentChange(paymentchange);
    }
    this.doHideThisPopup();

    return true;
  },
  actionInput: function (inSender, inEvent) {
    var lines, line, originalValue, change, precision, linechange, changeLessThan;

    lines = this.$.bodyContent.$.paymentlines.getComponents();

    if (!inEvent.hasErrors && lines.length === 2) {
      line = lines[inEvent.line === lines[0] ? 1 : 0];
      originalValue = OB.DEC.div(inEvent.value, inEvent.line.payment.mulrate);
      change = OB.DEC.sub(this.args.change, originalValue);
      precision = line.payment.obposPosprecision;
      linechange = OB.DEC.mul(change, line.payment.mulrate, precision);
      changeLessThan = line.payment.paymentMethod.changeLessThan;
      if (changeLessThan) {
        linechange = OB.DEC.mul(changeLessThan, Math.round(OB.DEC.div(linechange, changeLessThan, 5)), precision);
      } else {
        linechange = OB.Payments.Change.getChangeRounded({
          payment: line.payment,
          amount: linechange
        });
      }
      linechange = Math.min(linechange, line.maxValue);
      linechange = Math.max(linechange, 0);

      line.assignValidValue(linechange);
    }
    this.calculateRemaining();

    return true;
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