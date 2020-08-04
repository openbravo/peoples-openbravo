/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _, enyo */

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalPriceModificationCancel',
  classes: 'obObposPointOfSaleUiModalsBtnModalPriceModificationCancel',
  i18nContent: 'OBMOBC_LblCancel',
  tap: function() {
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalPriceModificationAccept',
  classes: 'obObposPointOfSaleUiModalsBtnModalPriceModificationAccept',
  i18nContent: 'OBMOBC_LblOk',
  events: {
    onPriceModificationDone: ''
  },
  tap: function() {
    this.doPriceModificationDone();
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.List',
  name: 'OB.UI.ModalPriceModificationReason',
  classes: 'obUiModalPriceModificationReason',
  renderLine: enyo.kind({
    kind: 'enyo.Option',
    classes: 'obUiModalPriceModificationReason-renderLine-enyoOption',
    initComponents: function() {
      this.inherited(arguments);
      this.setValue(this.model.get('id'));
      this.setContent(this.model.get('_identifier'));
    }
  }),
  renderEmpty: 'enyo.Control',
  initComponents: function() {
    this.setCollection(new Backbone.Collection());
  }
});

enyo.kind({
  kind: 'OB.UI.Modal',
  name: 'OB.UI.ModalPriceModification',
  classes: 'obUiModalPriceModification',
  myId: 'modalPriceModification',
  autoDismiss: false,
  closeOnEscKey: false,
  hideCloseButton: true,
  body: {
    classes: 'obUiModalPriceModification-body',
    components: [
      {
        name: 'labelPriceModification',
        classes: 'obUiModalPriceModification-body-labelPriceModification'
      },
      {
        kind: 'OB.UI.ModalPriceModificationReason',
        name: 'priceModificationReason',
        classes: 'obUiModalPriceModification-body-priceModificationReason'
      }
    ]
  },
  i18nHeader: 'OBPOS_PriceModification',
  footer: {
    classes: 'obUiModalPriceModification-footer',
    components: [
      {
        kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalPriceModificationAccept',
        classes:
          'obUiModalPriceModification-footer-obObposPointOfSaleUiModalsBtnModalPriceModificationAccept'
      },
      {
        kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalPriceModificationCancel',
        classes:
          'obUiModalPriceModification-footer-obObposPointOfSaleUiModalsBtnModalPriceModificationCancel'
      }
    ]
  },
  events: {
    onPriceModification: ''
  },
  handlers: {
    onPriceModificationDone: 'doPriceModificationDone'
  },
  doPriceModificationDone: function() {
    this.args.callback({
      reason: this.$.body.$.priceModificationReason.getValue()
    });
  },
  executeOnShow: function() {
    const defaultReason = OB.App.State.getState().Ticket.lines.find(
      l => this.args.lineIds.includes(l.id) && l.oBPOSPriceModificationReason
    );

    if (defaultReason) {
      this.$.body.$.priceModificationReason.setSelected(
        OB.MobileApp.model
          .get('priceModificationReasons')
          .map(r => r.id)
          .indexOf(defaultReason.oBPOSPriceModificationReason)
      );
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    this.$.body.$.labelPriceModification.setContent(
      OB.I18N.getLabel('OBPOS_lblPriceModification')
    );
    var priceModificationReasonCollection = [];
    _.each(OB.MobileApp.model.get('priceModificationReasons'), function(
      reason
    ) {
      priceModificationReasonCollection.push(
        new Backbone.Model({
          id: reason.id,
          _identifier: reason[OB.Constants.IDENTIFIER]
        })
      );
    });
    this.$.body.$.priceModificationReason
      .getCollection()
      .reset(priceModificationReasonCollection);
  }
});
