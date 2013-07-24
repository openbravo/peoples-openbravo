/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalMultiOrdersLayaway',
  executeOnShow: function () {
    this.$.bodyButtons.$.btnModalMultiSearchInput.setValue('');
  },
  bodyContent: {
    i18nContent: 'OBPOS_MultiOrdersLayaway' // TODO: add this as part of the message + '\n' + OB.I18N.getLabel('OBPOS_cannotBeUndone')
  },
  bodyButtons: {
    components: [{
      name: 'btnModalMultiSearchInput',
      kind: 'OB.UI.SearchInput'
    }, {
      kind: 'OB.UI.btnApplyMultiLayaway'
    }]
  },
  initComponents: function () {
    this.header = OB.I18N.getLabel('OBPOS_MultiOrdersLayawayHeader');
    this.inherited(arguments);
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.btnApplyMultiLayaway',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblApplyButton',
  tap: function () {
    var amount = OB.DEC.Zero,
        tmp;
    if (this.owner.$.btnModalMultiSearchInput.getValue().indexOf('%') !== -1) {
      tmp = this.owner.$.btnModalMultiSearchInput.getValue().replace('%', '');
      amount = OB.DEC.div(OB.DEC.mul(this.model.get('multiOrders').get('multiOrdersList').get(this.owner.owner.args.id).getPending(), tmp), 100);
    } else {
      amount = OB.I18N.parseNumber(this.owner.$.btnModalMultiSearchInput.getValue());
    }
    this.model.get('multiOrders').get('multiOrdersList').get(this.owner.owner.args.id).set('amountToLayaway', amount);
    this.model.get('multiOrders').get('multiOrdersList').get(this.owner.owner.args.id).setOrderType(null, 2);
    this.doHideThisPopup();
  },
  init: function (model) {
    this.model = model;
  }
});