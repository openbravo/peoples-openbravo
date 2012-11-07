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
  kind: 'OB.UI.Button',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  attributes:{
    'onEnterTap': 'hide'
  },
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
  }
});

enyo.kind({
  name: 'OB.UI.LogoutDialogLogout',
  kind: 'OB.UI.Button',
  isApplyButton: true,
  content: OB.I18N.getLabel('OBPOS_LogoutDialogLogout'),
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
    OB.POS.logout();
  }
});

enyo.kind({
  name: 'OB.UI.LogoutDialogLock',
  kind: 'OB.UI.Button',
  content: OB.I18N.getLabel('OBPOS_LogoutDialogLock'),
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
    OB.POS.lock();
  }
});