/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.ModalPayment',
  kind: 'OB.UI.ModalAction',
  header: '',
  maxheight: '600px',
  bodyContent: {},
  executeOnShow: function () {

    this.$.header.setContent(this.args.receipt && this.args.receipt.getTotal() > 0 ? OB.I18N.getLabel('OBPOS_LblModalPayment', [OB.I18N.formatCurrency(this.args.amount)]) : OB.I18N.getLabel('OBPOS_LblModalReturn', [OB.I18N.formatCurrency(this.args.amount)]));

    this.$.bodyContent.destroyComponents();
    //default values to reset changes done by a payment method
    this.closeOnEscKey = this.dfCloseOnEscKey;
    this.autoDismiss = this.dfAutoDismiss;
    this.executeOnShown = null;
    this.executeBeforeHide = null;
    this.executeOnHide = null;
    this.$.bodyContent.createComponent({
      mainPopup: this,
      kind: this.args.provider,
      paymentMethod: this.args.paymentMethod,
      paymentType: this.args.name,
      paymentAmount: this.args.amount,
      isocode: this.args.isocode,
      key: this.args.key,
      receipt: this.args.receipt,
      cashManagement: this.args.cashManagement,
      allowOpenDrawer: this.args.paymentMethod.allowopendrawer,
      isCash: this.args.paymentMethod.iscash,
      openDrawer: this.args.paymentMethod.openDrawer,
      printtwice: this.args.paymentMethod.printtwice,
      isReversePayment: this.args.isReversePayment,
      reversedPaymentId: this.args.reversedPaymentId,
      reversedPayment: this.args.reversedPayment
    }).render();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.dfAutoDismiss = this.autoDismiss;
    this.dfCloseOnEscKey = this.closeOnEscKey;
  }
});

enyo.kind({
  name: 'OB.UI.ModalPaymentVoid',
  kind: 'OB.UI.ModalAction',
  header: '',
  maxheight: '600px',
  bodyContent: {},
  bodyButtons: {},
  executeOnShow: function () {
    this.$.header.setContent(OB.I18N.getLabel('OBPOS_LblModalVoidTransaction', [OB.I18N.formatCurrency(this.args.amount)]));
    this.$.headerCloseButton.hide();
    this.autoDismiss = false;
  }
});