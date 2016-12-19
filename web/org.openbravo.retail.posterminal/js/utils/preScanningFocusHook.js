/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.UTIL.HookManager.registerHook('OBMOBC_PreScanningFocus', function (args, callbacks) {
  if (OB.UTIL.RfidController.isRfidConfigured() && OB.UTIL.RfidController.get('rfidWebsocket')) {
    if (args.scanMode === true && !OB.UTIL.RfidController.get('isRFIDEnabled') && OB.UTIL.RfidController.get('reconnectOnScanningFocus') && (OB.MobileApp.view.originalRFIDMode || (OB.MobileApp.view.originalRFIDMode === false && OB.MobileApp.model.get('lastPaneShown') !== 'payment'))) {
      OB.UTIL.RfidController.connectRFIDDevice();
    } else if (args.scanMode === false && OB.UTIL.RfidController.get('isRFIDEnabled')) {
      OB.UTIL.RfidController.disconnectRFIDDevice();
    }
  }
  OB.UTIL.HookManager.callbackExecutor(args, callbacks);
});