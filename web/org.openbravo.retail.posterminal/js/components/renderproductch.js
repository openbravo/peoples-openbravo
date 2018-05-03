/*
 ************************************************************************************
 * Copyright (C) 2013-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.RenderProductCh',
  avoidDoubleClick: false,
  style: 'width: 86%; padding: 0px; text-overflow: ellipsis; overflow: hidden; white-space: nowrap;',
  classes: 'btnlink-white-simple',
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
    if (this.model.get('_identifier').length < 13) {
      this.setContent(this.model.get('_identifier'));
    } else {
      this.setContent(this.model.get('_identifier').substring(0, 10) + '...');
    }
    if (this.model.get('filtering')) {
      this.addClass('btnlink-yellow-bold');
    }
  }
});