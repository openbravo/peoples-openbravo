/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.UTIL.HookManager.registerHook('OBMOBC_PreScanningFocus', function (args, callbacks) {
  if (args.scanMode && OB.UTIL.isRfidConfigured() && OB.UTIL.rfidWebsocket  && !OB.UTIL.isRFIDEnabled && OB.UTIL.reconnectOnScanningFocus) {
    OB.UTIL.connectRFIDDevice();
  } else if (args.scanMode === false && OB.UTIL.isRfidConfigured() && OB.UTIL.rfidWebsocket && OB.UTIL.isRFIDEnabled) {
    OB.UTIL.disconnectRFIDDevice();
  }
  OB.UTIL.HookManager.callbackExecutor(args, callbacks);
});