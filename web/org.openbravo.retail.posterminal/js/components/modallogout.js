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
  name: 'OB.UI.ModalLogout',
  kind: 'OB.UI.ModalAction',
  header: OB.I18N.getLabel('OBPOS_LogoutDialogLogout'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_LogoutDialogText')
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.LogoutDialogLogout'
    },
    //,{ kind: 'OB.UI.LogoutDialogLock' //Disabled until feature be ready}
    {
      kind: 'OB.UI.LogoutDialogCancel'
    }]
  }
});

enyo.kind({
  name: 'OB.UI.LogoutDialogCancel',
  kind: 'OB.UI.ModalDialogButton',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  tap: function () {
    this.doHideThisPopup();
  }
});

enyo.kind({
  name: 'OB.UI.LogoutDialogLogout',
  kind: 'OB.UI.ModalDialogButton',
  isDefaultAction: true,
  content: OB.I18N.getLabel('OBPOS_LogoutDialogLogout'),
  tap: function () {
    this.doHideThisPopup();
    OB.UTIL.showLoggingOut(true);
    OB.POS.logout();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.LogoutDialogLock',
  content: OB.I18N.getLabel('OBPOS_LogoutDialogLock'),
  tap: function () {
    this.doHideThisPopup();
    OB.POS.lock();
  }
});