/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OBRDM.UI.OrderLineInfoPopup',
  kind: 'OB.UI.Modal',
  classes: 'obrdmUiOrderLineInfoPopup',
  i18nHeader: 'OBRDM_LblInformation',
  body: {
    kind: 'OBRDM.UI.OrderLineInfo_body'
  },
  handlers: {
    onHideInfoPopup: 'hidePopup',
    onShowInfoPopup: 'showPopup'
  },
  executeOnShow: function() {
    this.line = this.args.line;
    this.waterfall('onShowInfo', {
      line: this.line
    });
  },
  executeOnHide: function() {
    if (this.args.callback) {
      this.args.callback(this.args.context);
    }
  },
  initComponents: function() {
    this.inherited(arguments);
  },
  hidePopup: function(inSender, inEvent) {
    this.hide();
  },
  showPopup: function(inSender, inEvent) {
    this.show();
  }
});

enyo.kind({
  name: 'OBRDM.UI.OrderLineInfo_body',
  classes: 'obrdmUiOrderLineInfoBody',
  components: [
    {
      name: 'table',
      kind: 'OBRDM.UI.OrderLineInfoTable',
      classes: 'obrdmUiOrderLineInfoBody-table'
    },
    {
      name: 'buttonFooter',
      classes: 'obrdmUiOrderLineInfoBody-buttonFooter',
      components: [
        {
          name: 'infoButtons',
          classes: 'obrdmUiOrderLineInfoBody-buttonFooter-infoButtons'
        }
      ]
    }
  ],
  buttons: [
    {
      kind: 'OBRDM.UI.OrderLineInfoCloseBtn',
      name: 'buttoncancel',
      classes: 'obrdmUiOrderLineInfoBody-buttoncancel'
    }
  ],
  initComponents: function() {
    var me = this;

    this.inherited(arguments);
    _.each(this.buttons, function(button) {
      me.$.infoButtons.createComponent(button, {
        owner: me
      });
    });
  }
});

enyo.kind({
  name: 'OBRDM.UI.OrderLineInfoCloseBtn',
  kind: 'OB.UI.Button',
  classes: 'btnlink-small btnlink-gray obrdm-orderlineinfo-closebtn',
  i18nLabel: 'OBRDM_LblClose',
  events: {
    onHideInfoPopup: ''
  },
  tap: function() {
    this.doHideInfoPopup();
  }
});

enyo.kind({
  name: 'OBRDM.UI.OrderLineInfoLine',
  classes: 'obrdmUiOrderLineInfoLine',
  handlers: {
    onShowInfo: 'showInfo'
  },
  components: [
    {
      name: 'label',
      classes: 'obrdmUiOrderLineInfoLine-label'
    },
    {
      name: 'value',
      classes: 'obrdmUiOrderLineInfoLine-value'
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.label.setContent(OB.I18N.getLabel(this.i18nLabel));
  },
  showInfo: function(inSender, inEvent) {
    if (inEvent.line.get(this.modelProperty)) {
      this.$.value.setContent(inEvent.line.get(this.modelProperty));
    }
    return true;
  }
});

enyo.kind({
  name: 'OBRDM.UI.OrderLineInfoTable',
  classes: 'obrdmUiOrderLineInfoTable',
  infoTableComponents: [],
  components: [],
  initComponents: function() {
    this.inherited(arguments);
    var me = this;
    _.each(this.infoTableComponents, function(comp) {
      me.createComponent(comp, {
        owner: me
      });
    });
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OBRDM.UI.OrderLineInfoPopup',
  name: 'OBRDM_UI_OrderLineInfoPopup'
});
