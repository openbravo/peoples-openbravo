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
        classes: 'span6',
        style: 'text-align:left;',
        content: OB.I18N.getLabel('OBPOS_LblModalType')
      }, {
        name: 'paymenttype',
        classes: 'span6',
        style: 'font-weight: bold;'
      }]
    }, {
      classes: 'row-fluid',
      components: [{
        classes: 'span6',
        style: 'text-align:left;',
        content: OB.I18N.getLabel('OBPOS_LblModalAmount')
      }, {
        name: 'paymentamount',
        classes: 'span6',
        style: 'font-weight: bold;'
      }]
    }]
  }, {
    kind: 'OB.UI.MockPayment_OkButton'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.paymenttype.setContent(this.paymentType);
    this.$.paymentamount.setContent(OB.I18N.formatCurrency(this.paymentAmount));
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.MockPayment_OkButton',
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  style: 'float: right;',
  content: OB.I18N.getLabel('OBPOS_LblOk'),
  isApplyButton: true,
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.owner.receipt.addPayment(new OB.Model.PaymentLine({
      'kind': this.owner.key,
      'name': this.owner.paymentType,
      'amount': this.owner.paymentAmount
    }));

    this.doHideThisPopup();
  }
});