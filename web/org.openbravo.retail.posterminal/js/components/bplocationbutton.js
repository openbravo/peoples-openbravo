/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 * 
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global enyo, Backbone */

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.BPLocation',
  classes: 'btnlink btnlink-small btnlink-gray',
  published: {
    order: null
  },
  events: {
    onShowPopup: ''
  },
  handlers: {
    onBPLocSelectionDisabled: 'buttonDisabled'
  },
  buttonDisabled: function (inSender, inEvent) {
    this.setDisabled(inEvent.status);
  },
  tap: function () {
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'OB_UI_ModalBPLocation'
      });
    }
  },
  initComponents: function () { },
  renderBPLocation: function (newLocation) {
    this.setContent(newLocation);
  },
  orderChanged: function (oldValue) {
    if (this.order.get('bp')) {
      this.renderBPLocation(this.order.get('bp').get('locName'));
    } else {
      this.renderBPLocation('');
    }

    this.order.on('change:bp', function (model) {
      if (model.get('bp')) {
        this.renderBPLocation(model.get('bp').get('locName'));
      } else {
        this.renderBPLocation('');
      }
    }, this);
  }
});
