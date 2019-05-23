/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.UTIL.HookManager.registerHook('OBPOS_NewReceipt', function (args, callbacks) {
  if (!args.newOrder.get('obrdmDeliveryModeProperty')) {
    args.newOrder.set('obrdmDeliveryModeProperty', 'PickAndCarry');
  }
  OB.UTIL.HookManager.callbackExecutor(args, callbacks);
});