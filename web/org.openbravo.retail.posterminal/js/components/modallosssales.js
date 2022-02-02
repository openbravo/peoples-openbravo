/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.UI.LossSaleLine',
  kind: 'OB.UI.listItemButton',
  classes: 'obposUiLossSaleLine',
  components: [
    {
      classes: 'obposUiLossSaleLine-product',
      name: 'product'
    },
    {
      classes: 'obposUiLossSaleLine-priceLimit',
      name: 'priceLimit'
    },
    {
      classes: 'obposUiLossSaleLine-unitPrice',
      name: 'unitPrice'
    }
  ],
  create: function() {
    this.inherited(arguments);
    this.$.product.setContent(this.model.get('product'));
    this.$.priceLimit.setContent(
      OB.I18N.formatCurrency(this.model.get('priceLimit'))
    );
    this.$.unitPrice.setContent(
      OB.I18N.formatCurrency(this.model.get('unitPrice'))
    );
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ButtonLossSaleValidate',
  classes: 'obUiButtonButtonLossSaleValidate',
  i18nLabel: 'OBPOS_lblValidate',
  initComponents: function() {
    this.inherited(arguments);
  },
  events: {
    onProcessRequestLossSale: ''
  },
  tap: function() {
    this.doHideThisPopup();
    this.doProcessRequestLossSale({ requestApproval: true });
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ButtonLossSaleAdjust',
  classes: 'obUiButtonButtonLossSaleAdjust',
  i18nLabel: 'OBPOS_lblAdjust',
  initComponents: function() {
    this.inherited(arguments);
  },
  events: {
    onProcessRequestLossSale: ''
  },
  tap: function() {
    this.doHideThisPopup();
    this.doProcessRequestLossSale({ requestApproval: true, adjustPrice: true });
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ButtonLossSaleNotValidate',
  classes: 'obUiButtonButtonLossSaleNotValidate',
  i18nLabel: 'OBPOS_lblNotValidate',
  initComponents: function() {
    this.inherited(arguments);
  },
  events: {
    onProcessRequestLossSale: ''
  },
  tap: function() {
    this.doHideThisPopup();
    this.doProcessRequestLossSale({ lossSaleNotValidated: true });
  }
});

enyo.kind({
  name: 'OB.UI.ModalLossSaleFooter',
  classes: 'obUiModalLossSaleFooter',
  components: [
    {
      classes: 'obUiModal-footer-mainButtons',
      components: [
        {
          kind: 'OB.UI.ButtonLossSaleValidate',
          classes: 'obUiModalLossSaleFooter-obUiButtonLossSaleValidate',
          isDefaultAction: true
        },
        {
          kind: 'OB.UI.ButtonLossSaleAdjust',
          classes: 'obUiModalLossSaleFooter-obUiButtonLossSaleAdjust'
        },
        {
          kind: 'OB.UI.ButtonLossSaleNotValidate',
          classes: 'obUiModalLossSaleFooter-obUiButtonLossSaleNotValidate'
        }
      ]
    }
  ]
});

enyo.kind({
  name: 'OB.UI.LossSale',
  classes: 'obUiLossSale row-fluid',
  components: [
    {
      name: 'lossSaleLine',
      kind: 'OB.UI.ScrollableTable',
      classes: 'obUilossSaleLine-container',
      renderLine: 'OB.UI.LossSaleLine',
      renderEmpty: 'OB.UI.RenderEmpty'
    },
    {
      name: 'renderLoading',
      classes: 'obUiListLossSales-container-renderLoading',
      showing: false,
      initComponents: function() {
        this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
      }
    }
  ],
  init: function(model) {
    this.inherited(arguments);
    this.$.lossSaleLine.setCollection(new Backbone.Collection());
  },
  loadInfo: function(lossSaleLines) {
    this.$.lossSaleLine.collection.reset(lossSaleLines);
  }
});

/* Modal definition */
enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalLossSale',
  classes: 'obUiModalLossSale',
  i18nHeader: 'OBPOS_LblLossSale',
  handlers: {
    onProcessRequestLossSale: 'processRequestLossSale'
  },
  body: {
    kind: 'OB.UI.LossSale'
  },
  footer: {
    kind: 'OB.UI.ModalLossSaleFooter'
  },
  callback: null,

  initComponents: function() {
    this.inherited(arguments);
  },
  executeOnShow: function() {
    this.callback = this.args.callback;
    this.$.body.$.lossSale.loadInfo(this.args.lossSaleLines);
  },
  executeOnHide: function() {
    this.args.onhide();
  },
  processRequestLossSale: function(inSender, inEvent) {
    if (inEvent.adjustPrice) {
      inEvent.lines = this.args.lossSaleLines;
    }
    this.callback(inEvent);
  }
});
