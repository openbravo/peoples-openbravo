/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, $ */

enyo.kind({
  name: 'OB.UI.ModalReceiptPropertiesImpl',
  kind: 'OB.UI.ModalReceiptProperties',
  newAttributes: [{
    kind: 'OB.UI.renderTextProperty',
    name: 'receiptDescription',
    modelProperty: 'description',
    i18nLabel: 'OBPOS_LblDescription'
  }, {
    kind: 'OB.UI.renderBooleanProperty',
    name: 'printBox',
    checked: true,
    classes: 'modal-dialog-btn-check active',
    modelProperty: 'print',
    i18nLabel: 'OBPOS_Lbl_RP_Print'
  }
/*, {
    kind: 'OB.UI.renderBooleanProperty',
    name: 'emailBox',
    modelContent: 'bp:email',
    modelProperty: 'sendEmail',
    label: OB.I18N.getLabel('OBPOS_LblEmail')
  }*/
  ,
  {
    kind: 'OB.UI.renderBooleanProperty',
    name: 'invoiceBox',
    modelProperty: 'generateInvoice',
    i18nLabel: 'OBPOS_ToInvoice',
    readOnly: true
  }, {
    kind: 'OB.UI.renderBooleanProperty',
    name: 'returnBox',
    modelProperty: 'orderType',
    i18nLabel: 'OBPOS_ToBeReturned',
    readOnly: true
  }],

  init: function (model) {
    this.setHeader(OB.I18N.getLabel('OBPOS_ReceiptPropertiesDialogTitle'));

    this.model = model.get('order');
    this.model.bind('change', function () {
      var diff = this.model.changedAttributes(),
          att;
      for (att in diff) {
        if (diff.hasOwnProperty(att)) {
          this.loadValue(att);
        }
      }
    }, this);
  }
});