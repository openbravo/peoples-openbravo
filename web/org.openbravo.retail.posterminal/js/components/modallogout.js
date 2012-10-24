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
  myId: 'logoutDialog',
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
      //Cancel button	
      kind: 'OB.UI.Button',
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      attributes: {
        'data-dismiss': 'modal'
      },
      content: OB.I18N.getLabel('OBPOS_LblCancel')
    }]
  }
});

enyo.kind({
  name: 'OB.UI.LogoutDialogLogout',
  kind: 'OB.UI.Button',
  content: OB.I18N.getLabel('OBPOS_LogoutDialogLogout'),
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  tap: function () {
    $('#logoutDialog').modal('hide');
    OB.POS.logout();
  }
});

enyo.kind({
  name: 'OB.UI.LogoutDialogLock',
  kind: 'OB.UI.Button',
  content: OB.I18N.getLabel('OBPOS_LogoutDialogLock'),
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  tap: function () {
    $('#logoutDialog').modal('hide');
    OB.POS.lock();
  }
});