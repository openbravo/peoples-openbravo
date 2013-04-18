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
  style: 'width: 100px; border: 2px solid #cccccc;',
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
    this.setContent(this.model.get('_identifier'));
  }
});