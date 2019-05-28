/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function () {

  if (OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true) && OB.UTIL.HookManager) {

    OB.UTIL.HookManager.registerHook('OBPOS_NewReceipt', function (args, callbacks) {
      if (!args.newOrder.get('obrdmDeliveryModeProperty')) {
        args.newOrder.set('obrdmDeliveryModeProperty', 'PickAndCarry');
      }
      OB.UTIL.HookManager.callbackExecutor(args, callbacks);
    });

  }
}());