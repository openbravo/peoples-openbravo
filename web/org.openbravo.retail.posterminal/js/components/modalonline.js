/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalOnline',
  myId: 'modalOnline',
  header: OB.I18N.getLabel('OBPOS_Online'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_OnlineConnectionHasReturned')
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.Button',
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      content: OB.I18N.getLabel('OBPOS_LblOk'),
      tap: function () {
        window.location = window.location.pathname + '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal);
      }
    }, {
      kind: 'OB.UI.Button',
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      content: OB.I18N.getLabel('OBPOS_LblCancel'),
      tap: function () {
        $('#' + this.parent.parent.parent.parent.parent.getId()).modal('hide');
        OB.POS.navigate('retail.pointofsale');
      }
    }]
  },
  initComponents: function () {
    this.inherited(arguments);

  }
});