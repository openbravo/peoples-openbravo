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
  header: OB.I18N.getLabel('OBPOS_Online'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_OnlineConnectionHasReturned')
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ModalDialogButton',
      content: OB.I18N.getLabel('OBPOS_LblLoginAgain'),
      isDefaultAction: true,
      tap: function () {
        OB.POS.terminal.$.dialogsContainer.$.modalOnline.hide();
        window.location = window.location.pathname + '?terminal=' + window.encodeURIComponent(OB.POS.paramTerminal);
      }
    }, {
      kind: 'OB.UI.ModalDialogButton',
      content: OB.I18N.getLabel('OBPOS_LblCancel'),
      tap: function () {
        OB.POS.terminal.$.dialogsContainer.$.modalOnline.hide();
        OB.POS.navigate('retail.pointofsale');
      }
    }]
  },
  initComponents: function () {
    this.inherited(arguments);
  }
});