/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.RenderProductCh',
  style: 'width: 150px; border: 2px solid #cccccc;',
  classes: 'btnlink-white',
  events: {
    onShowPopup: ''
  },
  tap: function () {
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalproductcharacteristic',
        args: {
          model: this.model
        }
      });
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    if (this.model.get('_identifier').length < 16) {
      this.setContent(this.model.get('_identifier'));
    } else {
      this.setContent(this.model.get('_identifier').substring(0, 13) + '...');
    }

  }
});

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.RenderEmptyCh',
  style: 'width: 150px; border: 2px solid #cccccc;',
  classes: 'btnlink-white',
  events: {
    onClearAction: ''
  },
  tap: function () {
    this.doClearAction();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent('Clear Filters');
  }
});