/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone, $, _, enyo */

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalPriceModificationCancel',
  i18nContent: 'OBMOBC_LblCancel',
  tap: function () {
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.OBPOSPointOfSale.UI.Modals.btnModalPriceModificationAccept',
  i18nContent: 'OBMOBC_LblOk',
  events: {
    onPriceModificationDone: ''
  },
  tap: function () {
    this.doPriceModificationDone();
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.List',
  name: 'OB.UI.ModalPriceModificationReason',
  classes: 'combo',
  style: 'width: 80%',
  renderLine: enyo.kind({
    kind: 'enyo.Option',
    initComponents: function () {
      this.inherited(arguments);
      this.setValue(this.model.get('id'));
      this.setContent(this.model.get('_identifier'));
    }
  }),
  renderEmpty: 'enyo.Control',
  initComponents: function () {
    this.setCollection(new Backbone.Collection());
  }
});

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalPriceModification',
  myId: 'modalPriceModification',
  autoDismiss: false,
  closeOnEscKey: false,
  bodyContent: {
    components: [{
      name: 'labelPriceModification',
      classes: 'modal-body-label'
    }, {
      style: 'width: 50%; float: left;',
      kind: 'OB.UI.ModalPriceModificationReason',
      name: 'priceModificationReason'
    }]
  },
  i18nHeader: 'OBPOS_PriceModification',
  bodyButtons: {
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalPriceModificationAccept'
    }, {
      kind: 'OB.OBPOSPointOfSale.UI.Modals.btnModalPriceModificationCancel'
    }]
  },
  events: {
    onPriceModification: ''
  },
  handlers: {
    onPriceModificationDone: 'doPriceModificationDone'
  },
  doPriceModificationDone: function () {
    var me = this,
        i;
    if (this.args.selectedModels.length > 1) {
      for (i = 0; i < this.args.selectedModels.length; i++) {
        this.args.selectedModels[i].set('oBPOSPriceModificationReason', me.$.bodyContent.$.priceModificationReason.getValue());
      }
    } else {
      this.args.line.set('oBPOSPriceModificationReason', me.$.bodyContent.$.priceModificationReason.getValue());
    }
    this.args.callback();
  },
  executeOnShow: function () {
    var i;
    if (this.args.selectedModels.length <= 1 && this.args.line.get('oBPOSPriceModificationReason')) {
      for (i = 0; i < OB.MobileApp.model.get('priceModificationReasons').length; i++) {
        if (OB.MobileApp.model.get('priceModificationReasons')[i].id === this.args.line.get('oBPOSPriceModificationReason')) {
          this.$.bodyContent.$.priceModificationReason.setSelected(i);
          break;
        }
      }
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.$.bodyContent.$.labelPriceModification.setContent(OB.I18N.getLabel('OBPOS_lblPriceModification'));
    var priceModificationReasonCollection = [];
    _.each(OB.MobileApp.model.get('priceModificationReasons'), function (reason) {
      priceModificationReasonCollection.push(new Backbone.Model({
        id: reason.id,
        _identifier: reason[OB.Constants.IDENTIFIER]
      }));
    });
    this.$.bodyContent.$.priceModificationReason.getCollection().reset(priceModificationReasonCollection);
    this.$.headerCloseButton.hide();
  }
});