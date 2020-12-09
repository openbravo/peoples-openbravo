/*
 ************************************************************************************
 * Copyright (C) 2017-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PaymentMethods',
  kind: 'OB.UI.Modal',
  classes: 'obObposPointOfSaleUiPaymentMethods',
  i18nHeader: 'OBPOS_MorePaymentsHeader',
  sideButtons: [],
  handlers: {
    onRegisterButton: 'registerMorePaymentMethod'
  },
  body: {
    classes: 'obObposPointOfSaleUiPaymentMethods-body',
    components: [
      {
        classes: 'obObposPointOfSaleUiPaymentMethods-body-container1',
        components: [
          {
            name: 'buttonslist',
            classes:
              'obObposPointOfSaleUiPaymentMethods-body-container1-buttonslist'
          }
        ]
      }
    ]
  },
  footer: {
    classes: 'obUiModal-footer-mainButtons',
    components: [
      {
        kind: 'OB.UI.ModalDialogButton',
        i18nContent: 'OBRDM_LblClose',
        isDefaultAction: true,
        tap: function() {
          this.doHideThisPopup();
        }
      }
    ]
  },
  registerMorePaymentMethod: function(inSender, inEvent) {
    this.args.toolbar.bubbleUp('onRegisterButton', inEvent, inSender);
    return true;
  },
  createPaymentButtons: function(toolbar) {
    enyo.forEach(
      this.sideButtons,
      function(sidebutton) {
        if (sidebutton.active) {
          sidebutton.btn.definition.includedInPopUp = true;
          this.$.body.$.buttonslist.createComponent(sidebutton);
        }
      },
      this
    );
  },
  executeOnShow: function() {
    var sideButtonLength = _.filter(this.sideButtons, function(sideButton) {
      return sideButton.active;
    }).length;
    if (this.$.body.$.buttonslist.children.length === 0) {
      this.createPaymentButtons(this.args.toolbar);
    } else if (
      this.$.body.$.buttonslist.children.length > 0 &&
      this.$.body.$.buttonslist.children.length !== sideButtonLength
    ) {
      this.$.body.$.buttonslist.destroyComponents();
      this.createPaymentButtons();
      this.$.body.$.buttonslist.render();
    }
    return true;
  },
  init: function(model) {
    this.model = model;
  }
});
