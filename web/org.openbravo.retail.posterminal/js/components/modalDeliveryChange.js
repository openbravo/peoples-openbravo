/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo*/

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalDeliveryChangeButtonOK',
  classes: 'obUiModalDeliveryChangeButtonOK',
  i18nContent: 'OBMOBC_LblOk',
  events: {},
  tap: function() {
    var popup = this.owner.owner;
    if (popup.args.payload) {
      popup.args.payload.prepaymentChangeMode =
        popup.$.body.$.paymentOptions.active.setPrepaymentChange;
      popup.args.payload.prepaymentChangeAmt = popup.args.deliveryChange;
    } else {
      popup.args.receipt.set(
        'prepaymentChangeMode',
        popup.$.body.$.paymentOptions.active.setPrepaymentChange
      );
      popup.args.receipt.set('prepaymentChangeAmt', popup.args.deliveryChange);
      if (popup.args.receipt.get('prepaymentChangeMode')) {
        popup.args.receipt.trigger('updatePending');
      }
    }

    if (popup.args.callback) {
      popup.args.callback(popup.args);
    }
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalDeliveryChange',
  classes: 'obUiModalDeliveryChange',
  i18nHeader: 'OBPOS_LblActionRequired',
  autoDismiss: false,
  hideCloseButton: true,
  body: {
    classes: 'obUiModalDeliveryChange-body',
    components: [
      {
        name: 'labelPaymentInformation',
        classes: 'obUiModalDeliveryChange-body-labelPaymentInformation'
      },
      {
        name: 'paymentOptions',
        kind: 'Group',
        classes: 'obUiModalDeliveryChange-body-paymentOptions',
        components: [
          {
            kind: 'OB.UI.RadioButton',
            name: 'keepPayment',
            classes: 'obUiModalDeliveryChange-paymentOptions-keepPayment',
            components: [
              {
                name: 'keepPaymentLbl',
                classes: 'obUiModalDeliveryChange-keepPayment-keepPaymentLbl'
              }
            ]
          },
          {
            classes: 'obUiModalDeliveryChange-paymentOptions-element1'
          },
          {
            kind: 'OB.UI.RadioButton',
            setPrepaymentChange: true,
            name: 'returnChange',
            classes: 'obUiModalDeliveryChange-paymentOptions-returnChange',
            components: [
              {
                classes: 'obUiModalDeliveryChange-returnChange-returnChangeLbl',
                name: 'returnChangeLbl'
              }
            ]
          }
        ]
      }
    ]
  },
  footer: {
    classes: 'obUiModalDeliveryChange-footer',
    components: [
      {
        kind: 'OB.UI.ModalDeliveryChangeButtonOK',
        classes:
          'obUiModalDeliveryChange-footer-obUiModalDeliveryChangeButtonOK'
      }
    ]
  },
  events: {},
  handlers: {},
  initComponents: function() {
    this.inherited(arguments);
  },
  executeOnShow: function() {
    var symbol = OB.MobileApp.model.get('terminal').symbol,
      symbolAtRight = OB.MobileApp.model.get('terminal')
        .currencySymbolAtTheRight;
    this.$.body.$.labelPaymentInformation.setContent(
      OB.I18N.getLabel('OBPOS_DeliveryChangeMsg', [
        OB.I18N.formatCurrencyWithSymbol(
          this.args.deliveryChange,
          symbol,
          symbolAtRight
        )
      ])
    );
    this.$.body.$.keepPaymentLbl.setContent(
      OB.I18N.getLabel('OBPOS_KeepAsPaymentMsg')
    );
    this.$.body.$.returnChangeLbl.setContent(
      OB.I18N.getLabel('OBPOS_ReturnAsChangeMsg')
    );
    this.$.body.$.keepPayment.activeRadio();
  }
});
