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
  executeOnShow: function (args) {

    if (args.receipt.get('orderType') === 0) {
      this.$.header.setContent(OB.I18N.getLabel('OBPOS_LblModalPayment', [OB.I18N.formatCurrency(args.amount)]));
    } else if (receipt.get('orderType') === 1) {
      this.$.header.setContent(OB.I18N.getLabel('OBPOS_LblModalReturn', [OB.I18N.formatCurrency(args.amount)]));
    } else {
      this.$.header.setContent('');
    }

    this.$.bodyContent.destroyComponents();
    this.$.bodyContent.createComponent({
      kind: args.provider,
      paymentMethod: args.paymentMethod,
      paymentType: args.name,
      paymentAmount: args.amount,
      key: args.key,
      receipt: args.receipt
    }).render();
  }
});