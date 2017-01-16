/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PaymentMethods',
  kind: 'OB.UI.Modal',
  topPosition: '125px',
  i18nHeader: 'OBPOS_MorePaymentsHeader',
  sideButtons: [],
  handlers: {
    onRegisterButton: 'registerMorePaymentMethod'
  },
  body: {
    classes: 'row-fluid',
    components: [{
      classes: 'span12',
      components: [{
        style: 'border-bottom: 1px solid #cccccc;',
        classes: 'row-fluid',
        components: [{
          name: 'buttonslist',
          classes: 'span12'
        }]
      }]
    }]
  },
  registerMorePaymentMethod: function (inSender, inEvent) {
    this.args.toolbar.bubbleUp('onRegisterButton', inEvent, inSender);
    return true;
  },
  createPaymentButtons: function (toolbar) {
    enyo.forEach(this.sideButtons, function (sidebutton) {
      sidebutton.btn.definition.includedInPopUp = true;
      this.$.body.$.buttonslist.createComponent(sidebutton);
    }, this);
  },
  executeOnShow: function () {
    if (this.$.body.$.buttonslist.children.length === 0) {
      this.createPaymentButtons(this.args.toolbar);
    } else if (this.$.body.$.buttonslist.children.length > 0 && this.$.body.$.buttonslist.children.length !== this.sideButtons.length) {
      this.$.body.$.buttonslist.destroyComponents();
      this.createPaymentButtons();
      this.$.body.$.buttonslist.render();
    }
    return true;
  },
  init: function (model) {
    this.model = model;
  }
});