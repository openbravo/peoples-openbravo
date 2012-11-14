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
  name: 'OB.UI.ModalPayment',
  kind: 'OB.UI.ModalAction',
  header: '',
  maxheight: '600px',
  bodyContent: {},
  bodyButtons: {},
  executeOnShow: function () {

    if (this.args.receipt.get('orderType') === 0) {
      this.$.header.setContent(OB.I18N.getLabel('OBPOS_LblModalPayment', [OB.I18N.formatCurrency(this.args.amount)]));
    } else if (this.args.receipt.get('orderType') === 1) {
      this.$.header.setContent(OB.I18N.getLabel('OBPOS_LblModalReturn', [OB.I18N.formatCurrency(this.args.amount)]));
    } else {
      this.$.header.setContent('');
    }

    this.$.bodyContent.destroyComponents();
    this.$.bodyContent.createComponent({
      kind: this.args.provider,
      paymentMethod: this.args.paymentMethod,
      paymentType: this.args.name,
      paymentAmount: this.args.amount,
      key: this.args.key,
      receipt: this.args.receipt
    }).render();
  }
});