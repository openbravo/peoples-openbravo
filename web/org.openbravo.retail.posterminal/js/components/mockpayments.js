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
  name: 'OB.UI.MockPayment',
  components: [{
    components: [{
      classes: 'row-fluid',
      components: [{
        style: 'float:left; padding-left:30px',
        name: 'lblType'
      }, {
        name: 'paymenttype',
        style: 'float:right; font-weight: bold; padding-right:30px'
      }]
    }, {
      style: 'clear: both'
    }, {
      classes: 'row-fluid',
      components: [{
        style: 'float:left; padding-left:30px',
        name: 'lblAmount'
      }, {
        name: 'paymentamount',
        style: 'float:right; font-weight: bold; padding-right:30px'
      }]
    }]
  }, {
    style: 'clear: both'
  }, {
    kind: 'OB.UI.MockPayment_OkButton'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.lblType.setContent(OB.I18N.getLabel('OBPOS_LblModalType'));
    this.$.lblAmount.setContent(OB.I18N.getLabel('OBPOS_LblModalAmount'));
    this.$.paymenttype.setContent(this.paymentType);
    this.$.paymentamount.setContent(OB.I18N.formatCurrency(this.paymentAmount));
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.MockPayment_OkButton',
  style: 'float: right;',
  i18nContent: 'OBMOBC_LblOk',
  isDefaultAction: true,
  tap: function () {
    this.owner.receipt.addPayment(new OB.Model.PaymentLine({
      'kind': this.owner.key,
      'name': this.owner.paymentType,
      'amount': this.owner.paymentAmount,
      'allowOpenDrawer': this.owner.allowOpenDrawer,
      'isCash': this.owner.isCash,
      'openDrawer': this.owner.openDrawer
    }));

    this.doHideThisPopup();
  }
});